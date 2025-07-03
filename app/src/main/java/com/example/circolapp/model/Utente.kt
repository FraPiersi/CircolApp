package com.example.circolapp.model

// com/example/circolapp/model/Utente.kt
data class Utente(
    val username: String = "",
    val nome: String = "",
    val saldo: Double = 0.0,
    val movimenti: Map<String, Movimento> = emptyMap()
)
