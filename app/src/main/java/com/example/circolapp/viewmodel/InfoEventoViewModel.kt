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

    fun caricaEvento(eventoCaricato: Evento?) {
        if (eventoCaricato == null) {
            Log.e("InfoEventoViewModel", "Tentativo di caricare un evento nullo.")
            _messaggioToast.value = "Errore: Dettagli evento non disponibili."
            return
        }
        _evento.value = eventoCaricato
        // Potresti voler controllare se l'utente corrente è già tra i partecipanti
        // e aggiornare lo stato del pulsante "Partecipa" (es. renderlo "Annulla Partecipazione")
        // Questa logica può essere aggiunta qui o quando l'evento viene caricato.
    }

    fun onPartecipaClicked() {
        val eventoCorrente = _evento.value
        if (eventoCorrente == null || eventoCorrente.id.isBlank()) {
            _messaggioToast.value = "Impossibile partecipare: ID evento non valido."
            return
        }

        // Ottieni l'utente corrente. Sostituisci con il tuo metodo.
        // È preferibile usare l'UID dell'utente se disponibile e consistente.
        val currentUser = auth.currentUser
        val usernamePartecipante = currentUser?.displayName ?: currentUser?.email ?: currentUser?.uid ?: "utente_anonimo"

        if (usernamePartecipante == "utente_anonimo" && currentUser == null) {
            _messaggioToast.value = "Devi essere loggato per partecipare."
            // Potresti voler avviare un flusso di login qui
            return
        }

        _isLoading.value = true // Mostra il loader

        // Riferimento al documento dell'evento specifico in Firestore
        // Assumendo che tu abbia una collection "eventi"
        val eventoRef = db.collection("eventi").document(eventoCorrente.id)

        viewModelScope.launch {
            try {
                // Usa FieldValue.arrayUnion per aggiungere lo username all'array "partecipanti"
                // Questo previene duplicati se l'utente clicca più volte.
                eventoRef.update("partecipanti", FieldValue.arrayUnion(usernamePartecipante))
                    .addOnSuccessListener {
                        Log.d("InfoEventoViewModel", "Utente $usernamePartecipante aggiunto ai partecipanti per l'evento: ${eventoCorrente.id}")
                        _messaggioToast.value = "Partecipazione confermata!"
                        _azionePartecipazioneCompletata.value = true // Segnala il successo
                        _isLoading.value = false // Nascondi il loader

                        // (Opzionale) Ricarica l'evento per aggiornare la UI se necessario,
                        // o aggiorna localmente la lista dei partecipanti nell'oggetto _evento.value
                        // Se l'oggetto Evento in Kotlin ha un campo List<String> partecipanti, aggiornalo:
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
                        _isLoading.value = false // Nascondi il loader
                    }
            } catch (e: Exception) { // Catch per eccezioni impreviste nello scope
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
}