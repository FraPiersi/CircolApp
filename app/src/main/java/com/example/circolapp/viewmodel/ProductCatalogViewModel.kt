package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Product
import com.example.circolapp.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Stato della UI per il catalogo prodotti
data class ProductCatalogScreenState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val currentUserRole: UserRole? = null
)

class ProductCatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = db.collection("prodotti")
    private var productsListener: ListenerRegistration? = null
    private var userRoleJob: Job? = null

    private val _screenState = MutableLiveData(ProductCatalogScreenState())
    val screenState: LiveData<ProductCatalogScreenState> get() = _screenState

    init {
        fetchCurrentUserRoleAndInitializeData()
    }

    private fun fetchCurrentUserRoleAndInitializeData() {
        if (userRoleJob?.isActive == true) return
        userRoleJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                Log.w("ViewModel", "Nessun utente Firebase loggato.")
                updateState { it.copy(isLoading = false, currentUserRole = null, errorMessage = "Utente non autenticato.") }
                return@launch
            }

            try {
                val userDoc = db.collection("utenti").document(firebaseUser.uid).get().await()
                val roleString = userDoc.getString("ruolo")
                val role = roleString?.let { UserRole.valueOf(it.uppercase()) }

                if (role != null) {
                    Log.d("ViewModel", "Ruolo utente recuperato: $role")
                    updateState { it.copy(currentUserRole = role) }
                    loadProducts()
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

    private fun loadProducts() {
        if (_screenState.value?.currentUserRole == null) {
            Log.w("ViewModel", "Tentativo di caricare prodotti senza ruolo utente.")
            updateState { it.copy(isLoading = false, errorMessage = "Impossibile caricare dati senza ruolo utente.") }
            return
        }

        listenToProductChanges()
    }

    private fun listenToProductChanges() {
        if (_screenState.value?.currentUserRole == null) {
            Log.w("ViewModel", "Tentativo di ascoltare prodotti senza ruolo utente.")
            return
        }

        productsListener?.remove()

        val query: Query = productsCollection.orderBy("nome", Query.Direction.ASCENDING)

        productsListener = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("ViewModel", "Listen failed.", e)
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
        if (_screenState.value?.currentUserRole == UserRole.USER) {
            Log.d("ProductCatalogVM", "Aggiunta al carrello: ${product.nome}")
            // Logica aggiunta carrello
        }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    fun refreshData() {
        // Ricarica i dati se l'utente è già autenticato
        val currentRole = _screenState.value?.currentUserRole

        if (currentRole != null) {
            // Se il ruolo è già disponibile, ricarica solo i prodotti
            loadProducts()
        } else {
            // Se non c'è il ruolo, ricarica tutto dall'inizio
            fetchCurrentUserRoleAndInitializeData()
        }
    }

    private fun updateState(updateAction: (ProductCatalogScreenState) -> ProductCatalogScreenState) {
        _screenState.value = updateAction(_screenState.value ?: ProductCatalogScreenState())
    }

    override fun onCleared() {
        super.onCleared()
        productsListener?.remove()
        userRoleJob?.cancel()
    }
}
