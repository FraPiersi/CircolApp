package com.example.circolapp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.FirebaseApp

/**
 * Custom test runner per gestire l'inizializzazione Firebase nei test
 */
class FirebaseTestRunner : AndroidJUnitRunner() {
    
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, FirebaseTestApplication::class.java.name, context)
    }
}

/**
 * Application personalizzata per i test
 */
class FirebaseTestApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inizializza Firebase solo se non è già inizializzato
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            // Log dell'errore ma non fallire l'applicazione
            android.util.Log.w("FirebaseTestApplication", "Errore nell'inizializzazione Firebase: ${e.message}")
        }
    }
}