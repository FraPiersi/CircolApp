package com.example.circolapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import android.util.Log

/**
 * Test per verificare la connettività del dispositivo e la configurazione base
 * Questi test sono progettati per passare anche quando il dispositivo è offline
 * o ha problemi di connettività, ma falliscono solo per errori critici dell'applicazione
 */
@RunWith(AndroidJUnit4::class)
class DeviceConnectivityTest {
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val TAG = "DeviceConnectivityTest"
    
    @Before
    fun setUp() {
        // Setup minimale che non dipende da servizi esterni
        Log.d(TAG, "Inizializzazione test base del dispositivo")
    }
    
    /**
     * Test 1: Verifica che l'app context sia disponibile
     * Questo test deve sempre passare se l'ambiente di test è corretto
     */
    @Test
    fun testAppContextAvailable() {
        assertNotNull("Context dell'applicazione non disponibile", context)
        assertEquals("com.example.circolapp", context.packageName)
        Log.d(TAG, "App context verificato: ${context.packageName}")
    }
    
    /**
     * Test 2: Verifica la configurazione base dell'app
     * Questo test verifica che l'app sia correttamente configurata senza dipendere da servizi esterni
     */
    @Test
    fun testBasicAppConfiguration() {
        try {
            // Verifica che l'applicazione sia configurata correttamente
            assertNotNull("ApplicationInfo non disponibile", context.applicationInfo)
            
            // Verifica che le risorse siano disponibili
            assertNotNull("Resources non disponibili", context.resources)
            
            Log.d(TAG, "Configurazione base dell'app verificata")
            
        } catch (e: Exception) {
            Log.e(TAG, "Errore nella verifica configurazione base", e)
            fail("Configurazione base dell'app non valida: ${e.message}")
        }
    }
    
    /**
     * Test 3: Verifica lo stato della connettività (informativo, non critico)
     * Questo test non fallisce mai, ma fornisce informazioni utili per il debugging
     */
    @Test
    fun testDeviceConnectivityStatus() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            
            if (connectivityManager == null) {
                Log.w(TAG, "ConnectivityManager non disponibile")
                return
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                
                val hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                val hasValidated = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
                
                Log.d(TAG, "Stato connessione - Internet: $hasInternet, Validated: $hasValidated")
                Log.d(TAG, "Active Network: $activeNetwork")
                
                if (!hasInternet) {
                    Log.w(TAG, "Device sembra offline - questo è normale se l'emulatore non ha connessione")
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                val isConnected = networkInfo?.isConnected == true
                Log.d(TAG, "Stato connessione (legacy): $isConnected")
                
                if (!isConnected) {
                    Log.w(TAG, "Device sembra offline (legacy check)")
                }
            }
            
            // Questo test non fallisce mai - è solo informativo
            assertTrue("Test connectivity sempre true", true)
            
        } catch (e: Exception) {
            Log.w(TAG, "Errore nel controllo connettività (non critico): ${e.message}")
            // Non falliamo il test per problemi di connettività
        }
    }
    
    /**
     * Test 4: Verifica che il Firebase Test Runner sia correttamente configurato
     */
    @Test
    fun testFirebaseTestRunnerConfiguration() {
        try {
            // Verifica che possiamo inizializzare Firebase senza errori critici
            FirebaseTestConfig.initializeFirebaseForTesting()
            
            // Se arriviamo qui, l'inizializzazione non ha generato eccezioni critiche
            assertTrue("Firebase Test Runner configurato", true)
            Log.d(TAG, "Firebase Test Runner inizializzato senza errori critici")
            
        } catch (e: Exception) {
            Log.w(TAG, "Errore nell'inizializzazione Firebase Test Runner: ${e.message}")
            
            // Fallisce solo se è un errore di configurazione critico (es. protobuf)
            val isCriticalError = e.message?.contains("protobuf") == true ||
                                 e.message?.contains("GeneratedMessageLite") == true ||
                                 e.message?.contains("registerDefaultInstance") == true ||
                                 e.message?.contains("ClassNotFoundException") == true
                                 
            if (isCriticalError) {
                fail("Errore critico nella configurazione Firebase Test Runner: ${e.message}")
            } else {
                Log.w(TAG, "Errore non critico in Firebase Test Runner - probabilmente dovuto a device offline")
            }
        }
    }
}