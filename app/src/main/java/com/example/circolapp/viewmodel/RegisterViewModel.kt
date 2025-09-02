package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class RegistrationResult {
    data class Success(val user: FirebaseUser) : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
    object Loading : RegistrationResult()
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private                        "photoUrl" to firebaseUser.photoUrl?.toString(), // Sarà null all'inizio
                        "ruolo" to UserRole.USER.name.lowercase(),                        "saldo" to 0.0,    // Saldo iniziale di default
                        "dataCreazione" to FieldValue.serverTimestamp() // Data di creazione
                    )

                    firestore.collection("utenti").document(firebaseUser.uid)
                        .set(userDocumentData)
                        .await()

                    Log.d("RegisterViewModel", "Utente registrato e documento creato in Firestore con ruolo USER.")
                    _registrationStatus.value = RegistrationResult.Success(firebaseUser)
                } else {
                    _registrationStatus.value = RegistrationResult.Error("Creazione utente Firebase fallita.")
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Errore durante la registrazione: ${e.message}", e)
                _registrationStatus.value = RegistrationResult.Error(e.message ?: "Errore sconosciuto durante la registrazione.")
            }
        }
    }
}
