package com.example.circolapp.model

import java.util.Date

// com/example/circolapp/model/Utente.kt
data class User(
    val uid: String = "",
    val username: String = "",
    val nome: String = "",
    val saldo: Double = 0.0,
    @Transient // Questo campo non viene serializzato in Firestore
    val movimenti: List<Movimento> = emptyList(), // Solo per compatibilità locale
    val photoUrl: String? = null,
    val hasTessera: Boolean = false,
    val numeroTessera: String? = null,
    val dataScadenzaTessera: Date? = null,
    val richiestaRinnovoInCorso: Boolean = false
)
