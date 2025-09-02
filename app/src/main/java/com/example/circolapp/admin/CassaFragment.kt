package com.example.circolapp.admin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.circolapp.R
import com.example.circolapp.ui.dialog.BarcodeScannerDialogFragment
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import android.graphics.Bitmap
import androidx.navigation.findNavController

class CassaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cassa, container, false)
        val btnRicarica = view.findViewById<Button>(R.id.btnRicarica)
        val btnRiscuoti = view.findViewById<Button>(R.id.btnRiscuoti)
        btnRicarica.setOnClickListener {
            val cameraPermission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(requireContext(), cameraPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(cameraPermission)
            } else {
                showBarcodeScannerDialog()
            }
        }
        btnRiscuoti.setOnClickListener {
            val action = CassaFragmentDirections.actionCassaFragmentToRiscuotiFragment("", "")
            requireActivity().findNavController(R.id.nav_host_fragment_main).navigate(action)
        }
        return view
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showBarcodeScannerDialog()
        } else {
            Toast.makeText(requireContext(), "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBarcodeScannerDialog() {
        val dialog = BarcodeScannerDialogFragment { qrCode ->
            val action = CassaFragmentDirections.actionCassaFragmentToRicaricaFragment(qrCode)
            requireActivity().runOnUiThread {
                requireActivity().findNavController(R.id.nav_host_fragment_main).navigate(action)
            }
        }
        dialog.show(parentFragmentManager, "BarcodeScannerDialog")
    }
}