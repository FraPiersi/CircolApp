package com.example.circolapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.circolapp.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EventiRepository {
    private val db = FirebaseFirestore.getInstance()
    private val eventiCollection = db.collection("eventi")

    /**
     * Recupera tutti gli eventi da Firestore in tempo reale
     */
    fun getEventi(): LiveData<List<Evento>> {
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("EventiRepository", "Errore nel caricamento eventi", error)
                    liveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                        } catch (e: Exception) {
                            Log.e("EventiRepository", "Errore nel parsing evento: ${document.id}", e)
                            null
                        }
                    }
                    liveData.value = eventiList
                } else {
                    liveData.value = emptyList()
                }
            }

        return liveData
    }

    /**
     * Aggiunge un nuovo evento a Firestore
     */
    fun addEvento(evento: Evento, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        )

        eventoRef.set(eventoData)
            .addOnSuccessListener {
                Log.d("EventiRepository", "Evento aggiunto con successo: ${eventoRef.id}")
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("EventiRepository", "Errore nell'aggiungere evento", e)
                onError?.invoke(e)
            }
    }

    /**
     * Aggiorna un evento esistente
     */
    fun updateEvento(evento: Evento, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        if (evento.id.isBlank()) {
            onError?.invoke(Exception("ID evento non valido"))
            return
        }

        val eventoData = hashMapOf(
            "nome" to evento.nome,
            "descrizione" to evento.descrizione,
            "luogo" to evento.luogo,
            "data" to evento.data
        )

        eventiCollection.document(evento.id)
            .update(eventoData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("EventiRepository", "Evento aggiornato con successo: ${evento.id}")
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("EventiRepository", "Errore nell'aggiornare evento", e)
                onError?.invoke(e)
            }
    }

    /**
     * Elimina un evento da Firestore
     */
    fun deleteEvento(eventoId: String, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        eventiCollection.document(eventoId)
            .delete()
            .addOnSuccessListener {
                Log.d("EventiRepository", "Evento eliminato con successo: $eventoId")
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("EventiRepository", "Errore nell'eliminare evento", e)
                onError?.invoke(e)
            }
    }
}

