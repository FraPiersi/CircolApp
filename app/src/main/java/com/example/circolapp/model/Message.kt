package com.example.circolapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
data class Message(

    // Campi per trasferimenti di denaro

   
    @get:Exclude @set:Exclude    constructor() : this("", "", "", null, null, false, null, null)
}