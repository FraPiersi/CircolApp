package com.example.circolapp.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Ordine(
    val uidUtente: String = "",
    val nomeProdotto: String = "",
    val prodottoId: String = "",
    val richiesteAggiuntive: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null,
    var stato: String = "INVIATO"
) {
    constructor() : this("", "", "", null, null, "INVIATO")
}