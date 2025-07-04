package com.example.circolapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NewChatNavigationEvent(val chatId: String, val otherUserId: String)

class NewChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Evento per la navigazione alla schermata dei messaggi
    private val _navigateToChat = MutableLiveData<NewChatNavigationEvent?>()
    val navigateToChat: LiveData<NewChatNavigationEvent?> get() = _navigateToChat


    init {
        loadAllUsers()
    }

    private fun loadAllUsers() {
        if (currentUserId == null) {
            _errorMessage.value = "Utente non autenticato."
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val usersList = mutableListOf<User>()
                val result = db.collection("utenti").get().await()
                for (document in result.documents) {
                    val user = document.toObject(User::class.java)
                    // Escludi l'utente corrente dalla lista
                    if (user != null && user.uid != currentUserId) {
                        usersList.add(user)
                    }
                }
                _users.value = usersList
            } catch (e: Exception) {
                Log.e("NewChatViewModel", "Errore nel caricare gli utenti", e)
                _errorMessage.value = "Impossibile caricare gli utenti: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startChatWithUser(selectedUser: User) {
        if (currentUserId == null) {
            _errorMessage.value = "Utente corrente non valido."
            return
        }
        _isLoading.value = true

        val otherUserId = selectedUser.uid

        // Crea un ID chat consistente (ordinando gli UID)
        val participantsList = listOf(currentUserId, otherUserId).sorted()
        val generatedChatId = participantsList.joinToString(separator = "_")


        viewModelScope.launch {
            try {
                // 1. Controlla se una chat tra questi due utenti esiste già
                val existingChatQuery = db.collection("chats")
                    .whereArrayContains("participants", currentUserId) // otteniamo le chat dell'utente corrente
                    .get()
                    .await()

                var foundChatId: String? = null
                for (doc in existingChatQuery.documents) {
                    val participants = doc.get("participants") as? List<String>
                    if (participants != null && participants.containsAll(listOf(currentUserId, otherUserId)) && participants.size == 2) {
                        foundChatId = doc.id
                        break
                    }
                }

                if (foundChatId != null) {
                    // Chat esistente trovata, naviga a quella
                    _navigateToChat.value = NewChatNavigationEvent(foundChatId, otherUserId)
                } else {
                    // 2. Chat non esiste, creane una nuova
                    val newChat = hashMapOf(
                        "participants" to participantsList,
                        "lastMessageText" to "Chat iniziata", // Messaggio iniziale opzionale
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "unreadCount" to mapOf(currentUserId to 0, otherUserId!! to 0) // Inizializza contatori non letti
                    )

                    // Usa l'ID generato o lascia che Firestore ne generi uno
                    // Usare un ID predeterminato (generatedChatId) può essere utile se vuoi accedervi direttamente
                    // senza una query, ma richiede di assicurarsi che sia unico (l'ordinamento aiuta).
                    // Se usi un ID generato da Firestore: db.collection("chats").add(newChat).await().id
                    db.collection("chats").document(generatedChatId).set(newChat).await()
                    _navigateToChat.value = NewChatNavigationEvent(generatedChatId, otherUserId)
                }

            } catch (e: Exception) {
                Log.e("NewChatViewModel", "Errore nell'avviare la chat", e)
                _errorMessage.value = "Impossibile avviare la chat: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onChatNavigated() {
        _navigateToChat.value = null
    }
}