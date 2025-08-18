package com.example.circolapp.model // o il tuo package models

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Ordine(
    val uidUtente: String = "",
    val nomeProdotto: String = "",
    val prodottoId: String = "", // Utile per riferimenti futuri
    val richiesteAggiuntive: String? = null,
    @ServerTimestamp // Per registrare automaticamente il timestamp del server
    val timestamp: Date? = null,
    var stato: String = "INVIATO" // Es. INVIATO, IN PREPARAZIONE, PRONTO, CONSEGNATO
) {
    // Costruttore vuoto necessario per Firestore
    constructor() : this("", "", "", null, null, "INVIATO")
}