package com.example.circolapp.model // o il tuo package models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    @DocumentId // Annotazione per mappare l'ID del documento Firestore
    val id: String? = "",
    val nome: String = "",
    val descrizione: String = "",
    val numeroPezzi: Int = 0,
    val importo: Double = 0.0, // o Long, a seconda di come lo memorizzi
    val imageUrl: String? = null
    // Aggiungi altri campi se necessario
) : Parcelable