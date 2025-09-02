package com.example.circolapp.model

import com.google.firebase.Timestamp

data class ChatConversation(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String? = "Utente",
    val otherUserPhotoUrl: String? = null,
    val lastMessageText: String? = "Nessun messaggio",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Int = 0
)