package com.dlsu.unisync.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dlsu.unisync.R
import com.dlsu.unisync.databinding.FragmentQrBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

// QR check-in with a real camera scanner (CameraX + ML Kit, bundled model, works
// offline). The simulate button remains for emulators without a camera.
class QrFragment : Fragment() {
    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                binding.checkInStatus.text = getString(R.string.qr_permission_denied)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.scanButton.setOnClickListener {
            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                startCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.checkInButton.setOnClickListener {
            binding.checkInStatus.text = getString(R.string.qr_status_done)
        }
    }

    private fun startCamera() {
        binding.qrBox.isVisible = false
        binding.cameraPreview.isVisible = true
        binding.checkInStatus.text = getString(R.string.qr_scanning)

        val providerFuture = ProcessCameraProvider.getInstance(requireContext())
        providerFuture.addListener({
            // The view may be gone by the time the provider is ready.
            if (_binding == null) return@addListener
            val provider = providerFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val analyzer = QrAnalyzer { value -> onQrScanned(provider, value) }
            analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), analyzer)

            // bindToLifecycle releases the camera automatically when the view dies.
            provider.unbindAll()
            provider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun onQrScanned(provider: ProcessCameraProvider, value: String) {
        if (_binding == null) return
        provider.unbindAll()
        binding.cameraPreview.isVisible = false
        binding.qrBox.isVisible = true
        binding.checkInStatus.text = getString(R.string.qr_checked_in_scanned, value)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // Feeds camera frames to ML Kit and reports the first QR code found.
    private class QrAnalyzer(private val onQrFound: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        private var handled = false

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage == null || handled) {
                imageProxy.close()
                return
            }
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val value = barcodes.firstOrNull()?.rawValue
                    if (value != null && !handled) {
                        handled = true
                        onQrFound(value)
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}
