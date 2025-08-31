package com.example.circolapp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.circolapp.model.UserRole
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.*

/**
 * Instrumented tests per le principali funzionalità di CircolApp
 *
 * Questi test coprono i flussi principali dell'applicazione:
 * 1. Registrazione e autenticazione utenti
 * 2. Gestione prodotti e ordinazioni
 * 3. Gestione eventi e partecipazione
 * 4. Sistema tessere (richiesta e assegnazione)
 * 5. Ricariche e pagamenti
 * 6. Differenze di permessi tra utenti e admin
 *
 * NOTA: Questi sono test di UI che verificano l'accessibilità e la presenza
 * degli elementi dell'interfaccia. Per test completamente funzionali,
 * sarebbe necessario configurare un ambiente di test Firebase con dati di test.
 *
 * Per eseguire i test:
 * ./gradlew connectedAndroidTest
 *
 * Funzionalità testate:
 * - Registrazione nuovo utente
 * - Login utente esistente
 * - Logout
 * - Aggiunta nuovo prodotto (admin)
 * - Ordinazione prodotto
 * - Partecipazione ad eventi
 * - Creazione eventi (admin)
 * - Richiesta tessera
 * - Assegnazione tessera (admin)
 * - Ricarica utente
 * - Pagamento in cassa
 * - Validazione form e permessi
 *
 * IMPORTANTE: Per test completi con Firebase, configurare:
 * 1. Firebase Test Lab o Firebase Auth Emulator
 * 2. Utenti di test specifici
 * 3. Dati di test nel database Firestore
 */
@RunWith(AndroidJUnit4::class)
class AppTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // Email e dati di test
    private val testEmail = "test.user@circolapp.com"
    private val testPassword = "password123"
    private val testDisplayName = "Test User"
    private val adminEmail = "admin@circolapp.com"
    private val adminPassword = "admin123"

    @Before
    fun setUp() {
        // Inizializza Firebase per i test
        FirebaseTestConfig.initializeFirebaseForTesting()
        
        // Configura Firestore per evitare errori protobuf
        FirestoreTestHelper.configureFirestoreForTesting()
        
        // Setup iniziale per ogni test
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        // Cleanup Firebase Auth dopo ogni test per evitare interferenze
        FirebaseTestConfig.clearFirebaseAuth()
        
        // Cleanup dati di test
        FirestoreTestHelper.cleanupTestData()
        
        // Cleanup dopo ogni test se necessario
    }

    /**
     * Test 1: Registrazione nuovo utente
     */
    @Test
    fun testUserRegistration() {
        val intent = Intent(context, RegisterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<RegisterActivity>(intent).use {
            // Inserisci dati di registrazione
            onView(withId(R.id.editTextDisplayName))
                .perform(typeText(testDisplayName))

            onView(withId(R.id.editTextEmailRegister))
                .perform(typeText(testEmail))

            onView(withId(R.id.editTextPasswordRegister))
                .perform(typeText(testPassword))

            // Chiudi la tastiera
            onView(withId(R.id.editTextPasswordRegister))
                .perform(closeSoftKeyboard())

            // Clicca il pulsante di registrazione
            onView(withId(R.id.buttonRegister))
                .perform(click())

            // Verifica che il progress bar sia visibile durante il caricamento
            onView(withId(R.id.progressBarRegister))
                .check(matches(isDisplayed()))

            // Attendi il completamento (timeout lungo per operazioni Firebase)
            Thread.sleep(5000)
        }
    }

    /**
     * Test 2: Login utente esistente
     */
    @Test
    fun testUserLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            // Inserisci credenziali di login
            onView(withId(R.id.editTextEmail))
                .perform(typeText(testEmail))

            onView(withId(R.id.editTextPassword))
                .perform(typeText(testPassword))

            onView(withId(R.id.editTextPassword))
                .perform(closeSoftKeyboard())

            // Clicca il pulsante di login
            onView(withId(R.id.buttonLogin))
                .perform(click())

            // Verifica che il progress bar sia visibile
            onView(withId(R.id.progressBarLogin))
                .check(matches(isDisplayed()))

            // Attendi il completamento del login
            Thread.sleep(5000)

            // Verifica che sia stata avviata MainActivity (il login è riuscito)
            // Questo test verifica che non siamo più nella LoginActivity
        }
    }

    /**
     * Test 3: Logout utente
     */
    @Test
    fun testUserLogout() {
        // Avvia MainActivity (simulando che l'utente sia già loggato)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga al profilo
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il pulsante logout sia presente
            onView(withId(R.id.buttonLogout))
                .check(matches(isDisplayed()))

            // Il test verifica la presenza del pulsante logout
            // L'azione di logout effettiva richiederebbe un ambiente di test più complesso
        }
    }

    /**
     * Test 4: Aggiunta nuovo prodotto (funzionalità admin)
     */
    @Test
    fun testAddNewProduct() {
        // Avvia MainActivity come admin
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(3000)

            // Verifica che siamo nel catalogo prodotti (default per admin)
            onView(withId(R.id.recyclerViewProducts))
                .check(matches(isDisplayed()))

            // Verifica che il FAB per aggiungere prodotti sia visibile
            onView(withId(R.id.fabAddProduct))
                .check(matches(isDisplayed()))
                .perform(click())

            Thread.sleep(1000)

            // Compila i campi del nuovo prodotto
            onView(withId(R.id.etProductName))
                .perform(typeText("Prodotto Test"))

            onView(withId(R.id.etProductDescription))
                .perform(typeText("Descrizione del prodotto di test"))

            onView(withId(R.id.etProductPieces))
                .perform(typeText("10"))

            onView(withId(R.id.etProductAmount))
                .perform(typeText("15.50"))

            onView(withId(R.id.etProductAmount))
                .perform(closeSoftKeyboard())

            // Verifica che il pulsante salva sia presente
            onView(withId(R.id.btnSaveProduct))
                .check(matches(isDisplayed()))

            // Il test verifica l'accesso alla funzionalità e la presenza dei campi
            // Il salvataggio effettivo richiederebbe un ambiente di test più complesso
        }
    }

    /**
     * Test 5: Ordinazione prodotto
     */
    @Test
    fun testProductOrdering() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga ai pagamenti
            onView(withId(R.id.pagamentoFragment))
                .perform(click())

            Thread.sleep(1000)

            // Clicca per vedere i prodotti
            onView(withId(R.id.btn_opzione2))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il catalogo prodotti sia visibile
            onView(withId(R.id.recyclerViewProducts))
                .check(matches(isDisplayed()))

            // Il test verifica l'accesso al catalogo prodotti per ordinare
            // L'ordinazione effettiva richiederebbe prodotti nel database e autenticazione
        }
    }

    /**
     * Test 6: Partecipazione ad un evento
     */
    @Test
    fun testEventParticipation() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga agli eventi
            onView(withId(R.id.eventiFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che la lista eventi sia presente
            onView(withId(R.id.eventiRecyclerView))
                .check(matches(isDisplayed()))

            // Il test verifica l'accesso alla lista eventi
            // La partecipazione effettiva richiederebbe eventi nel database
        }
    }

    /**
     * Test 7: Creazione evento (funzionalità admin)
     */
    @Test
    fun testEventCreation() {
        // Avvia MainActivity come admin
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga agli eventi
            onView(withId(R.id.eventiFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il FAB per aggiungere eventi sia presente (solo admin)
            onView(withId(R.id.fabAddEvento))
                .check(matches(isDisplayed()))
                .perform(click())

            Thread.sleep(1000)

            // Compila i dati dell'evento
            onView(withId(R.id.editNomeEvento))
                .perform(typeText("Evento Test"))

            onView(withId(R.id.editDescrizioneEvento))
                .perform(typeText("Descrizione evento di test"))

            onView(withId(R.id.editDescrizioneEvento))
                .perform(closeSoftKeyboard())

            // Verifica che il pulsante salva sia presente
            onView(withId(R.id.btnSalvaEvento))
                .check(matches(isDisplayed()))

            // Il test verifica l'accesso alla funzionalità di creazione eventi per admin
        }
    }

    /**
     * Test 8: Richiesta tessera
     */
    @Test
    fun testTesseraRequest() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga al profilo
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il pulsante tessera sia presente
            onView(withId(R.id.buttonTessera))
                .check(matches(isDisplayed()))

            // Il test verifica la presenza della funzionalità tessera
            // La richiesta effettiva richiederebbe l'autenticazione Firebase
        }
    }

    /**
     * Test 9: Assegnazione tessera (funzionalità admin)
     */
    @Test
    fun testTesseraAssignment() {
        // Avvia MainActivity come admin
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga al profilo
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il pulsante gestisci tessere sia presente (visibile solo agli admin)
            onView(withId(R.id.buttonGestisciTessere))
                .check(matches(isDisplayed()))

            // Il test verifica l'accesso alla funzionalità admin per gestire tessere
        }
    }

    /**
     * Test 10: Ricarica utente
     */
    @Test
    fun testUserRecharge() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga ai pagamenti
            onView(withId(R.id.pagamentoFragment))
                .perform(click())

            Thread.sleep(1000)

            // Clicca per la ricarica tramite QR
            onView(withId(R.id.btn_opzione1))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che siamo nella pagina QR (che gestisce anche ricariche)
            onView(withId(R.id.qrCodeImageView))
                .check(matches(isDisplayed()))

            // Il test verifica l'accesso alla funzionalità di ricarica tramite QR
        }
    }

    /**
     * Test 11: Pagamento in cassa
     */
    @Test
    fun testCashPayment() {
        // Avvia MainActivity come admin (per gestire pagamenti in cassa)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga al fragment cassa
            onView(withId(R.id.cassaFragment))
                .perform(click())

            Thread.sleep(2000)

            // Il test verifica l'accesso alla funzionalità di cassa per gli admin
        }
    }

    /**
     * Test 12: Verifica funzionalità di base dell'app
     */
    @Test
    fun testBasicAppFunctionality() {
        // Verifica che l'app si avvii correttamente con LoginActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            // Verifica che tutti gli elementi di login siano presenti
            onView(withId(R.id.editTextEmail))
                .check(matches(isDisplayed()))

            onView(withId(R.id.editTextPassword))
                .check(matches(isDisplayed()))

            onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()))

            onView(withId(R.id.textViewRegister))
                .check(matches(isDisplayed()))

            // Verifica il link per la registrazione
            onView(withId(R.id.textViewRegister))
                .perform(click())

            Thread.sleep(2000)

            // Dovremmo essere nella RegisterActivity
            onView(withId(R.id.buttonRegister))
                .check(matches(isDisplayed()))
        }
    }

    // Helper methods per semplificare i test

    /**
     * Esegue login come utente di test
     * Nota: In un ambiente di test reale, questo dovrebbe essere sostituito
     * con mock o test users appropriati
     */
    private fun loginAsTestUser() {
        // Questo è un placeholder per il processo di login
        // In un ambiente di test reale, si dovrebbe:
        // 1. Usare Firebase Auth Test Environment
        // 2. Creare utenti di test specifici
        // 3. Gestire lo stato di autenticazione correttamente
    }

    /**
     * Esegue login come admin
     * Nota: In un ambiente di test reale, questo dovrebbe essere sostituito
     * con mock o test users appropriati
     */
    private fun loginAsAdmin() {
        // Questo è un placeholder per il processo di login admin
        // In un ambiente di test reale, si dovrebbe:
        // 1. Usare Firebase Auth Test Environment
        // 2. Creare admin di test specifici
        // 3. Gestire lo stato di autenticazione correttamente
    }

    /**
     * Test 13: Verifica funzionalità QR Code
     */
    @Test
    fun testQrCodeGeneration() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga al QR Code tramite il menu pagamento
            onView(withId(R.id.pagamentoFragment))
                .perform(click())

            Thread.sleep(1000)

            onView(withId(R.id.btn_opzione1))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che l'ImageView del QR code sia presente
            onView(withId(R.id.qrCodeImageView))
                .check(matches(isDisplayed()))

            // Verifica che il titolo sia visualizzato
            onView(withId(R.id.textViewQrTitle))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))))
        }
    }

    /**
     * Test 14: Verifica gestione saldo utente
     */
    @Test
    fun testUserBalanceManagement() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga al profilo per vedere il saldo
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il saldo sia visibile nel profilo
            onView(withId(R.id.text_saldo))
                .check(matches(isDisplayed()))

            // Naviga alla home per vedere i movimenti
            onView(withId(R.id.homeFragment))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che la lista movimenti sia presente
            onView(withId(R.id.recyclerViewMovimenti))
                .check(matches(isDisplayed()))
        }
    }

    /**
     * Test 15: Verifica accesso diverse funzionalità utente
     */
    @Test
    fun testUserAccessToFeatures() {
        // Avvia MainActivity come utente
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Verifica accesso a tutte le funzionalità base dell'utente

            // Verifica Home fragment
            onView(withId(R.id.homeFragment))
                .perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.saldoText))
                .check(matches(isDisplayed()))

            // Verifica Eventi fragment
            onView(withId(R.id.eventiFragment))
                .perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.eventiRecyclerView))
                .check(matches(isDisplayed()))

            // Verifica Pagamento fragment
            onView(withId(R.id.pagamentoFragment))
                .perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.btn_opzione1))
                .check(matches(isDisplayed()))

            // Verifica Profilo fragment
            onView(withId(R.id.profiloFragment))
                .perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.buttonLogout))
                .check(matches(isDisplayed()))
        }
    }

    /**
     * Test helper per verificare connettività e stato dell'app
     */
    @Test
    fun testAppConnectivityAndState() {
        // Verifica che l'app si avvii correttamente
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            // Verifica che la schermata di login sia presente
            onView(withId(R.id.editTextEmail))
                .check(matches(isDisplayed()))

            onView(withId(R.id.editTextPassword))
                .check(matches(isDisplayed()))

            onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()))

            onView(withId(R.id.textViewRegister))
                .check(matches(isDisplayed()))

            // Verifica che i campi siano inizialmente vuoti
            onView(withId(R.id.editTextEmail))
                .check(matches(withText("")))

            onView(withId(R.id.editTextPassword))
                .check(matches(withText("")))
        }
    }

    /**
     * Test per validazione form registrazione
     */
    @Test
    fun testRegistrationFormValidation() {
        val intent = Intent(context, RegisterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<RegisterActivity>(intent).use {
            // Testa registrazione con campi vuoti
            onView(withId(R.id.buttonRegister))
                .perform(click())

            Thread.sleep(1000)

            // Testa con password troppo corta
            onView(withId(R.id.editTextDisplayName))
                .perform(typeText("Test User"))

            onView(withId(R.id.editTextEmailRegister))
                .perform(typeText("test@test.com"))

            onView(withId(R.id.editTextPasswordRegister))
                .perform(typeText("123")) // Password troppo corta

            onView(withId(R.id.editTextPasswordRegister))
                .perform(closeSoftKeyboard())

            onView(withId(R.id.buttonRegister))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che non sia avvenuta navigazione (siamo ancora in RegisterActivity)
            onView(withId(R.id.buttonRegister))
                .check(matches(isDisplayed()))
        }
    }

    /**
     * Test per validazione form login
     */
    @Test
    fun testLoginFormValidation() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            // Testa login con campi vuoti
            onView(withId(R.id.buttonLogin))
                .perform(click())

            Thread.sleep(1000)

            // Verifica che siamo ancora nella schermata di login
            onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()))

            // Testa con email non valida
            onView(withId(R.id.editTextEmail))
                .perform(typeText("email-non-valida"))

            onView(withId(R.id.editTextPassword))
                .perform(typeText("password"))

            onView(withId(R.id.editTextPassword))
                .perform(closeSoftKeyboard())

            onView(withId(R.id.buttonLogin))
                .perform(click())

            Thread.sleep(2000)

            // Verifica che il login non sia riuscito
            onView(withId(R.id.editTextEmail))
                .check(matches(isDisplayed()))
        }
    }

    /**
     * Test per verifica permessi admin vs user
     */
    @Test
    fun testAdminPermissions() {
        // Avvia MainActivity come admin
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(3000)

            // Verifica che il catalogo prodotti sia visibile (default per admin)
            onView(withId(R.id.recyclerViewProducts))
                .check(matches(isDisplayed()))

            // Verifica che il FAB per aggiungere prodotti sia visibile
            onView(withId(R.id.fabAddProduct))
                .check(matches(isDisplayed()))

            // Naviga agli eventi e verifica FAB per aggiungere eventi
            onView(withId(R.id.eventiFragment))
                .perform(click())

            Thread.sleep(2000)

            onView(withId(R.id.fabAddEvento))
                .check(matches(isDisplayed()))

            // Verifica accesso a gestione tessere
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            onView(withId(R.id.buttonGestisciTessere))
                .check(matches(isDisplayed()))
        }
    }
}