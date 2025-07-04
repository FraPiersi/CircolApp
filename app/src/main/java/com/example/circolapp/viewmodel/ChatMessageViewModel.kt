package com.example.circolapp.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.circolapp.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatMessageViewModel(
    private val chatId: String,
    private val currentUserId: String,
    private val otherUserId: String // Necessario per aggiornare unreadCount
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val messagesRef = db.collection("chats").document(chatId).collection("messages")
    private val chatDocRef = db.collection("chats").document(chatId)

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _isLoading = MutableLiveData<Boolean>(true) // Inizia con loading
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val newMessageText = MutableLiveData<String>("") // Per il campo di input

    private var messagesListener: ListenerRegistration? = null

    init {
        loadMessages()
        markMessagesAsRead() // Segna i messaggi come letti quando si entra nella chat
    }

    private fun loadMessages() {
        _isLoading.value = true
        messagesListener?.remove()

        messagesListener = messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatMessageVM", "Listen failed.", e)
                    _errorMessage.value = "Errore nel caricare i messaggi: ${e.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val messageList = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.apply {
                            this.isSentByCurrentUser = this.senderId == currentUserId
                            // this.messageId = doc.id // L'adapter ne ha bisogno per DiffUtil
                        }
                    }
                    _messages.value = messageList
                }
                _isLoading.value = false
            }
    }

    fun sendMessage() {
        val text = newMessageText.value?.trim()
        if (text.isNullOrEmpty() || currentUserId.isBlank()) {
            _errorMessage.value = if (text.isNullOrEmpty()) "Il messaggio non può essere vuoto." else "Utente non valido."
            return
        }

        val message = Message(
            senderId = currentUserId,
            text = text,
            timestamp = null // Sarà impostato da @ServerTimestamp
        )

        viewModelScope.launch {
            try {
                messagesRef.add(message).await()
                newMessageText.value = "" // Pulisci il campo di input
                updateChatDocumentOnNewMessage(text)
            } catch (e: Exception) {
                Log.e("ChatMessageVM", "Errore invio messaggio", e)
                _errorMessage.value = "Errore invio messaggio: ${e.message}"
            }
        }
    }

    private suspend fun updateChatDocumentOnNewMessage(lastText: String) {
        val updates = hashMapOf<String, Any>(
            "lastMessageText" to lastText,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "unreadCount.$otherUserId" to FieldValue.increment(1) // Incrementa per l'altro utente
        )
        try {
            chatDocRef.update(updates).await()
        } catch (e: Exception) {
            Log.e("ChatMessageVM", "Errore aggiornamento documento chat", e)
            // Gestisci l'errore, ma l'invio del messaggio principale è andato a buon fine
        }
    }

    private fun markMessagesAsRead() {
        // Resetta il contatore unread per l'utente corrente in questa chat
        // currentUserId è la chiave che vogliamo azzerare
        val updateField = "unreadCount.$currentUserId"
        chatDocRef.update(updateField, 0)
            .addOnSuccessListener { Log.d("ChatMessageVM", "Chat $chatId marcata come letta per $currentUserId") }
            .addOnFailureListener { e -> Log.w("ChatMessageVM", "Errore nel marcare chat $chatId come letta", e) }
    }


    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}

// Factory per il ViewModel se hai bisogno di passare argomenti al costruttore
@Suppress("UNCHECKED_CAST")
class ChatMessageViewModelFactory(
    private val chatId: String,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatMessageViewModel::class.java)) {
            return ChatMessageViewModel(chatId, currentUserId, otherUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}