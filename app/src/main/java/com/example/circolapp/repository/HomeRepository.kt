package com.example.circolapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.circolapp.model.Movimento // Assicurati che il percorso sia corretto
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class HomeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val utentiCollection = db.collection("utenti") // Riferimento alla collection

    /**
     * Recupera il saldo dell'utente in tempo reale da Firestore usando l'UID.
     * @param userUid L'UID dell'utente autenticato.
     * @return LiveData<Double> che emette il saldo aggiornato.
     */
    fun getSaldo(userUid: String): LiveData<Double> {
        val liveData = MutableLiveData<Double>()
        if (userUid.isBlank()) {
            Log.w("HomeRepository", "User UID è vuoto, impossibile recuperare il saldo.")
            liveData.value = 0.0 // O gestisci l'errore diversamente
            return liveData
        }

        // L'ID del documento è l'UID dell'utente
        utentiCollection.document(userUid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("HomeRepository", "Errore nell'ascoltare il saldo per UID: $userUid", error)
                liveData.value = 0.0 // O gestisci l'errore diversamente
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val saldo = snapshot.getDouble("saldo") ?: 0.0
                liveData.value = saldo
            } else {
                Log.d("HomeRepository", "Documento non trovato per il saldo UID: $userUid (potrebbe essere un nuovo utente senza ancora il campo saldo)")
                liveData.value = 0.0 // Se il documento non esiste o non ha il campo saldo
            }
        }
        return liveData
    }

    /**
     * Recupera la lista dei movimenti dell'utente in tempo reale da Firestore usando l'UID.
     * @param userUid L'UID dell'utente autenticato.
     * @return LiveData<List<Movimento>> che emette la lista aggiornata dei movimenti.
     */
    fun getMovimenti(userUid: String): LiveData<List<Movimento>> {
        val liveData = MutableLiveData<List<Movimento>>()
        if (userUid.isBlank()) {
            Log.w("HomeRepository", "User UID è vuoto, impossibile recuperare i movimenti.")
            liveData.value = emptyList() // O gestisci l'errore diversamente
            return liveData
        }

        // L'ID del documento è l'UID dell'utente
        utentiCollection.document(userUid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("HomeRepository", "Errore nell'ascoltare i movimenti per UID: $userUid", error)
                liveData.value = emptyList() // O gestisci l'errore diversamente
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Assumendo che 'movimenti' sia un array di mappe nel documento utente
                val movimentiList = snapshot.get("movimenti") as? List<Map<String, Any>> ?: emptyList()
                val movimenti = movimentiList.mapNotNull {
                    try {
                        Movimento(
                            importo = (it["importo"] as? Number)?.toDouble() ?: 0.0,
                            descrizione = it["descrizione"] as? String ?: "",
                            data = (it["data"] as? Timestamp)?.toDate() ?: Date(0L) // Default a Epoch se data non valida
                        )
                    } catch (e: Exception) {
                        Log.e("HomeRepository", "Errore nel parsing di un movimento per UID: $userUid", e)
                        null // Salta il movimento se c'è un errore di parsing
                    }
                }.sortedByDescending { it.data } // Ordina i movimenti dal più recente al meno recente
                liveData.value = movimenti
            } else {
                Log.d("HomeRepository", "Documento non trovato per i movimenti UID: $userUid")
                liveData.value = emptyList() // Se il documento non esiste
            }
        }
        return liveData
    }
}