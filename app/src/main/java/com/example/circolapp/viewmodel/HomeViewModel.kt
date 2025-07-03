package com.example.circolapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.circolapp.model.Movimento
import com.example.circolapp.repository.HomeRepository

class HomeViewModel(username: String) : ViewModel() {
    private val repository = HomeRepository()
     val saldo = repository.getSaldo(username)
     val movimenti = repository.getMovimenti(username)

}