package com.example.circolapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.ChatConversation
import com.example.circolapp.model.User // Assumendo che tu abbia un modello Utente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _conversations = MutableLiveData<List<ChatConversation>>()
    val conversations: LiveData<List<ChatConversation>> get() = _conversations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var conversationsListener: ListenerRegistration? = null

    init {
        loadConversations()
    }

    fun loadConversations() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _errorMessage.value = "Utente non autenticato."
            _isLoading.value = false
            return
        }

        _isLoading.value = true

        conversationsListener?.remove()

        conversationsListener = db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatListViewModel", "Listen failed.", e)
                    _errorMessage.value = "Errore nel caricare le chat: ${e.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val chatConversationList = mutableListOf<ChatConversation>()
                        for (doc in snapshots.documents) {
                            val participants = doc.get("participants") as? List<String>
                            val lastMessageText = doc.getString("lastMessageText")
                            val lastMessageTimestamp = doc.getTimestamp("lastMessageTimestamp")
                            val chatId = doc.id

                            if (participants != null) {
                                val otherUserId = participants.firstOrNull { it != currentUserId }

                                if (otherUserId != null) {
                                    val otherUserDetails = getUserDetails(otherUserId)

                                    chatConversationList.add(
                                        ChatConversation(
                                            chatId = chatId,
                                            otherUserId = otherUserId,
                                            otherUserName = otherUserDetails?.username,
                                            otherUserPhotoUrl = otherUserDetails?.photoUrl,
                                            lastMessageText = lastMessageText,
                                            lastMessageTimestamp = lastMessageTimestamp
                                        )
                                    )
                                }
                            }
                        }
                        _conversations.value = chatConversationList
                    } catch (ex: Exception) {
                        Log.e("ChatListViewModel", "Errore nella trasformazione delle chat", ex)
                        _errorMessage.value = "Errore durante l'elaborazione delle chat."
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
    }

    private suspend fun getUserDetails(userId: String): User? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("ChatListViewModel", "Errore nel recuperare i dettagli utente $userId", e)
            null
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                db.collection("chats").document(chatId).delete().await()
                Log.d("ChatListViewModel", "Chat con ID $chatId eliminata con successo.")
            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Errore durante l'eliminazione della chat $chatId", e)
                _errorMessage.value = "Impossibile eliminare la chat: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        conversationsListener?.remove()
    }
}