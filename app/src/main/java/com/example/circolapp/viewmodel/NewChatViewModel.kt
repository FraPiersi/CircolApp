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

data class NewChatNavigationEvent(val chatId: String,    private                    if (user != null && user.uid != currentUserId) {
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

                    .get()
                    .await()

                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "unreadCount" to mapOf(currentUserId to 0, otherUserId!! to 0)                    )

                    // senza una query, ma richiede di assicurarsi che sia unico (l'ordinamento aiuta).
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