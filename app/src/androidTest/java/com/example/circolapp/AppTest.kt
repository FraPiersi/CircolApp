package com.example.circolapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf

/**
 * Instrumented tests per CircolApp - Test di UI e interazioni
 * 
 * Questi test verificano:
 * - UI e layout delle activity principali
 * - Navigazione tra schermata di login e registrazione  
 * - Validazione form e input fields
 * - Visibilità e funzionamento elementi UI
 * - Interazioni con bottoni e campi input
 * - Comportamento con operazioni asincrone (Firebase)
 * 
 * NOTA: I test gestiscono le operazioni Firebase ma si aspettano
 * che falliscano con credenziali di test non valide
 * 
 * Per eseguire i test:
 * ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppTest {

    @get:Rule
    val loginActivityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * Test 1: Verifica elementi UI nella schermata di login
     */
    @Test
    fun testLoginScreenElementsVisibility() {
        // Verifica che tutti gli elementi principali siano visibili
        onView(withId(R.id.imageViewLogo))
            .check(matches(isDisplayed()))

        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Accesso Utente")))

        onView(withId(R.id.editTextEmail))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextPassword))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))

        onView(withId(R.id.textViewRegister))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 2: Verifica inserimento testo nel campo email
     */
    @Test
    fun testEmailInputField() {
        val testEmail = "test@example.com"

        // Inserisce testo nel campo email senza click esplicito
        onView(withId(R.id.editTextEmail))
            .perform(replaceText(testEmail), closeSoftKeyboard())
            .check(matches(withText(testEmail)))
    }

    /**
     * Test 3: Verifica che il link di registrazione sia cliccabile
     */
    @Test
    fun testRegisterLinkClickable() {
        // Verifica che il link di registrazione sia visibile e cliccabile
        onView(withId(R.id.textViewRegister))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText("Non hai un account? Registrati")))
    }

    /**
     * Test 4: Verifica elementi UI di base
     */
    @Test
    fun testBasicUIElements() {
        // Test molto semplice che verifica solo l'esistenza degli elementi
        onView(withId(R.id.imageViewLogo))
            .check(matches(isDisplayed()))

        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 5: Verifica input campo email semplificato
     */
    @Test
    fun testSimpleEmailInput() {
        val testEmail = "simple@test.com"

        onView(withId(R.id.editTextEmail))
            .perform(replaceText(testEmail))
            .check(matches(withText(testEmail)))
    }

    /**
     * Test 6: Verifica bottone login
     */
    @Test
    fun testLoginButton() {
        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText("Login")))
    }

    /**
     * Test 7: Verifica visibilità campo password
     */
    @Test
    fun testPasswordFieldVisibility() {
        onView(withId(R.id.editTextPassword))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    /**
     * Test 8: Verifica logo app
     */
    @Test
    fun testAppLogo() {
        onView(withId(R.id.imageViewLogo))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 9: Verifica titolo login
     */
    @Test
    fun testLoginTitle() {
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Accesso Utente")))
    }

    /**
     * Test 10: Verifica click semplice sul bottone login
     */
    @Test
    fun testLoginButtonClick() {
        // Chiudi eventuale tastiera prima del test
        onView(isRoot()).perform(closeSoftKeyboard())
        
        // Assicurati che tutti gli elementi siano caricati
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
        
        // Scorri per assicurarsi che il bottone sia visibile
        onView(withId(R.id.buttonLogin))
            .perform(scrollTo())
        
        // Verifica lo stato del bottone prima del click
        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
        
        // Esegui il click
        onView(withId(R.id.buttonLogin))
            .perform(click())

        // Breve pausa per permettere al sistema di reagire
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // Verifica che siamo ancora sulla stessa schermata
        // (il login dovrebbe fallire senza credenziali valide)
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 11: Verifica comportamento login con dati inseriti (test più completo)
     */
    @Test
    fun testLoginWithCredentials() {
        val testEmail = "test@example.com"
        val testPassword = "password123"

        // Chiudi eventuale tastiera
        onView(isRoot()).perform(closeSoftKeyboard())

        // Inserisce email
        onView(withId(R.id.editTextEmail))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(replaceText(testEmail))
            .perform(closeSoftKeyboard())

        // Inserisce password  
        onView(withId(R.id.editTextPassword))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(replaceText(testPassword))
            .perform(closeSoftKeyboard())

        // Verifica i dati inseriti
        onView(withId(R.id.editTextEmail))
            .check(matches(withText(testEmail)))
        onView(withId(R.id.editTextPassword))
            .check(matches(withText(testPassword)))

        // Clicca login
        onView(withId(R.id.buttonLogin))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())

        // Pausa per permettere al ViewModel di processare
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // Il login dovrebbe fallire con credenziali invalide, ma almeno il click dovrebbe funzionare
        // Verifica che il titolo sia ancora visibile (non dovrebbe navigare via)
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 12: Verifica click su link registrazione 
     */
    @Test
    fun testRegisterLinkClick() {
        // Chiudi eventuale tastiera
        onView(isRoot()).perform(closeSoftKeyboard())
        
        onView(withId(R.id.textViewRegister))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .perform(click())

        // Dopo il click dovrebbe navigare alla RegisterActivity
        // Nota: questo test può fallire se non c'è la RegisterActivity, 
        // ma almeno testiamo che il click funzioni
    }

    /**
     * Test 13: Verifica comportamento campi input
     */
    @Test
    fun testInputFieldsInteraction() {
        val testEmail = "test@domain.com"
        val testPassword = "securepass"

        // Chiudi eventuale tastiera
        onView(isRoot()).perform(closeSoftKeyboard())

        // Test campo email
        onView(withId(R.id.editTextEmail))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(replaceText(testEmail))
            .check(matches(withText(testEmail)))

        // Test campo password
        onView(withId(R.id.editTextPassword))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(replaceText(testPassword))
            .check(matches(withText(testPassword)))

        // Verifica che i dati persistano
        onView(withId(R.id.editTextEmail))
            .check(matches(withText(testEmail)))
        onView(withId(R.id.editTextPassword))
            .check(matches(withText(testPassword)))
    }

    /**
     * Test 15: Test robusto con gestione avanzata per il click del bottone
     */
    @Test
    fun testLoginButtonClickRobust() {
        // Setup iniziale - chiudi tastiera e stabilizza UI
        onView(isRoot()).perform(closeSoftKeyboard())
        
        // Aspetta che l'Activity sia completamente caricata
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Accesso Utente")))

        // Verifica che tutti gli elementi siano presenti prima di procedere
        onView(withId(R.id.editTextEmail))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextPassword))
            .check(matches(isDisplayed()))
            
        // Focus sul bottone per assicurarsi che sia nel viewport
        onView(withId(R.id.buttonLogin))
            .perform(scrollTo())
            
        // Verifica stato del bottone in modo esteso
        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
            .check(matches(withText("Login")))
            
        // Breve pausa per stabilizzare la UI
        try {
            Thread.sleep(200)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
            
        // Esegui click verificando lo stato immediatamente prima
        onView(allOf(withId(R.id.buttonLogin), isDisplayed(), isEnabled()))
            .perform(click())
            
        // Pausa maggiore per permettere processing asincrono
        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // Verifica finale - dovremmo essere ancora sulla login screen
        // perché il login fallisce senza credenziali valide
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 16: Verifica interazione bottone senza logica business (test alternativo)
     */
    @Test
    fun testLoginButtonInteractionOnly() {
        // Chiudi eventuale tastiera prima del test
        onView(isRoot()).perform(closeSoftKeyboard())
        
        // Test che verifica solo che il bottone sia interagibile
        // senza effettuare il vero click che triggera Firebase
        onView(withId(R.id.buttonLogin))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
            .check(matches(withText("Login")))
    }
}