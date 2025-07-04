package com.example.circolapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductCatalogViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("prodotti")

    private val _allProducts = MutableLiveData<List<Product>>() // Lista originale da Firestore
    // private val allProductsList: List<Product> = listOf() // Sostituito da _allProducts

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> get() = _filteredProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val searchQuery = MutableLiveData<String>("")
    private var searchJob: Job? = null

    init {
        loadProducts()
        observeSearchQuery()
    }

    private fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = productsCollection
                    .orderBy("nome", Query.Direction.ASCENDING) // Ordina per nome, ad esempio
                    .get()
                    .await()

                val products = snapshot.documents.mapNotNull { doc ->
                    // Manual mapping in case toObject fails or for custom logic
                    try {
                        val product = doc.toObject(Product::class.java)
                        product?.copy(id = doc.id) // Assicurati che l'ID sia nel modello
                    } catch (e: Exception) {
                        Log.e("ProductCatalogVM", "Error parsing product ${doc.id}", e)
                        null
                    }
                }
                _allProducts.value = products
                _filteredProducts.value = products // Inizialmente mostra tutti i prodotti
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("ProductCatalogVM", "Error loading products", e)
                _errorMessage.value = "Errore nel caricamento dei prodotti: ${e.message}"
                _allProducts.value = emptyList()
                _filteredProducts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeSearchQuery() {
        searchQuery.observeForever { query ->
            searchJob?.cancel() // Cancella la ricerca precedente se l'utente digita velocemente
            searchJob = viewModelScope.launch {
                delay(300) // Debounce: attendi 300ms prima di filtrare
                filterProducts(query)
            }
        }
    }

    private fun filterProducts(query: String?) {
        val currentProducts = _allProducts.value ?: emptyList()
        if (query.isNullOrBlank()) {
            _filteredProducts.value = currentProducts
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            _filteredProducts.value = currentProducts.filter { product ->
                product.nome.lowercase().contains(lowerCaseQuery) ||
                        product.descrizione.lowercase().contains(lowerCaseQuery)
                // Aggiungi altri campi per la ricerca se necessario
            }
        }
    }

    fun getProductById(productId: String): Product? {
        return _allProducts.value?.firstOrNull { it.id == productId }
    }


    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        searchQuery.removeObserver { } // Rimuovi l'observer per evitare leak
    }
}