package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val userRole: UserRole, val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = Firebase.firestore

    private val _authResult = MutableLiveData<AuthResult>(AuthResult.Idle)
    val authResult: LiveData<AuthResult> get() = _authResult

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _authResult.value = AuthResult.Loading
            fetchUserRoleAndProceed(currentUser)
        } else {
            _authResult.value = AuthResult.Idle
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authResult.value = AuthResult.Error("Inserisci email e password.")
            return
        }
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val authTask = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                authTask.user?.let {
                        user ->
                    fetchUserRoleAndProceed(user)
                } ?: run {
                    _authResult.value = AuthResult.Error("Errore: utente non trovato dopo il login.")
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Login fallito", e)
                _authResult.value = AuthResult.Error("Autenticazione fallita: ${e.message}")
            }
        }
    }

    private fun fetchUserRoleAndProceed(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            try {
                val userDocRef = firestore.collection("utenti").document(firebaseUser.uid)
                val documentSnapshot = userDocRef.get().await()

                if (documentSnapshot.exists()) {
                    val ruoloString = documentSnapshot.getString("ruolo")
                    Log.d("AuthViewModel", "Ruolo utente '${firebaseUser.uid}': $ruoloString")
                    val userRole = when (ruoloString?.lowercase()) {
                        UserRole.ADMIN.name.lowercase() -> UserRole.ADMIN
                        UserRole.USER.name.lowercase() -> UserRole.USER
                        else -> {
                            Log.w("AuthViewModel", "Ruolo sconosciuto '$ruoloString' per UID: ${firebaseUser.uid}. Aggiornamento a USER se possibile o default a USER.")
                            UserRole.USER
                        }
                    }
                    _authResult.value = AuthResult.Success(userRole, firebaseUser)
                } else {
                    Log.w("AuthViewModel", "Documento utente non trovato per UID: ${firebaseUser.uid}. Creazione con ruolo USER.")
                    createFirestoreUserDocument(firebaseUser, UserRole.USER)
                    _authResult.value = AuthResult.Success(UserRole.USER, firebaseUser)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Errore nel recuperare/creare il ruolo utente da Firestore.", e)
                _authResult.value = AuthResult.Error("Errore nel recupero dati utente: ${e.message}")
            }
        }
    }

    private suspend fun createFirestoreUserDocument(firebaseUser: FirebaseUser, defaultRole: UserRole) {
        val userDocRef = firestore.collection("utenti").document(firebaseUser.uid)
        try {
            val userData = hashMapOf(
                "uid" to firebaseUser.uid,
                "email" to firebaseUser.email,
                "displayName" to firebaseUser.displayName,
                "photoUrl" to firebaseUser.photoUrl?.toString(),
                "ruolo" to defaultRole.name.lowercase(),
                "saldo" to 0.0,
                "dataCreazione" to FieldValue.serverTimestamp()
            )
            userDocRef.set(userData).await()
            Log.d("AuthViewModel", "Documento utente creato in Firestore per ${firebaseUser.uid} con ruolo ${defaultRole.name}")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Errore durante la creazione del documento utente in Firestore (chiamata da AuthViewModel)", e)
            throw e
        }
    }

    fun resetAuthResult() {
        _authResult.value = AuthResult.Idle
    }
}

