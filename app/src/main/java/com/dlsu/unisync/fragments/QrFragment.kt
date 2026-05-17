package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dlsu.unisync.R

// Simulated QR check-in screen for presentation flow.
class QrFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val statusText = view.findViewById<TextView>(R.id.checkInStatus)
        view.findViewById<Button>(R.id.checkInButton).setOnClickListener {
            statusText.text = "Checked in to MOBDEVE at Gokongwei 305."
        }
    }
}
