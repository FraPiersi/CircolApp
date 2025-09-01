// InfoEventoViewModel.kt
package com.example.circolapp.viewmodel

import android.util.Log
import androidx.fragment.app.add
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Evento
import com.google.firebase.auth.FirebaseAuth // Per ottenere l'utente corrente (esempio)
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class InfoEventoViewModel : ViewModel() {

    private val _evento = MutableLiveData<Evento?>()
    val evento: LiveData<Evento?> get() = _evento

    private val _messaggioToast = MutableLiveData<String?>()
    val messaggioToast: LiveData<String?> get() = _messaggioToast

    private val _azionePartecipazioneCompletata = MutableLiveData<Boolean>()
    val azionePartecipazioneCompletata: LiveData<Boolean> get() = _azionePartecipazioneCompletata

    private val _isLoading = MutableLiveData<Boolean>(false) // Per mostrare/nascondere un loader
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Istanza di Firestore
    private val db = FirebaseFirestore.getInstance()
    // Istanza di FirebaseAuth (esempio per ottenere username/userId)
    private val auth = FirebaseAuth.getInstance()

    
    private val _partecipanti = MutableLiveData<List<String>>()
    val partecipanti: LiveData<List<String>> get() = _partecipanti

    fun caricaEvento(eventoCaricato: Evento?) {
        if (eventoCaricato == null) {
            Log.e("InfoEventoViewModel", "Tentativo di caricare un evento nullo.")
            _messaggioToast.value = "Errore: Dettagli evento non disponibili."
            return
        }
        _evento.value = eventoCaricato
        // Carica i partecipanti dalla sottocollezione
        caricaPartecipantiDaSottocollezione(eventoCaricato.id)
    }

    private fun caricaPartecipantiDaSottocollezione(eventoId: String) {
        if (eventoId.isBlank()) return

        val eventoRef = db.collection("eventi").document(eventoId)
        eventoRef.collection("partecipanti")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("InfoEventoViewModel", "Errore nel caricamento partecipanti", error)
                    _partecipanti.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaPartecipanti = snapshot.documents.mapNotNull { document ->
                        document.getString("username")
                    }
                    _partecipanti.value = listaPartecipanti

                    // Aggiorna anche l'evento locale per compatibilità
                    _evento.value = _evento.value?.copy(partecipanti = listaPartecipanti)
                } else {
                    _partecipanti.value = emptyList()
                }
            }
    }

    // Metodo per verificare se l'utente corrente sta già partecipando
    fun verificaPartecipazioneUtente(eventoId: String, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false)
            return
        }

        val eventoRef = db.collection("eventi").document(eventoId)
        eventoRef.collection("partecipanti").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun onPartecipaClicked() {
        val eventoCorrente = _evento.value
        if (eventoCorrente == null || eventoCorrente.id.isBlank()) {
            _messaggioToast.value = "Impossibile partecipare: ID evento non valido."
            return
        }

        // Ottieni l'utente corrente (o username/displayName)
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _messaggioToast.value = "Utente non autenticato."
            return
        }

        val usernamePartecipante = currentUser.displayName ?: currentUser.email ?: "UtenteAnonimo"
        _isLoading.value = true // Mostra il loader

        
        val eventoRef = db.collection("eventi").document(eventoCorrente.id)

        viewModelScope.launch {
            try {
                
                val partecipanteData = mapOf(
                    "uid" to currentUser.uid,
                    "username" to usernamePartecipante,
                    "email" to (currentUser.email ?: ""),
                    "dataPartecipazione" to com.google.firebase.Timestamp.now()
                )

                val partecipanteRef = eventoRef.collection("partecipanti").document(currentUser.uid)
                partecipanteRef.set(partecipanteData)
                    .addOnSuccessListener {
                        Log.d("InfoEventoViewModel", "Utente $usernamePartecipante aggiunto ai partecipanti per l'evento: ${eventoCorrente.id}")
                        _messaggioToast.value = "Partecipazione confermata!"
                        _azionePartecipazioneCompletata.value = true
                        _isLoading.value = false

                        // Aggiorna localmente la lista dei partecipanti
                        val updatedPartecipanti = _evento.value?.partecipanti?.toMutableList() ?: mutableListOf()
                        if (!updatedPartecipanti.contains(usernamePartecipante)) {
                            updatedPartecipanti.add(usernamePartecipante)
                        }
                        _evento.value = _evento.value?.copy(partecipanti = updatedPartecipanti)
                    }
                    .addOnFailureListener { e ->
                        Log.e("InfoEventoViewModel", "Errore nell'aggiornare i partecipanti", e)
                        _messaggioToast.value = "Errore durante la partecipazione: ${e.message}"
                        _azionePartecipazioneCompletata.value = false
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("InfoEventoViewModel", "Eccezione imprevista durante la partecipazione", e)
                _messaggioToast.value = "Errore imprevisto. Riprova."
                _azionePartecipazioneCompletata.value = false
                _isLoading.value = false
            }
        }
    }

    fun onMessaggioToastMostrato() {
        _messaggioToast.value = null
    }

    fun onAzionePartecipazioneGestita() {
        _azionePartecipazioneCompletata.value = false
    }

    fun onAnnullaPartecipazioneClicked() {
        val eventoCorrente = _evento.value
        if (eventoCorrente == null || eventoCorrente.id.isBlank()) {
            _messaggioToast.value = "Impossibile annullare: ID evento non valido."
            return
        }

        // Ottieni l'utente corrente
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _messaggioToast.value = "Devi essere loggato per annullare la partecipazione."
            return
        }

        val usernamePartecipante = currentUser.displayName ?: currentUser.email ?: "UtenteAnonimo"
        _isLoading.value = true // Mostra il loader

        
        val eventoRef = db.collection("eventi").document(eventoCorrente.id)

        viewModelScope.launch {
            try {
                // Rimuovi il partecipante dalla sottocollezione "partecipanti"
                val partecipanteRef = eventoRef.collection("partecipanti").document(currentUser.uid)
                partecipanteRef.delete()
                    .addOnSuccessListener {
                        Log.d("InfoEventoViewModel", "Utente $usernamePartecipante rimosso dai partecipanti per l'evento: ${eventoCorrente.id}")
                        _messaggioToast.value = "Partecipazione annullata!"
                        _azionePartecipazioneCompletata.value = true
                        _isLoading.value = false

                        // Aggiorna localmente la lista dei partecipanti
                        val updatedPartecipanti = _evento.value?.partecipanti?.toMutableList() ?: mutableListOf()
                        updatedPartecipanti.remove(usernamePartecipante)
                        _evento.value = _evento.value?.copy(partecipanti = updatedPartecipanti)
                    }
                    .addOnFailureListener { e ->
                        Log.e("InfoEventoViewModel", "Errore nell'annullare la partecipazione", e)
                        _messaggioToast.value = "Errore durante l'annullamento: ${e.message}"
                        _azionePartecipazioneCompletata.value = false
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("InfoEventoViewModel", "Eccezione imprevista durante l'annullamento", e)
                _messaggioToast.value = "Errore imprevisto. Riprova."
                _azionePartecipazioneCompletata.value = false
                _isLoading.value = false
            }
        }
    }
}