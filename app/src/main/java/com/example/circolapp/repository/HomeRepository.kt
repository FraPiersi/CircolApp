package com.example.circolapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.circolapp.model.Movimento
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class HomeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val utentiCollection = db.collection("utenti")

    /**
     * Recupera il saldo dell'utente in tempo reale da Firestore usando l'UID.
     * @param userUid L'UID dell'utente autenticato.
     * @return LiveData<Double> che emette il saldo aggiornato.
     */
    fun getSaldo(userUid: String): LiveData<Double> {
        val liveData = MutableLiveData<Double>()
        if (userUid.isBlank()) {
            Log.w("HomeRepository", "User UID è vuoto, impossibile recuperare il saldo.")
            liveData.value = 0.0
            return liveData
        }

        utentiCollection.document(userUid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("HomeRepository", "Errore nell'ascoltare il saldo per UID: $userUid", error)
                liveData.value = 0.0
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val saldo = snapshot.getDouble("saldo") ?: 0.0
                liveData.value = saldo
            } else {
                Log.d("HomeRepository", "Documento non trovato per il saldo UID: $userUid (potrebbe essere un nuovo utente senza ancora il campo saldo)")
                liveData.value = 0.0
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
            liveData.value = emptyList()
            return liveData
        }

        utentiCollection.document(userUid).collection("movimenti")
            .orderBy("data", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("HomeRepository", "Errore nell'ascoltare i movimenti per UID: $userUid", error)
                    liveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val movimenti = snapshot.documents.mapNotNull { document ->
                        try {
                            Movimento(
                                importo = document.getDouble("importo") ?: 0.0,
                                descrizione = document.getString("descrizione") ?: "",
                                data = document.getTimestamp("data")?.toDate() ?: Date(0L)
                            )
                        } catch (e: Exception) {
                            Log.e("HomeRepository", "Errore nel parsing di un movimento per UID: $userUid", e)
                            null
                        }
                    }
                    liveData.value = movimenti
                } else {
                    Log.d("HomeRepository", "Nessun movimento trovato per UID: $userUid")
                    liveData.value = emptyList()
                }
            }
        return liveData
    }

    /**
     * Aggiunge un movimento alla sottocollezione movimenti dell'utente
     */
    fun aggiungiMovimento(userUid: String, movimento: Movimento, callback: (Boolean) -> Unit) {
        if (userUid.isBlank()) {
            Log.w("HomeRepository", "User UID è vuoto, impossibile aggiungere movimento.")
            callback(false)
            return
        }

        val movimentoData = mapOf(
            "importo" to movimento.importo,
            "descrizione" to movimento.descrizione,
            "data" to com.google.firebase.Timestamp(movimento.data)
        )

        utentiCollection.document(userUid).collection("movimenti")
            .add(movimentoData)
            .addOnSuccessListener {
                Log.d("HomeRepository", "Movimento aggiunto con successo per UID: $userUid")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("HomeRepository", "Errore nell'aggiungere movimento per UID: $userUid", e)
                callback(false)
            }
    }
}