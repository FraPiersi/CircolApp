package com.example.circolapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val imageUrl: String? = null,

    val isMoneyTransfer: Boolean = false,
    val transferAmount: Double? = null,
    val transferStatus: String? = null,

    @get:Exclude @set:Exclude var isSentByCurrentUser: Boolean = false
) {
    constructor() : this("", "", "", null, null, false, null, null)
}