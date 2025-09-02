package com.example.circolapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.circolapp.model.Movimento
import com.example.circolapp.repository.HomeRepository
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var currentUid: String? = firebaseAuth.currentUser?.uid

    val saldo: LiveData<Double>
    val movimenti: LiveData<List<Movimento>>

    init {
        if (currentUid != null) {
            saldo = repository.getSaldo(currentUid!!)
            movimenti = repository.getMovimenti(currentUid!!)
        } else {
            saldo = MutableLiveData(0.0)
            movimenti = MutableLiveData(emptyList())
            Log.e("HomeViewModel", "UID utente non disponibile all'inizializzazione.")
        }
    }

    fun onUserLoggedIn() {
        currentUid = firebaseAuth.currentUser?.uid
        if (currentUid != null) {
            
        } else {
            (saldo as? MutableLiveData)?.value = 0.0
            (movimenti as? MutableLiveData)?.value = emptyList()
        }
    }
}