package com.example.circolapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.circolapp.model.Product
import com.example.circolapp.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class ProductCatalogScreenState(
        }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    fun refreshData() {
        // Ricarica i dati se l'utente è già autenticato
            loadProducts()
        } else {
            fetchCurrentUserRoleAndInitializeData()
        }
    }

    private fun updateState(updateAction: (ProductCatalogScreenState) -> ProductCatalogScreenState) {
        _screenState.value = updateAction(_screenState.value ?: ProductCatalogScreenState())
    }

    override fun onCleared() {
        super.onCleared()
        productsListener?.remove()
        userRoleJob?.cancel()
    }
}
