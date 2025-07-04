package com.example.circolapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp // Importa questo

data class Message(
    val messageId: String = "", // ID del documento messaggio
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp // Firestore popolerà questo con il timestamp del server alla scrittura
    val timestamp: Timestamp? = null,
    val imageUrl: String? = null, // Per futuri messaggi con immagini

    // Campo transitorio per la UI, per sapere se il messaggio è dell'utente corrente
    @get:Exclude @set:Exclude var isSentByCurrentUser: Boolean = false
) {
    // Costruttore vuoto necessario per la deserializzazione da Firestore
    constructor() : this("", "", "", null, null)
}