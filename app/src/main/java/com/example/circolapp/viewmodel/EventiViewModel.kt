package com.example.circolapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.circolapp.model.Evento
import com.example.circolapp.repository.EventiRepository

class EventiViewModel : ViewModel() {
    private val repository = EventiRepository()
    val eventi: LiveData<List<Evento>> = repository.getEventi()

    fun addEvento(evento: Evento, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        repository.addEvento(evento, onComplete, onError)
    }
}