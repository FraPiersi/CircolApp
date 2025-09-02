package com.example.circolapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Feedback(
    val id: String = "",
    val uidUtente: String = "",
    val nomeUtente: String = "",
    val emailUtente: String = "",
    val titolo: String = "",
    val messaggio: String = "",
    val categoria: String = "", // "GENERALE", "BUG", "SUGGERIMENTO", "ALTRO"
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val letto: Boolean = false
) {
    // Costruttore vuoto per Firestore
    constructor() : this("", "", "", "", "", "", "", null, false)
}
