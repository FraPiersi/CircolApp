package com.example.circolapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.circolapp.model.Movimento
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import java.util.Date

class HomeRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getSaldo(username: String): LiveData<Double> {
        val liveData = MutableLiveData<Double>()
        db.collection("utenti").document(username).addSnapshotListener { snapshot, _ ->
            val saldo = snapshot?.getDouble("saldo") ?: 0.0
            liveData.value = saldo
        }
        return liveData
    }

    fun getMovimenti(username: String): LiveData<List<Movimento>> {
        val liveData = MutableLiveData<List<Movimento>>()
        db.collection("utenti").document(username).addSnapshotListener { snapshot, _ ->
            val movimentiList = snapshot?.get("movimenti") as? List<Map<String, Any>> ?: emptyList()
            val movimenti = movimentiList.map {
                Movimento(
                    importo = (it["importo"] as? Number)?.toDouble() ?: 0.0,
                    descrizione = it["descrizione"] as? String ?: "",
                    data = (it["data"] as? Timestamp)?.toDate() ?: Date(0L)
                )
            }
            liveData.value = movimenti
        }
        return liveData
    }
}