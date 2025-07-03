package com.example.circolapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Evento(
    val id: String = "",
    val nome: String = "",
    val descrizione: String = "",
): Parcelable