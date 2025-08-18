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

    // Nuovo metodo per ottenere il saldo dell'utente corrente
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

    // Nuovo metodo per inviare trasferimento di denaro
    fun sendMoneyTransfer(amount: Double, recipientId: String) {
        viewModelScope.launch {
            try {
                // Esegui la transazione per trasferire il denaro
                db.runTransaction { transaction ->
                    val senderRef = db.collection("utenti").document(currentUserId)
                    val recipientRef = db.collection("utenti").document(recipientId)

                    val senderSnapshot = transaction.get(senderRef)
                    val recipientSnapshot = transaction.get(recipientRef)

                    val senderBalance = senderSnapshot.getDouble("saldo") ?: 0.0
                    val recipientBalance = recipientSnapshot.getDouble("saldo") ?: 0.0

                    // Verifica se il mittente ha saldo sufficiente
                    if (senderBalance < amount) {
                        throw Exception("Saldo insufficiente. Saldo disponibile: €${String.format("%.2f", senderBalance)}")
                    }

                    // Calcola i nuovi saldi
                    val newSenderBalance = senderBalance - amount
                    val newRecipientBalance = recipientBalance + amount

                    // Aggiorna i saldi
                    transaction.update(senderRef, "saldo", newSenderBalance)
                    transaction.update(recipientRef, "saldo", newRecipientBalance)

                    // Crea i movimenti per entrambi gli utenti
                    val currentTime = com.google.firebase.Timestamp.now()

                    // Movimento per il mittente (negativo)
                    val senderMovimento = mapOf(
                        "importo" to -amount,
                        "descrizione" to "Trasferimento a ${recipientSnapshot.getString("displayName") ?: "Utente"}",
                        "data" to currentTime
                    )

                    // Movimento per il destinatario (positivo)
                    val recipientMovimento = mapOf(
                        "importo" to amount,
                        "descrizione" to "Ricevuto da ${senderSnapshot.getString("displayName") ?: "Utente"}",
                        "data" to currentTime
                    )

                    // Aggiorna i movimenti del mittente
                    val senderMovimenti = (senderSnapshot.get("movimenti") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                    senderMovimenti.add(senderMovimento)
                    transaction.update(senderRef, "movimenti", senderMovimenti)

                    // Aggiorna i movimenti del destinatario
                    val recipientMovimenti = (recipientSnapshot.get("movimenti") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                    recipientMovimenti.add(recipientMovimento)
                    transaction.update(recipientRef, "movimenti", recipientMovimenti)

                    // Crea il messaggio di trasferimento denaro nella chat
                    val transferMessage = Message(
                        senderId = currentUserId,
                        text = "💰 Trasferimento di €${String.format("%.2f", amount)}",
                        isMoneyTransfer = true,
                        transferAmount = amount,
                        transferStatus = "COMPLETED"
                    )

                    // Aggiungi il messaggio alla chat
                    val messageRef = messagesRef.document()
                    transaction.set(messageRef, transferMessage)

                    // Aggiorna il documento della chat
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