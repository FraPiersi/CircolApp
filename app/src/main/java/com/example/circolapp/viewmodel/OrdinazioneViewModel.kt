package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Ordine
import com.example.circolapp.model.Product // Assicurati di avere questo modello
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrdinazioneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("prodotti")
    private val ordiniCollection = db.collection("ordinazioni")
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    val productNamesAdapter = MutableLiveData<ArrayAdapter<String>>()

    // Dati del form
    val selectedProduct = MutableLiveData<Product?>()
    val richiesteAggiuntive = MutableLiveData<String>("")

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _ordineInviato = MutableLiveData<Boolean>()
    val ordineInviato: LiveData<Boolean> get() = _ordineInviato

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Per abilitare/disabilitare il bottone di invio
    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(selectedProduct) { value = isProductSelected() }
    }

    private fun isProductSelected(): Boolean {
        return selectedProduct.value != null
    }


    init {
        loadProducts()
    }

    private fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = productsCollection
                    .orderBy("nome", Query.Direction.ASCENDING)
                    .get()
                    .await()
                val productList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }
                _products.value = productList

                // Prepara l'adapter per lo spinner
                val productNames = productList.map { it.nome }
                val adapter = ArrayAdapter(
                    getApplication<Application>().applicationContext,
                    android.R.layout.simple_dropdown_item_1line, // Layout standard per dropdown
                    productNames
                )
                productNamesAdapter.value = adapter

            } catch (e: Exception) {
                Log.e("OrdinazioneVM", "Error loading products", e)
                _errorMessage.value = "Errore nel caricamento dei prodotti: ${e.message}"
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onProductSelected(position: Int) {
        selectedProduct.value = _products.value?.getOrNull(position)
    }

    fun inviaOrdine() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _errorMessage.value = "Utente non autenticato. Effettua il login."
            return
        }

        val prodottoSelezionato = selectedProduct.value
        if (prodottoSelezionato == null) {
            _errorMessage.value = "Per favore, seleziona un prodotto."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val nuovoOrdine = Ordine(
                    uidUtente = currentUser.uid,
                    nomeProdotto = prodottoSelezionato.nome,
                    prodottoId = prodottoSelezionato.id!!, // Salva l'ID del prodotto
                    richiesteAggiuntive = richiesteAggiuntive.value?.takeIf { it.isNotBlank() }
                    // timestamp e stato verranno impostati da Firestore (ServerTimestamp) e dal default
                )

                ordiniCollection.add(nuovoOrdine).await() // .add() genera un ID automatico
                _ordineInviato.value = true
                _errorMessage.value = null // Resetta eventuali errori precedenti
                // Resetta i campi del form dopo l'invio
                selectedProduct.postValue(null) // Usa postValue se da coroutine non principale (anche se qui siamo su main)
                richiesteAggiuntive.postValue("")

            } catch (e: Exception) {
                Log.e("OrdinazioneVM", "Errore nell'invio dell'ordine", e)
                _errorMessage.value = "Errore nell'invio dell'ordine: ${e.message}"
                _ordineInviato.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onOrdineInviatoHandled() {
        _ordineInviato.value = false // Resetta lo stato per evitare trigger multipli
    }

    fun onErrorMessageHandled() {
        _errorMessage.value = null
    }
}