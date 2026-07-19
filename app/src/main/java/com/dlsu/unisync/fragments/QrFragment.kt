package com.dlsu.unisync.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.databinding.FragmentQrBinding
import com.dlsu.unisync.models.SimpleItem
import com.dlsu.unisync.viewmodels.CheckInsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// QR check-in with a real camera scanner (CameraX + ML Kit, bundled model, works
// offline). Scanned codes must match the UniSync payload format; accepted
// check-ins are persisted and listed. The simulate button remains for emulators.
class QrFragment : Fragment() {
    private val checkInsViewModel: CheckInsViewModel by activityViewModels { CheckInsViewModel.Factory }
    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!
    private var analyzer: QrAnalyzer? = null
    private val timeFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> startCamera()
                // Permanently denied: the system dialog will never show again,
                // so offer the app-settings page instead of a dead end.
                !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> showSettingsPrompt()
                else -> binding.checkInStatus.text = getString(R.string.qr_permission_denied)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recentRecycler.layoutManager = LinearLayoutManager(requireContext())
        checkInsViewModel.checkIns.observe(viewLifecycleOwner) { checkIns ->
            binding.recentRecycler.adapter = SimpleItemAdapter(
                checkIns.map {
                    SimpleItem(it.course, "${it.room} • ${timeFormat.format(Date(it.timestamp))}")
                }
            )
            binding.recentEmpty.isVisible = checkIns.isEmpty()
        }

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
            recordCheckIn(course = "MOBDEVE", room = "Gokongwei 305")
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
            analyzer?.close()
            val qrAnalyzer = QrAnalyzer { value -> onQrScanned(provider, value) }
            analyzer = qrAnalyzer
            analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), qrAnalyzer)

            // bindToLifecycle releases the camera automatically when the view dies.
            provider.unbindAll()
            provider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun onQrScanned(provider: ProcessCameraProvider, value: String) {
        if (_binding == null) return
        provider.unbindAll()
        analyzer?.close()
        analyzer = null
        binding.cameraPreview.isVisible = false
        binding.qrBox.isVisible = true

        // Only UniSync payloads count as check-ins; anything else (URLs, random
        // codes, oversized strings) is rejected without echoing its content.
        val match = CHECK_IN_PATTERN.matchEntire(value)
        if (match != null) {
            recordCheckIn(course = match.groupValues[1], room = match.groupValues[2])
        } else {
            binding.checkInStatus.text = getString(R.string.qr_invalid)
        }
    }

    private fun recordCheckIn(course: String, room: String) {
        checkInsViewModel.addCheckIn(course, room)
        binding.checkInStatus.text = getString(R.string.qr_checked_in, course, room)
    }

    private fun showSettingsPrompt() {
        Snackbar.make(binding.root, R.string.qr_permission_blocked, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_settings) {
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
            }
            .show()
    }

    override fun onDestroyView() {
        analyzer?.close()
        analyzer = null
        _binding = null
        super.onDestroyView()
    }

    // Feeds camera frames to ML Kit and reports the first QR code found.
    private class QrAnalyzer(private val onQrFound: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        private var handled = false

        @Volatile
        private var closed = false

        fun close() {
            closed = true
            scanner.close()
        }

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage == null || handled || closed) {
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

    companion object {
        // Expected QR payload: unisync://checkin/<course>/<room>
        private val CHECK_IN_PATTERN = Regex("unisync://checkin/([A-Za-z0-9 _-]{1,40})/([A-Za-z0-9 _-]{1,40})")
    }
}
