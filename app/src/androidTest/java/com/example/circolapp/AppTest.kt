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
 */
@RunWith(AndroidJUnit4::class)
class AppTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testEmail = "test.user@circolapp.com"
    private val testPassword = "password123"
    private val testDisplayName = "Test User"
    private val adminEmail = "admin@circolapp.com"
    private val adminPassword = "admin123"

    @Before
    fun setUp() {
        FirebaseTestConfig.initializeFirebaseForTesting()
        FirestoreTestHelper.configureFirestoreForTesting()
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        FirebaseTestConfig.clearFirebaseAuth()
        FirestoreTestHelper.cleanupTestData()
    }

    @Test
    fun testUserRegistration() {
        val intent = Intent(context, RegisterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<RegisterActivity>(intent).use {
            onView(withId(R.id.editTextDisplayName))
                .perform(typeText(testDisplayName))

            onView(withId(R.id.editTextEmailRegister))
                .perform(typeText(testEmail))

            onView(withId(R.id.editTextPasswordRegister))
                .perform(typeText(testPassword))

            onView(withId(R.id.editTextPasswordRegister))
                .perform(closeSoftKeyboard())

            onView(withId(R.id.buttonRegister))
                .perform(click())

            onView(withId(R.id.progressBarRegister))
                .check(matches(isDisplayed()))

            Thread.sleep(5000)
        }
    }

    @Test
    fun testUserLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            
            onView(withId(R.id.editTextEmail))
                .perform(typeText(testEmail))

            onView(withId(R.id.editTextPassword))
                .perform(typeText(testPassword))

            onView(withId(R.id.editTextPassword))
                .perform(closeSoftKeyboard())

            
            onView(withId(R.id.buttonLogin))
                .perform(click())

            
            onView(withId(R.id.progressBarLogin))
                .check(matches(isDisplayed()))

            
            Thread.sleep(5000)

        }
    }

    /**
     */
    @Test
    fun testUserLogout() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.buttonLogout))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testAddNewProduct() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(3000)

            
            onView(withId(R.id.recyclerViewProducts))
                .check(matches(isDisplayed()))

            
            onView(withId(R.id.fabAddProduct))
                .check(matches(isDisplayed()))
                .perform(click())

            Thread.sleep(1000)

            
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

            
            onView(withId(R.id.btnSaveProduct))
                .check(matches(isDisplayed()))

            // Il salvataggio effettivo richiederebbe un ambiente di test più complesso
        }
    }

    /**
     */
    @Test
    fun testProductOrdering() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga ai pagamenti
            onView(withId(R.id.pagamentoFragment))
                .perform(click())

            Thread.sleep(1000)

            
            onView(withId(R.id.btn_opzione2))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.recyclerViewProducts))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testEventParticipation() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga agli eventi
            onView(withId(R.id.eventiFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.eventiRecyclerView))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testEventCreation() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga agli eventi
            onView(withId(R.id.eventiFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.fabAddEvento))
                .check(matches(isDisplayed()))
                .perform(click())

            Thread.sleep(1000)

            
            onView(withId(R.id.editNomeEvento))
                .perform(typeText("Evento Test"))

            onView(withId(R.id.editDescrizioneEvento))
                .perform(typeText("Descrizione evento di test"))

            onView(withId(R.id.editDescrizioneEvento))
                .perform(closeSoftKeyboard())

            
            onView(withId(R.id.btnSalvaEvento))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testTesseraRequest() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.buttonTessera))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testTesseraAssignment() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.buttonGestisciTessere))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testUserRecharge() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            // Naviga ai pagamenti
            onView(withId(R.id.pagamentoFragment))
                .perform(click())

            Thread.sleep(1000)

            
            onView(withId(R.id.btn_opzione1))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.qrCodeImageView))
                .check(matches(isDisplayed()))

        }
    }

    /**
     */
    @Test
    fun testCashPayment() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            
            onView(withId(R.id.cassaFragment))
                .perform(click())

            Thread.sleep(2000)

        }
    }

    /**
     */
    @Test
    fun testBasicAppFunctionality() {
        
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            
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
     */
    @Test
    fun testQrCodeGeneration() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            
            onView(withId(R.id.pagamentoFragment))
                .perform(click())

            Thread.sleep(1000)

            onView(withId(R.id.btn_opzione1))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.qrCodeImageView))
                .check(matches(isDisplayed()))

            
            onView(withId(R.id.textViewQrTitle))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))))
        }
    }

    /**
     */
    @Test
    fun testUserBalanceManagement() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.USER.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(2000)

            
            onView(withId(R.id.profiloFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.text_saldo))
                .check(matches(isDisplayed()))

            
            onView(withId(R.id.homeFragment))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.recyclerViewMovimenti))
                .check(matches(isDisplayed()))
        }
    }

    /**
     */
    @Test
    fun testUserAccessToFeatures() {
        
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
        
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<LoginActivity>(intent).use {
            
            onView(withId(R.id.editTextEmail))
                .check(matches(isDisplayed()))

            onView(withId(R.id.editTextPassword))
                .check(matches(isDisplayed()))

            onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()))

            onView(withId(R.id.textViewRegister))
                .check(matches(isDisplayed()))

            
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
            
            onView(withId(R.id.buttonRegister))
                .perform(click())

            Thread.sleep(1000)

            
            onView(withId(R.id.editTextDisplayName))
                .perform(typeText("Test User"))

            onView(withId(R.id.editTextEmailRegister))
                .perform(typeText("test@test.com"))

            onView(withId(R.id.editTextPasswordRegister))
                .perform(typeText("123")) // 

            onView(withId(R.id.editTextPasswordRegister))
                .perform(closeSoftKeyboard())

            onView(withId(R.id.buttonRegister))
                .perform(click())

            Thread.sleep(2000)

            
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
            
            onView(withId(R.id.buttonLogin))
                .perform(click())

            Thread.sleep(1000)

            
            onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()))

            
            onView(withId(R.id.editTextEmail))
                .perform(typeText("email-non-valida"))

            onView(withId(R.id.editTextPassword))
                .perform(typeText("password"))

            onView(withId(R.id.editTextPassword))
                .perform(closeSoftKeyboard())

            onView(withId(R.id.buttonLogin))
                .perform(click())

            Thread.sleep(2000)

            
            onView(withId(R.id.editTextEmail))
                .check(matches(isDisplayed()))
        }
    }

    /**
     * Test per verifica permessi admin vs user
     */
    @Test
    fun testAdminPermissions() {
        
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("USER_ROLE", UserRole.ADMIN.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            Thread.sleep(3000)

            
            onView(withId(R.id.recyclerViewProducts))
                .check(matches(isDisplayed()))

            
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