package com.example.circolapp.viewmodel
// Esempio in un HomeViewModel.kt
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.circolapp.model.Movimento // Assicurati che il percorso sia corretto
import com.example.circolapp.repository.HomeRepository // Assicurati che il percorso sia corretto
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var currentUid: String? = firebaseAuth.currentUser?.uid

    val saldo: LiveData<Double>
    val movimenti: LiveData<List<Movimento>>

    init {
        if (currentUid != null) {
            saldo = repository.getSaldo(currentUid!!)
            movimenti = repository.getMovimenti(currentUid!!)
        } else {
            // Gestisci il caso in cui l'UID non è disponibile (utente non loggato)
            // Potresti inizializzare con LiveData vuoti o valori di default
            // o emettere un evento per richiedere il login.
            saldo = MutableLiveData(0.0) // Esempio di valore di default
            movimenti = MutableLiveData(emptyList()) // Esempio di valore di default
            Log.e("HomeViewModel", "UID utente non disponibile all'inizializzazione.")
            // Considera di esporre uno stato di errore o di reindirizzare al login
        }
    }

    // Se l'utente può fare logout e login mentre l'app è aperta,
    // potresti aver bisogno di un meccanismo per ri-ottenere l'UID
    // e ri-sottoscrivere i LiveData.
    // Ad esempio, una funzione chiamata dopo un login riuscito.
    fun onUserLoggedIn() {
        currentUid = firebaseAuth.currentUser?.uid
        if (currentUid != null) {
            // (saldo as MutableLiveData).value = repository.getSaldo(currentUid!!).value // Non puoi fare così direttamente
            // Dovrai ri-assegnare o usare switchMap se vuoi che i LiveData siano dinamici
            // Per semplicità, qui assumiamo che il ViewModel venga ricreato o che
            // il LiveData venga inizializzato con UID validi.
            // Se il ViewModel persiste attraverso i login/logout, considera l'uso di
            // Transformations.switchMap per cambiare la sorgente del LiveData.
        } else {
            (saldo as? MutableLiveData)?.value = 0.0
            (movimenti as? MutableLiveData)?.value = emptyList()
        }
    }
}