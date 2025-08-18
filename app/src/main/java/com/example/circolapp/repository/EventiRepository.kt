package com.example.circolapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.circolapp.model.Evento
import com.google.firebase.firestore.FirebaseFirestore

class EventiRepository {
    private val db = FirebaseFirestore.getInstance()
    private val eventiLiveData = MutableLiveData<List<Evento>>()

    fun getEventi(): LiveData<List<Evento>> {
        db.collection("eventi").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Evento::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            eventiLiveData.value = lista
        }
        return eventiLiveData
    }

    fun addEvento(evento: Evento, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        // Crea una mappa senza il campo id
        val eventoMap = hashMapOf<String, Any?>()
        eventoMap["nome"] = evento.nome
        eventoMap["descrizione"] = evento.descrizione
        // aggiungi qui altri campi se presenti nella classe Evento
        db.collection("eventi")
            .add(eventoMap)
            .addOnSuccessListener { onComplete?.invoke() }
            .addOnFailureListener { e -> onError?.invoke(e) }
    }
}