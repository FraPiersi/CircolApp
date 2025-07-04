package com.example.circolapp.viewmodel // o il tuo package viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.MainActivity // Assumendo che sia la tua schermata principale
import com.example.circolapp.repository.UserRepository // Creeremo questo
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

// Eventi per la navigazione o per mostrare messaggi una tantum
sealed class AuthEvent {
    data class NavigateToMain(val user: FirebaseUser) : AuthEvent()
    data class ShowErrorToast(val message: String) : AuthEvent()
    object StartFirebaseUIFlow : AuthEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository: UserRepository = UserRepository() // Istanzia il tuo repository

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData per eventi una tantum
    private val _authEvent = MutableLiveData<AuthEvent?>()
    val authEvent: LiveData<AuthEvent?> get() = _authEvent

    // LiveData per l'utente corrente (opzionale, ma utile per osservare lo stato di login)
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    // In AuthViewModel.kt
    init {
        val user = auth.currentUser
        _currentUser.value = user
        if (user != null) {
            Log.d("AuthViewModel", "Utente già loggato: ${user.uid}. Navigazione a Main...")
            viewModelScope.launch {
                try {
                    userRepository.addOrUpdateUserInFirestore(user) // Aggiorna dati utente (es. lastSeen)
                    _authEvent.value = AuthEvent.NavigateToMain(user)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Errore sync utente (già loggato): ${e.message}", e)
                    // Se fallisce l'aggiornamento, cosa fare? Navigare comunque? Mostrare errore?
                    // Per ora, navighiamo comunque e mostriamo un errore se fallisce il sync.
                    _authEvent.value = AuthEvent.NavigateToMain(user) // Naviga anche se il sync ha problemi
                    _authEvent.value = AuthEvent.ShowErrorToast("Problema nel sincronizzare i dati utente: ${e.message}")
                }
            }
        } else {
            Log.d("AuthViewModel", "Nessun utente loggato. Avvio FirebaseUI Flow...")
            _authEvent.value = AuthEvent.StartFirebaseUIFlow
        }
    }

    fun createSignInIntent(): Intent {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
            // Aggiungi altri provider se necessario
        )
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            // .setLogo(R.drawable.my_logo) // Imposta un logo se vuoi
            // .setTheme(R.style.MyAuthTheme) // Imposta un tema se vuoi
            .build()
    }

    fun handleSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        _isLoading.value = true

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                Log.d("AuthViewModel", "Login/Registrazione FirebaseUI riuscita. Utente: ${firebaseUser.uid}")
                viewModelScope.launch {
                    try {
                        userRepository.addOrUpdateUserInFirestore(firebaseUser)
                        _currentUser.value = firebaseUser
                        _authEvent.value = AuthEvent.NavigateToMain(firebaseUser)
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Errore sync utente dopo FirebaseUI: ${e.message}", e)
                        _authEvent.value = AuthEvent.ShowErrorToast("Errore nella sincronizzazione dei dati utente: ${e.message}")
                    } finally {
                        _isLoading.value = false
                    }
                }
            } else {
                Log.e("AuthViewModel", "FirebaseUI OK, ma currentUser è null.")
                _authEvent.value = AuthEvent.ShowErrorToast("Errore: utente non trovato dopo il login.")
                _isLoading.value = false
            }
        } else {
            val errorMsgFromResponse = response?.error?.localizedMessage
            val errorMsg = if (errorMsgFromResponse != null) {
                "Accesso fallito: $errorMsgFromResponse"
            } else if (response == null) {
                "Accesso annullato dall'utente." // FirebaseUI AuthCancelledException
            } else {
                "Accesso fallito o annullato. Codice errore: ${response.error?.errorCode}"
            }
            Log.w("AuthViewModel", "FirebaseUI fallito/annullato. Response: $response, Error: $errorMsg")
            _authEvent.value = AuthEvent.ShowErrorToast(errorMsg)
            _isLoading.value = false
        }
    }

    fun onAuthEventHandled() {
        _authEvent.value = null // Resetta l'evento dopo che è stato gestito dalla View
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            AuthUI.getInstance().signOut(getApplication()) // Per FirebaseUI
            _currentUser.value = null
            // Potresti voler navigare alla schermata di login qui o emettere un evento
            Log.d("AuthViewModel", "Utente disconnesso.")
        }
    }
}