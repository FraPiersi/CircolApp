package com.example.circolapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@com.google.firebase.firestore.IgnoreExtraProperties
data class Product(
    val id: String = "",
    val nome: String = "",
    val descrizione: String = "",
    val numeroPezzi: Int = 0,
    val importo: Double = 0.0,
    val imageUrl: String? = null,
    val ordinabile: Boolean = true
) : Parcelable {
    constructor() : this(
        id = "",
        nome = "",
        descrizione = "",
        numeroPezzi = 0,
        importo = 0.0,
        imageUrl = null,
        ordinabile = true
    )
}