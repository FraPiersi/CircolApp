package com.example.circolapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * Configurazione per test Firebase
 * Gestisce l'inizializzazione di Firebase per i test instrumented
 */
object FirebaseTestConfig {
    
    private var isInitialized = false
    
    /**
     * Inizializza Firebase per i test
     * Questo metodo dovrebbe essere chiamato nel setUp di ogni test class
     */
    fun initializeFirebaseForTesting() {
        if (isInitialized) return
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        try {
            // Verifica se Firebase è già inizializzato
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Inizializza Firebase con configurazione di default
                FirebaseApp.initializeApp(context)
            }
            
            // Configura Firestore per i test
            val firestore = FirebaseFirestore.getInstance()
            
            // Configura settings per evitare conflitti di rete
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // Disabilita la persistenza locale per i test
                .build()
            
            firestore.firestoreSettings = settings
            
            isInitialized = true
            
        } catch (e: Exception) {
            // Log dell'errore ma non fallire il test per questo
            android.util.Log.w("FirebaseTestConfig", "Errore nell'inizializzazione Firebase per test: ${e.message}")
        }
    }
    
    /**
     * Pulisce lo stato di Firebase Auth per i test
     */
    fun clearFirebaseAuth() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            android.util.Log.w("FirebaseTestConfig", "Errore nel logout Firebase: ${e.message}")
        }
    }
    
    /**
     * Verifica se Firebase è disponibile per i test
     */
    fun isFirebaseAvailable(): Boolean {
        return try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}