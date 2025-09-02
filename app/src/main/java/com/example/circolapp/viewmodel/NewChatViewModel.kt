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

        val participantsList = listOf(currentUserId, otherUserId).sorted()
        val generatedChatId = participantsList.joinToString(separator = "_")


        viewModelScope.launch {
            try {
                val existingChatQuery = db.collection("chats")
                    .whereArrayContains("participants", currentUserId)
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
                    _navigateToChat.value = NewChatNavigationEvent(foundChatId, otherUserId)
                } else {
                    val newChat = hashMapOf(
                        "participants" to participantsList,
                        "lastMessageText" to "Chat iniziata",
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "unreadCount" to mapOf(currentUserId to 0, otherUserId!! to 0)
                    )

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