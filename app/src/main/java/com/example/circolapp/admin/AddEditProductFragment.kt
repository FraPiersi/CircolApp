package com.example.circolapp.admin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewViewimport androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.circolapp.R
import com.example.circolapp.databinding.FragmentAddEditProductBinding
import com.example.circolapp.viewmodel.AddEditProductEvent
import com.example.circolapp.viewmodel.AddEditProductViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddEditProductFragment : Fragment() {

    private    private    private
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Permesso fotocamera concesso", Toast.LENGTH_SHORT).show()
                startBarcodeScanningFlow()
            } else {
                Toast.makeText(context, "Permesso fotocamera negato. Impossibile scansionare.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_product, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.start(args.productId)
        setupObservers()
        setupImageHandling()

        if (args.productId != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Modifica Prodotto"
            binding.btnDeleteProduct.visibility = View.VISIBLE
            binding.btnDeleteProduct.setOnClickListener { showDeleteConfirmationDialog() }
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Aggiungi Prodotto"
            binding.btnDeleteProduct.visibility = View.GONE
        }

        binding.btnScanBarcode.setOnClickListener {
            checkCameraPermissionAndScan()
        }

        binding.btnSaveProduct.setOnClickListener {
            viewModel.saveProduct()
        }
    }

    private fun setupImageHandling() {
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnRemoveImage.setOnClickListener {
            viewModel.removeImage()
            resetImageDisplay()
            binding.btnRemoveImage.visibility = View.GONE
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(binding.ivProductImage)
    }

    private fun displayImageFromUrl(url: String?) {
        if (url.isNullOrEmpty()) {
            resetImageDisplay()
            return
        }

        Glide.with(this)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(binding.ivProductImage)
        
        binding.btnRemoveImage.visibility = View.VISIBLE
    }

    private fun resetImageDisplay() {
        binding.ivProductImage.setImageResource(R.drawable.ic_image_placeholder)
    }

    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startBarcodeScanningFlow()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permesso Fotocamera Richiesto")
                    .setMessage("L'app necessita del permesso per accedere alla fotocamera per scansionare i codici a barre.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startBarcodeScanningFlow() {

        scannerDialog = AlertDialog.Builder(requireContext())
            .setTitle("Scansiona Codice Prodotto")
            .setView(dialogView)
            .setNegativeButton("Annulla") { dialog, _ ->
                stopCameraAndScanner()
                dialog.dismiss()
            }
            .setOnDismissListener {
                stopCameraAndScanner()            }
            .create()

        if (cameraXPreviewView == null) {
            Toast.makeText(context, "Errore: CameraX PreviewView non trovata nel dialog.", Toast.LENGTH_LONG).show()
            Log.e("BarcodeScan", "CameraX PreviewView is null in dialog.")
            return
        }

        setupCamera()
        scannerDialog?.show()
    }

    private fun setupCamera() {
        cameraPreview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(cameraXPreviewView!!.surfaceProvider)
            }

        // ImageAnalysis Use Case
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)            .build()

        imageAnalysis?.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                            requireActivity().runOnUiThread {
                                viewModel.productCode.postValue(barcodeValue)
                                scannerDialog?.dismiss()                                stopCameraAndScanner()                                Toast.makeText(context, "Codice scansionato: $barcodeValue", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("BarcodeScan", "Errore durante la scansione del barcode con CameraX", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()                    }
            } else {
                imageProxy.close()            }
        })

        try {
            cameraProvider!!.unbindAll() // Svincola use case precedenti
            cameraProvider!!.bindToLifecycle(
                this as LifecycleOwner, // Il Fragment è un LifecycleOwner
                cameraSelector,
                cameraPreview,
                imageAnalysis
            )
            Log.d("BarcodeScan", "CameraX Use Cases associati al lifecycle.")
        } catch (exc: Exception) {
            Log.e("BarcodeScan", "Errore nell'associare CameraX Use Cases", exc)
            Toast.makeText(context, "Impossibile avviare fotocamera: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopCameraAndScanner() {
        Log.d("BarcodeScan", "Stopping CameraX and BarcodeScanner")
        try {
            cameraProvider?.unbindAll() // Svincola tutti gli use case
        } catch (e: Exception) {
            Log.e("BarcodeScan", "Errore nello svincolare CameraX Use Cases", e)
        }
        barcodeScanner?.close() // Rilascia lo scanner ML Kit
        barcodeScanner = null
        // Non è necessario nullificare cameraProvider, cameraPreview, imageAnalysis
        // perché verranno reinizializzati se necessario.
        // cameraXPreviewView diventerà null quando il dialog viene distrutto.
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBarAddEdit.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveProduct.isEnabled = !isLoading
            binding.btnDeleteProduct.isEnabled = !isLoading
            binding.btnScanBarcode.isEnabled = !isLoading && viewModel.isCodeEditable.value == true
            binding.btnSelectImage.isEnabled = !isLoading
        })

        // Observer per l'URL dell'immagine del prodotto
        viewModel.productImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            displayImageFromUrl(imageUrl)
        }

        // Observer per l'immagine selezionata localmente
        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                displaySelectedImage(uri)
                binding.btnRemoveImage.visibility = View.VISIBLE
            }
        }

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            Log.d("AddEditFragment_Event", "Received event: $event")
            event?.let {
                Log.d("AddEditFragment_Event", "Processing event: ${it::class.java.simpleName}, value: $it")
                when (it) {
                    is AddEditProductEvent.ProductSaved -> {
                        Log.d("AddEditFragment_Event", "ProductSaved event HANDLED. Showing Toast...")
                        Toast.makeText(context, "Prodotto salvato!", Toast.LENGTH_SHORT).show()
                        Log.d("AddEditProduct", "Current destination before pop: ${findNavController().currentDestination?.label}")
        cameraExecutor.shutdown() // Arresta l'executor
        _binding = null
        cameraXPreviewView = null // Pulisci riferimento alla view del dialog
        scannerDialog = null
    }
}
