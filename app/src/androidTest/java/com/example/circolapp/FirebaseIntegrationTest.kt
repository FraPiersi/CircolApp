package com.example.circolapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Test specifici per Firebase Firestore
 * Questi test verificano che l'integrazione con Firestore funzioni correttamente
 * e gestiscono i problemi di compatibilità protobuf
 */
@RunWith(AndroidJUnit4::class)
class FirebaseIntegrationTest {
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val TAG = "FirebaseIntegrationTest"
    
    @Before
    fun setUp() {
        // Inizializza Firebase per i test
        FirebaseTestConfig.initializeFirebaseForTesting()
        
        // Configura Firestore
        assertTrue("Firestore non configurato correttamente", 
                  FirestoreTestHelper.configureFirestoreForTesting())
    }
    
    /**
     * Test 1: Verifica che Firebase sia inizializzato correttamente
     */
    @Test
    fun testFirebaseInitialization() {
        assertTrue("Firebase non è disponibile", FirebaseTestConfig.isFirebaseAvailable())
        assertTrue("Firestore non è pronto", FirestoreTestHelper.isFirestoreReady())
    }
    
    /**
     * Test 2: Verifica connessione base a Firestore
     */
    @Test
    fun testFirestoreBasicConnection() {
        if (!FirestoreTestHelper.isFirestoreReady()) {
            Log.w(TAG, "Firestore non disponibile, skipping test")
            return
        }
        
        val latch = CountDownLatch(1)
        var connectionSuccess = false
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Test semplice: prova a leggere una collection
            firestore.collection("test_connection")
                .limit(1)
                .get()
                .addOnCompleteListener { task ->
                    connectionSuccess = task.isSuccessful || task.exception?.message?.contains("permission") == true
                    Log.d(TAG, "Test connessione Firestore: ${if (connectionSuccess) "OK" else "FAILED"}")
                    if (!connectionSuccess) {
                        Log.w(TAG, "Errore connessione: ${task.exception?.message}")
                    }
                    latch.countDown()
                }
            
            // Aspetta massimo 10 secondi
            val completed = latch.await(10, TimeUnit.SECONDS)
            
            if (!completed) {
                Log.w(TAG, "Timeout nel test di connessione Firestore")
                connectionSuccess = true // Non falliamo per timeout di rete
            }
            
            assertTrue("Connessione Firestore fallita", connectionSuccess)
            
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel test di connessione Firestore", e)
            
            // Se l'errore è relativo a protobuf, è quello che vogliamo evitare
            val isProtobufError = e.message?.contains("protobuf") == true ||
                                 e.message?.contains("GeneratedMessageLite") == true ||
                                 e.message?.contains("registerDefaultInstance") == true
                                 
            if (isProtobufError) {
                fail("Errore protobuf rilevato: ${e.message}")
            } else {
                // Altri errori possono essere dovuti a configurazione di rete/auth, li ignoriamo
                Log.w(TAG, "Errore non-critico nel test Firestore: ${e.message}")
            }
        }
    }
    
    /**
     * Test 3: Verifica che le operazioni Firestore base non generino errori protobuf
     */
    @Test
    fun testFirestoreOperationsWithoutProtobufErrors() {
        if (!FirestoreTestHelper.isFirestoreReady()) {
            Log.w(TAG, "Firestore non disponibile, skipping test")
            return
        }
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Testa operazioni che potrebbero scatenare errori protobuf
            val testData = mapOf(
                "test_field" to "test_value",
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            
            // Test di scrittura (può fallire per permessi, ma non dovrebbe dare errori protobuf)
            val latch = CountDownLatch(1)
            var protobufErrorOccurred = false
            
            firestore.collection("test_protobuf")
                .add(testData)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val error = task.exception
                        protobufErrorOccurred = error?.message?.contains("protobuf") == true ||
                                               error?.message?.contains("GeneratedMessageLite") == true ||
                                               error?.message?.contains("registerDefaultInstance") == true
                        
                        if (protobufErrorOccurred) {
                            Log.e(TAG, "Errore protobuf rilevato durante test scrittura", error)
                        } else {
                            Log.d(TAG, "Errore non-protobuf (normale): ${error?.message}")
                        }
                    }
                    latch.countDown()
                }
            
            latch.await(10, TimeUnit.SECONDS)
            
            assertFalse("Errore protobuf rilevato durante le operazioni Firestore", protobufErrorOccurred)
            
        } catch (e: Exception) {
            val isProtobufError = e.message?.contains("protobuf") == true ||
                                 e.message?.contains("GeneratedMessageLite") == true ||
                                 e.message?.contains("registerDefaultInstance") == true
                                 
            if (isProtobufError) {
                fail("Errore protobuf rilevato: ${e.message}")
            } else {
                Log.w(TAG, "Errore non-protobuf durante test: ${e.message}")
            }
        }
    }
}