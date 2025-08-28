package com.example.circolapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Evento(
    val id: String = "",
    val nome: String = "",
    val descrizione: String = "",
    val data: Date? = null,
    val luogo: String = "",
    @Transient // Questo campo non viene serializzato in Firestore
    val partecipanti: List<String> = emptyList() // Solo per compatibilità locale
): Parcelable