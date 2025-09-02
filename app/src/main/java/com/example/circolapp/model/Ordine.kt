package com.example.circolapp.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Ordine(
) {
    // Costruttore vuoto necessario per Firestore
    constructor() : this("", "", "", null, null, "INVIATO")
}