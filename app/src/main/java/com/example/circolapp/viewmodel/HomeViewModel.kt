package com.example.circolapp.viewmodel
// Esempio in un HomeViewModel.kt
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.circolapp.model.Movimentoimport com.example.circolapp.repository.HomeRepositoryimport com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private            // o emettere un evento per richiedere il login.
            saldo = MutableLiveData(0.0)            movimenti = MutableLiveData(emptyList())            Log.e("HomeViewModel", "UID utente non disponibile all'inizializzazione.")
            // Considera di esporre uno stato di errore o di reindirizzare al login
        }
    }

    // potresti aver bisogno di un meccanismo per ri-ottenere l'UID
    // e ri-sottoscrivere i LiveData.
    fun onUserLoggedIn() {
        currentUid = firebaseAuth.currentUser?.uid
        if (currentUid != null) {
            // (saldo as MutableLiveData).value = repository.getSaldo(currentUid!!).value // Non puoi fare così direttamente
            // Dovrai ri-assegnare o usare switchMap se vuoi che i LiveData siano dinamici
            // il LiveData venga inizializzato con UID validi.
            // Transformations.switchMap per cambiare la sorgente del LiveData.
        } else {
            (saldo as? MutableLiveData)?.value = 0.0
            (movimenti as? MutableLiveData)?.value = emptyList()
        }
    }
}