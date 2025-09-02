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

    private            val inputStream = getApplication<Application>().contentResolver.openInputStream(imageUri)
                ?: throw Exception("Impossibile leggere il file immagine")
            inputStream.close()

            val storageRef = storage.reference
            val timestamp = System.currentTimeMillis()
            val fileName = "${productId}_${timestamp}.jpg"
            val imageRef = storageRef.child("product_images/$fileName")

            Log.d("ImageUpload", "Iniziando upload per: $fileName")
            Log.d("ImageUpload", "Storage reference: ${imageRef.path}")

            // Upload del file
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
