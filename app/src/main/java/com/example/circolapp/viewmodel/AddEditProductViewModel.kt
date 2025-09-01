package com.example.circolapp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

sealed class AddEditProductEvent {
    object ProductSaved : AddEditProductEvent()
    object ProductDeleted : AddEditProductEvent()
    data class Error(val message: String) : AddEditProductEvent()
    object ImageUploadStarted : AddEditProductEvent()
    object ImageUploadCompleted : AddEditProductEvent()
}

class AddEditProductViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val productsCollection = db.collection("prodotti")

    // Dati del prodotto
    val productName = MutableLiveData<String>()
    val productCode = MutableLiveData<String>()
    val productDescription = MutableLiveData<String>()
    val productPieces = MutableLiveData<String>()
    val productAmount = MutableLiveData<String>()
    val productOrdinabile = MutableLiveData<Boolean>(true)

    // Gestione immagine
    val productImageUrl = MutableLiveData<String?>()
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    private val _isCodeEditable = MutableLiveData<Boolean>(true)
    val isCodeEditable: LiveData<Boolean> get() = _isCodeEditable

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _event = MutableLiveData<AddEditProductEvent?>()
    val event: LiveData<AddEditProductEvent?> get() = _event

    private var isEditMode = false
    private var currentProductId: String? = null

    fun start(productId: String?) {
        isEditMode = productId != null
        currentProductId = productId
        if (isEditMode && productId != null) {
            _isCodeEditable.value = false
            productCode.value = productId
            loadProductData(productId)
        } else {
            _isCodeEditable.value = true
            productName.value = ""
            productCode.value = ""
            productDescription.value = ""
            productPieces.value = ""
            productAmount.value = ""
            productOrdinabile.value = true
        }
    }

    private fun loadProductData(productId: String) {
        _isLoading.value = true
        productsCollection.document(productId).get()
            .addOnSuccessListener { document ->
                _isLoading.value = false
                if (document != null && document.exists()) {
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        productName.value = it.nome
                        productDescription.value = it.descrizione
                        productPieces.value = it.numeroPezzi.toString()
                        productAmount.value = formatDoubleToString(it.importo)
                        productOrdinabile.value = it.ordinabile
                        productImageUrl.value = it.imageUrl
                    }
                } else {
                    _event.value = AddEditProductEvent.Error("Prodotto con codice '$productId' non trovato.")
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("ViewModel", "Errore caricamento prodotto: $productId", e)
                _event.value = AddEditProductEvent.Error("Errore caricamento: ${e.message}")
            }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun removeImage() {
        _selectedImageUri.value = null
        productImageUrl.value = null
    }

    private suspend fun uploadImageToStorage(imageUri: Uri, productId: String): String? {
        return try {
            _event.value = AddEditProductEvent.ImageUploadStarted

            // Verifica che l'URI sia valido
            val inputStream = getApplication<Application>().contentResolver.openInputStream(imageUri)
                ?: throw Exception("Impossibile leggere il file immagine")
            inputStream.close()

            val storageRef = storage.reference
            val timestamp = System.currentTimeMillis()
            val fileName = "${productId}_${timestamp}.jpg"
            val imageRef = storageRef.child("product_images/$fileName")

            Log.d("ImageUpload", "Iniziando upload per: $fileName")
            Log.d("ImageUpload", "Storage reference: ${imageRef.path}")

            // Upload del file
            val uploadTask = imageRef.putFile(imageUri).await()
            Log.d("ImageUpload", "Upload completato. Metadata: ${uploadTask.metadata?.path}")

            // Ottieni l'URL di download
            val downloadUrl = imageRef.downloadUrl.await()
            Log.d("ImageUpload", "Download URL ottenuto: $downloadUrl")

            _event.value = AddEditProductEvent.ImageUploadCompleted
            downloadUrl.toString()

        } catch (e: Exception) {
            Log.e("ImageUpload", "Errore dettagliato upload immagine", e)
            when (e) {
                is com.google.firebase.storage.StorageException -> {
                    Log.e("ImageUpload", "Storage error code: ${e.errorCode}")
                    Log.e("ImageUpload", "Storage error message: ${e.message}")
                    _event.value = AddEditProductEvent.Error("Errore Firebase Storage: ${e.message}")
                }
                is java.io.FileNotFoundException -> {
                    _event.value = AddEditProductEvent.Error("File immagine non trovato")
                }
                is java.io.IOException -> {
                    _event.value = AddEditProductEvent.Error("Errore di rete durante l'upload")
                }
                else -> {
                    _event.value = AddEditProductEvent.Error("Errore upload immagine: ${e.message}")
                }
            }
            null
        }
    }

    fun saveProduct() {
        val name = productName.value?.trim()
        val codeId = productCode.value?.trim()
        val description = productDescription.value?.trim()
        val piecesStr = productPieces.value
        val amountStr = productAmount.value?.trim()

        if (name.isNullOrEmpty() || codeId.isNullOrEmpty() || description.isNullOrEmpty() ||
            piecesStr.isNullOrEmpty() || amountStr.isNullOrEmpty()) {
            _event.value = AddEditProductEvent.Error("Tutti i campi sono obbligatori.")
            return
        }

        if (codeId.contains("/")) {
            _event.value = AddEditProductEvent.Error("Il codice prodotto non può contenere caratteri speciali come '/'.")
            return
        }

        val pieces = piecesStr.toIntOrNull()
        if (pieces == null || pieces < 0) {
            _event.value = AddEditProductEvent.Error("Numero pezzi non valido.")
            return
        }

        val amount = parseStringToDouble(amountStr)
        if (amount == null || amount < 0.0) {
            _event.value = AddEditProductEvent.Error("Importo non valido. Usa '.' come separatore decimale.")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            if (!isEditMode) {
                val existingDoc = productsCollection.document(codeId).get().await()
                if (existingDoc.exists()) {
                    _isLoading.value = false
                    _event.value = AddEditProductEvent.Error("Un prodotto con il codice '$codeId' esiste già.")
                    return@launch
                }
            }

            // Upload dell'immagine se selezionata
            var finalImageUrl = productImageUrl.value
            _selectedImageUri.value?.let { imageUri ->
                finalImageUrl = uploadImageToStorage(imageUri, codeId)
                if (finalImageUrl != null) {
                    productImageUrl.value = finalImageUrl
                }
            }

            val product = Product(
                id = codeId,
                nome = name,
                descrizione = description,
                numeroPezzi = pieces,
                importo = amount,
                imageUrl = finalImageUrl,
                ordinabile = productOrdinabile.value ?: true
            )

            Log.d("ViewModel_Save", "Product object before saving: ${product.toString()}")
            productsCollection.document(codeId)
                .set(product)
                .addOnSuccessListener {
                    Log.d("ViewModel_Save", "Attempting to post ProductSaved event")
                    _isLoading.value = false
                    _event.value = AddEditProductEvent.ProductSaved
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    Log.e("ViewModel", "Errore salvataggio prodotto $codeId", e)
                    _event.value = AddEditProductEvent.Error("Errore salvataggio: ${e.message}")
                }
        }
    }

    fun deleteProduct() {
        val codeIdToDelete = productCode.value?.trim()
        if (!isEditMode || codeIdToDelete.isNullOrEmpty()) {
            _event.value = AddEditProductEvent.Error("Nessun prodotto da eliminare.")
            return
        }
        _isLoading.value = true
        productsCollection.document(codeIdToDelete).delete()
            .addOnSuccessListener {
                _isLoading.value = false
                _event.value = AddEditProductEvent.ProductDeleted
            }
            .addOnFailureListener { e->
                _isLoading.value = false
                _event.value = AddEditProductEvent.Error("Errore eliminazione: ${e.message}")
            }
    }

    private fun formatDoubleToString(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun parseStringToDouble(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        return try {
            value.replace(',', '.').toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun onEventHandled() {
        _event.value = null
    }

    private suspend fun testStorageConnection(): Boolean {
        return try {
            val storageRef = storage.reference
            val testRef = storageRef.child("test/connection_test.txt")
            Log.d("StorageTest", "Testing storage connection...")
            Log.d("StorageTest", "Storage bucket: ${storage.reference.bucket}")

            try {
                testRef.metadata.await()
                Log.d("StorageTest", "Storage connection OK - file exists")
            } catch (e: Exception) {
                Log.d("StorageTest", "Storage connection OK - file doesn't exist (normal): ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e("StorageTest", "Storage connection FAILED", e)
            false
        }
    }
}
