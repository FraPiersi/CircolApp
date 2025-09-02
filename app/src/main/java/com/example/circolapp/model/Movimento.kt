package com.example.circolapp.model

import java.util.Date

data class Movimento(
    val importo: Double = 0.0,
    val descrizione: String = "",
    val data: Date = Date()
)
