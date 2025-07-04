package com.example.circolapp.model

// com/example/circolapp/model/Utente.kt
data class User(
    val uid: String = "",
    val username: String = "",
    val nome: String = "",
    val saldo: Double = 0.0,
    val movimenti: List<Movimento> = emptyList(),
    val photoUrl: String? = null
)
