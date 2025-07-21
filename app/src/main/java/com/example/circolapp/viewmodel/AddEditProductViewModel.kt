package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

sealed class AddEditProductEvent {
    object ProductSaved : AddEditProductEvent()
    object ProductDeleted : AddEditProductEvent()
    data class Error(val message: String) : AddEditProductEvent()
    // object CodeCheckResult : AddEditProductEvent() // Rimosso se non strettamente usato
}

class AddEditProductViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("prodotti")

    // Dati del prodotto
    val productName = MutableLiveData<String>()
    val productCode = MutableLiveData<String>() // Questo è l'ID
    val productDescription = MutableLiveData<String>()
    val productPieces = MutableLiveData<String>()
    val productCategory = MutableLiveData<String>()
    val productAmount = MutableLiveData<String>() // << NUOVO LiveData per l'importo (come String per input)

    private val _isCodeEditable = MutableLiveData<Boolean>(true)
    val isCodeEditable: LiveData<Boolean> get() = _isCodeEditable

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _event = MutableLiveData<AddEditProductEvent?>()
    val event: LiveData<AddEditProductEvent?> get() = _event

    private var isEditMode = false

    fun start(productId: String?) {
        isEditMode = productId != null
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
            productCategory.value = ""
            productAmount.value = "" // Inizializza l'importo
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
                        // productCode è già settato
                        productDescription.value = it.descrizione
                        productPieces.value = it.numeroPezzi.toString()
                        // Formatta il double in stringa per il campo di testo, considerando la localizzazione
                        productAmount.value = formatDoubleToString(it.importo)
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

    fun saveProduct() {
        val name = productName.value?.trim()
        val codeId = productCode.value?.trim()
        val description = productDescription.value?.trim()
        val piecesStr = productPieces.value
        val category = productCategory.value?.trim()
        val amountStr = productAmount.value?.trim() // Ottieni l'importo come stringa

        // Aggiungi amountStr alla validazione dei campi obbligatori
        if (name.isNullOrEmpty() || codeId.isNullOrEmpty() || description.isNullOrEmpty() ||
            piecesStr.isNullOrEmpty() || category.isNullOrEmpty() || amountStr.isNullOrEmpty()) {
            _event.value = AddEditProductEvent.Error("Tutti i campi sono obbligatori.")
            return
        }

        if (codeId.contains("/") /* || altri caratteri non validi */) {
            _event.value = AddEditProductEvent.Error("Il codice prodotto non può contenere caratteri speciali come '/'.")
            return
        }

        val pieces = piecesStr.toIntOrNull()
        if (pieces == null || pieces < 0) {
            _event.value = AddEditProductEvent.Error("Numero pezzi non valido.")
            return
        }

        // Converti e valida l'importo
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

            val product = Product(
                id = codeId,
                nome = name,
                descrizione = description,
                numeroPezzi = pieces,
                importo = amount, // Salva l'importo come Double
                imageUrl = null
            )
            Log.d("ViewModel_Save", "Product object before saving: ${product.toString()}")
            productsCollection.document(codeId)
                .set(product)
                .addOnSuccessListener {
                    // In AddEditProductViewModel -> saveProduct() -> addOnSuccessListener
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

    // Helper per formattare Double in Stringa per l'EditText (es. "12.99")
    private fun formatDoubleToString(value: Double): String {
        // Usa NumberFormat per una formattazione più robusta se necessario,
        // ma per input semplici, String.format può bastare.
        // Assicurati di usare il punto come separatore decimale per coerenza con l'input.
        return String.format(Locale.US, "%.2f", value) // Formatta a 2 decimali, usa Locale.US per avere '.'
    }

    // Helper per parsare la Stringa in Double dall'EditText
    private fun parseStringToDouble(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        return try {
            // Sostituisci la virgola con il punto se gli utenti potrebbero inserirla
            value.replace(',', '.').toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun onEventHandled() {
        _event.value = null
    }
}
