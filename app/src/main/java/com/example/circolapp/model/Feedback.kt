package com.example.circolapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Feedback(
    @ServerTimestamp
    constructor() : this("", "", "", "", "", "", "", null, false)
}
