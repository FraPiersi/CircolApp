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
import androidx.camera.view.PreviewView 
import androidx.core.content.ContextCompat
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

    private var _binding: FragmentAddEditProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditProductViewModel by viewModels()
    private val args: AddEditProductFragmentArgs by navArgs()

    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setSelectedImageUri(uri)
            displaySelectedImage(uri)
            binding.btnRemoveImage.visibility = View.VISIBLE
        }
    }

    
    private lateinit var cameraExecutor: ExecutorService
    private var barcodeScanner: BarcodeScanner? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraPreview: Preview? = null 
    private var scannerDialog: AlertDialog? = null 
    private var cameraXPreviewView: PreviewView? = null 
    

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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_barcode_scanner, null)
        cameraXPreviewView = dialogView.findViewById(R.id.camerax_preview_view_scanner) 

        scannerDialog = AlertDialog.Builder(requireContext())
            .setTitle("Scansiona Codice Prodotto")
            .setView(dialogView)
            .setNegativeButton("Annulla") { dialog, _ ->
                stopCameraAndScanner()
                dialog.dismiss()
            }
            .setOnDismissListener {
                stopCameraAndScanner() 
            }
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
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e("BarcodeScan", "Errore nell'ottenere CameraProvider", e)
                Toast.makeText(context, "Errore fotocamera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        if (cameraProvider == null) {
            Log.e("BarcodeScan", "CameraProvider non inizializzato.")
            return
        }
        if (cameraXPreviewView == null) {
            Log.e("BarcodeScan", "CameraXPreviewView non è disponibile nel dialog.")
            return
        }

        
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        
        cameraPreview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(cameraXPreviewView!!.surfaceProvider)
            }

        
        imageAnalysis = ImageAnalysis.Builder()
            
            
            .setTargetResolution(Size(1280, 720)) 
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) 
            .build()

        imageAnalysis?.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                barcodeScanner!!.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val firstBarcode = barcodes[0]
                            val barcodeValue = firstBarcode.rawValue
                            Log.d("BarcodeScan", "Barcode CameraX scansionato: $barcodeValue")

                            
                            requireActivity().runOnUiThread {
                                viewModel.productCode.postValue(barcodeValue)
                                scannerDialog?.dismiss() 
                                stopCameraAndScanner()   
                                Toast.makeText(context, "Codice scansionato: $barcodeValue", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("BarcodeScan", "Errore durante la scansione del barcode con CameraX", e)
                        
                    }
                    .addOnCompleteListener {
                        imageProxy.close() 
                    }
            } else {
                imageProxy.close() 
            }
        })

        try {
            cameraProvider!!.unbindAll() 
            cameraProvider!!.bindToLifecycle(
                this as LifecycleOwner, 
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
            cameraProvider?.unbindAll() 
        } catch (e: Exception) {
            Log.e("BarcodeScan", "Errore nello svincolare CameraX Use Cases", e)
        }
        barcodeScanner?.close() 
        barcodeScanner = null
        
        
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
                        val success = findNavController().popBackStack()
                        Log.d("AddEditProduct", "popBackStack success: $success. New current destination: ${findNavController().currentDestination?.label}")
                        if (!success) {
                            Log.e("AddEditProduct", "popBackStack FAILED!")
                        }
                    }
                    is AddEditProductEvent.ProductDeleted -> {
                        Toast.makeText(context, "Prodotto eliminato!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is AddEditProductEvent.Error -> {
                        Toast.makeText(context, "Errore: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                    is AddEditProductEvent.ImageUploadStarted -> {
                        Toast.makeText(context, "Caricamento immagine...", Toast.LENGTH_SHORT).show()
                    }
                    is AddEditProductEvent.ImageUploadCompleted -> {
                        Toast.makeText(context, "Immagine caricata con successo!", Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.onEventHandled()
            }
        })

        viewModel.isCodeEditable.observe(viewLifecycleOwner, Observer { isEditable ->
            binding.etProductCode.isEnabled = isEditable
            binding.tilProductCode.isEnabled = isEditable
            binding.btnScanBarcode.visibility = if(isEditable) View.VISIBLE else View.GONE
            if (!isEditable) {
                binding.tilProductCode.helperText = "Il codice non è modificabile per prodotti esistenti."
            } else {
                binding.tilProductCode.helperText = null
            }
        })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Conferma Eliminazione")
            .setMessage("Sei sicuro di voler eliminare questo prodotto? L'azione è irreversibile.")
            .setPositiveButton("Elimina") { _, _ -> viewModel.deleteProduct() }
            .setNegativeButton("Annulla", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCameraAndScanner() // Assicurati che le risorse della camera siano rilasciate
        cameraExecutor.shutdown() // Arresta l'executor
        _binding = null
        cameraXPreviewView = null // Pulisci riferimento alla view del dialog
        scannerDialog = null
    }
}
