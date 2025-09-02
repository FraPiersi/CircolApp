package com.example.circolapp.model

import java.util.Date


data class User(
    val photoUrl: String? = null,
    val hasTessera: Boolean = false,
    val numeroTessera: String? = null,
    val dataScadenzaTessera: Date? = null,
    val richiestaRinnovoInCorso: Boolean = false
)
