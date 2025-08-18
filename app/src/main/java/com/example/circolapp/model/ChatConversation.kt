package com.example.circolapp.model // o il tuo package models

import com.google.firebase.Timestamp

data class ChatConversation(
    val chatId: String = "",
    val otherUserId: String = "", // UID dell'altro utente nella chat 1-a-1
    val otherUserName: String? = "Utente", // Nome visualizzato dell'altro utente
    val otherUserPhotoUrl: String? = null, // URL foto profilo dell'altro utente
    val lastMessageText: String? = "Nessun messaggio",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Int = 0 // Potremmo aggiungerlo in seguito
)