package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Product
import com.example.circolapp.model.UserRole // Importa UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Stato della UI per il catalogo prodotti
data class ProductCatalogScreenState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val errorMessage: String? = null,
    val currentUserRole: UserRole? = null // <- Modificato da isUserAdmin a UserRole?
)

class ProductCatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = db.collection("prodotti")
    private var productsListener: ListenerRegistration? = null
    private var categoriesJob: Job? = null
    private var userRoleJob: Job? = null

    private val _screenState = MutableLiveData(ProductCatalogScreenState())
    val screenState: LiveData<ProductCatalogScreenState> get() = _screenState

    init {
        // Non chiamiamo più initialize() dal Fragment con il ruolo.
        // Il ViewModel ora si occupa di recuperare il ruolo.
        fetchCurrentUserRoleAndInitializeData()
    }

    private fun fetchCurrentUserRoleAndInitializeData() {
        if (userRoleJob?.isActive == true) return
        userRoleJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true) } // Inizia il caricamento generale
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                Log.w("ViewModel", "Nessun utente Firebase loggato.")
                updateState { it.copy(isLoading = false, currentUserRole = null, errorMessage = "Utente non autenticato.") }
                // Potresti voler emettere un evento per navigare al login
                return@launch
            }

            try {
                // Esempio: recupero ruolo da Firestore (adatta alla tua struttura)
                val userDoc = db.collection("utenti").document(firebaseUser.uid).get().await()
                val roleString = userDoc.getString("ruolo") // Assumendo campo "role"
                val role = roleString?.let { UserRole.valueOf(it.uppercase()) }

                if (role != null) {
                    Log.d("ViewModel", "Ruolo utente recuperato: $role")
                    updateState { it.copy(currentUserRole = role) }
                    // Solo dopo aver ottenuto il ruolo, carica categorie e prodotti
                    loadCategoriesAndInitialProducts()
                } else {
                    Log.w("ViewModel", "Ruolo utente non trovato o non valido in Firestore per ${firebaseUser.uid}")
                    updateState { it.copy(isLoading = false, currentUserRole = null, errorMessage = "Ruolo utente non definito.") }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Errore nel recuperare il ruolo utente", e)
                updateState { it.copy(isLoading = false, currentUserRole = null, errorMessage = "Errore recupero ruolo: ${e.localizedMessage}") }
            }
        }
    }


    private fun loadCategoriesAndInitialProducts() {
        if (_screenState.value?.currentUserRole == null) {
            Log.w("ViewModel", "Tentativo di caricare categorie/prodotti senza ruolo utente.")
            // Non procedere se il ruolo non è stato caricato.
            // fetchCurrentUserRoleAndInitializeData dovrebbe gestire questo.
            if (_screenState.value?.errorMessage == null) { // Evita di sovrascrivere un errore di ruolo
                updateState { it.copy(isLoading = false, errorMessage = "Impossibile caricare dati senza ruolo utente.") }
            }
            return
        }

        if (categoriesJob?.isActive == true) return
        categoriesJob = viewModelScope.launch {
            // Non impostare isLoading a true qui se fetchCurrentUserRoleAndInitializeData lo ha già fatto
            // updateState { it.copy(isLoading = true, errorMessage = null) }
            try {
                val snapshot = productsCollection.get().await()
                val distinctCategories = snapshot.documents
                    .mapNotNull { it.getString("categoria") }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                val categoriesWithDefault = mutableListOf<String>()
                val defaultCategory = "Tutte"
                categoriesWithDefault.add(defaultCategory)
                categoriesWithDefault.addAll(distinctCategories)

                updateState {
                    it.copy(
                        categories = categoriesWithDefault,
                        selectedCategory = it.selectedCategory ?: defaultCategory
                    )
                }
                listenToProductChanges() // Ora carica i prodotti
            } catch (e: Exception) {
                Log.e("ViewModel", "Errore caricamento categorie", e)
                updateState {
                    it.copy(
                        isLoading = false, // Assicurati che isLoading sia gestito correttamente
                        errorMessage = "Errore caricamento categorie: ${e.localizedMessage}",
                        categories = listOf("Tutte")
                    )
                }
            }
        }
    }

    fun selectCategory(category: String) {
        val currentSelectedCategory = _screenState.value?.selectedCategory
        if (category != currentSelectedCategory) {
            updateState { it.copy(selectedCategory = category) }
            listenToProductChanges()
        }
    }

    private fun listenToProductChanges() {
        if (_screenState.value?.currentUserRole == null) {
            Log.w("ViewModel", "Tentativo di ascoltare prodotti senza ruolo utente.")
            return // Non ascoltare se il ruolo non è definito
        }
        productsListener?.remove()
        // Non impostare isLoading qui se fetchCurrentUserRoleAndInitializeData o loadCategories lo hanno fatto
        // L'isLoading generale dovrebbe coprire questo.
        // updateState { it.copy(isLoading = true, errorMessage = null) }

        val categoryToFilter = _screenState.value?.selectedCategory
        var query: Query = productsCollection.orderBy("nome", Query.Direction.ASCENDING)

        if (categoryToFilter != null && categoryToFilter != "Tutte") {
            query = query.whereEqualTo("categoria", categoryToFilter)
        }

        productsListener = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("ViewModel", "Listen failed for category '$categoryToFilter'.", e)
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Errore caricamento prodotti: ${e.localizedMessage}",
                        products = emptyList()
                    )
                }
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val productList = snapshots.toObjects<Product>().mapIndexedNotNull { index, product ->
                    val docId = snapshots.documents[index].id
                    product.copy(id = docId)
                }
                updateState {
                    it.copy(
                        isLoading = false,
                        products = productList,
                        errorMessage = null
                    )
                }
            } else {
                updateState { it.copy(isLoading = false, products = emptyList()) }
            }
        }
    }

    fun addProductToCart(product: Product) {
        if (_screenState.value?.currentUserRole == UserRole.USER) { // Controlla il ruolo specifico
            Log.d("ProductCatalogVM", "Aggiunta al carrello: ${product.nome}")
            // Logica aggiunta carrello
        }
    }

    private fun updateState(updateAction: (ProductCatalogScreenState) -> ProductCatalogScreenState) {
        _screenState.value = updateAction(_screenState.value ?: ProductCatalogScreenState())
    }

    override fun onCleared() {
        super.onCleared()
        productsListener?.remove()
        categoriesJob?.cancel()
        userRoleJob?.cancel()
    }
}
