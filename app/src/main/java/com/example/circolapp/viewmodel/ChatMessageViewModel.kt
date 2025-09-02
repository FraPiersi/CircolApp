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
    private val otherUserId: String
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val messagesRef = db.collection("chats").document(chatId).collection("messages")
    private val chatDocRef = db.collection("chats").document(chatId)

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val newMessageText = MutableLiveData<String>("")

    private var messagesListener: ListenerRegistration? = null

    init {
        loadMessages()
        markMessagesAsRead()
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
            timestamp = null
        )

        viewModelScope.launch {
            try {
                messagesRef.add(message).await()
                newMessageText.value = ""
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
            "unreadCount.$otherUserId" to FieldValue.increment(1)
        )
        try {
            chatDocRef.update(updates).await()
        } catch (e: Exception) {
            Log.e("ChatMessageVM", "Errore aggiornamento documento chat", e)
        }
    }

    private fun markMessagesAsRead() {
        val updateField = "unreadCount.$currentUserId"
        chatDocRef.update(updateField, 0)
            .addOnSuccessListener { Log.d("ChatMessageVM", "Chat $chatId marcata come letta per $currentUserId") }
            .addOnFailureListener { e -> Log.w("ChatMessageVM", "Errore nel marcare chat $chatId come letta", e) }
    }

    fun getCurrentUserBalance(callback: (Double) -> Unit) {
        db.collection("utenti").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val balance = document.getDouble("saldo") ?: 0.0
                callback(balance)
            }
            .addOnFailureListener { e ->
                Log.e("ChatMessageVM", "Errore nel recuperare il saldo", e)
                callback(0.0)
            }
    }

    fun sendMoneyTransfer(amount: Double, recipientId: String) {
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val senderRef = db.collection("utenti").document(currentUserId)
                    val recipientRef = db.collection("utenti").document(recipientId)

                    val senderSnapshot = transaction.get(senderRef)
                    val recipientSnapshot = transaction.get(recipientRef)

                    val senderBalance = senderSnapshot.getDouble("saldo") ?: 0.0
                    val recipientBalance = recipientSnapshot.getDouble("saldo") ?: 0.0

                    if (senderBalance < amount) {
                        throw Exception("Saldo insufficiente. Saldo disponibile: €${String.format("%.2f", senderBalance)}")
                    }

                    val newSenderBalance = senderBalance - amount
                    val newRecipientBalance = recipientBalance + amount

                    transaction.update(senderRef, "saldo", newSenderBalance)
                    transaction.update(recipientRef, "saldo", newRecipientBalance)

                    val currentTime = com.google.firebase.Timestamp.now()

                    val senderMovimentoData = mapOf(
                        "importo" to -amount,
                        "descrizione" to "Trasferimento a ${recipientSnapshot.getString("displayName") ?: "Utente"}",
                        "data" to currentTime
                    )

                    val recipientMovimentoData = mapOf(
                        "importo" to amount,
                        "descrizione" to "Ricevuto da ${senderSnapshot.getString("displayName") ?: "Utente"}",
                        "data" to currentTime
                    )

                    val senderMovimentoRef = senderRef.collection("movimenti").document()
                    transaction.set(senderMovimentoRef, senderMovimentoData)

                    val recipientMovimentoRef = recipientRef.collection("movimenti").document()
                    transaction.set(recipientMovimentoRef, recipientMovimentoData)

                    val transferMessage = Message(
                        senderId = currentUserId,
                        text = "💰 Trasferimento di €${String.format("%.2f", amount)}",
                        isMoneyTransfer = true,
                        transferAmount = amount,
                        transferStatus = "COMPLETED"
                    )

                    val messageRef = messagesRef.document()
                    transaction.set(messageRef, transferMessage)

                    val chatUpdates = mapOf(
                        "lastMessageText" to "💰 Trasferimento di €${String.format("%.2f", amount)}",
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "unreadCount.$otherUserId" to FieldValue.increment(1)
                    )
                    transaction.update(chatDocRef, chatUpdates)

                }.await()

                _errorMessage.value = "Trasferimento di €${String.format("%.2f", amount)} completato!"

            } catch (e: Exception) {
                Log.e("ChatMessageVM", "Errore nel trasferimento denaro", e)
                _errorMessage.value = "Errore nel trasferimento: ${e.message}"
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}

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