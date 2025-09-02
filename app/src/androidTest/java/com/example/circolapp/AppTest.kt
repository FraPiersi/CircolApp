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

/**
 * Instrumented tests per CircolApp - Test di UI locali senza Firebase
 * 
 * Questi test verificano:
 * - UI e layout delle activity principali
 * - Navigazione tra schermata di login e registrazione  
 * - Validazione form e input fields
 * - Visibilità e funzionamento elementi UI
 * - Transizioni tra activity
 * 
 * NOTA: Questi test non accedono a Firebase e testano solo funzionalità locali
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
     * Test 2: Verifica inserimento testo nei campi email e password
     */
    @Test
    fun testLoginInputFields() {
        val testEmail = "test@example.com"
        val testPassword = "testpassword123"

        // Inserisce testo nel campo email
        onView(withId(R.id.editTextEmail))
            .perform(typeText(testEmail))
            .check(matches(withText(testEmail)))

        // Inserisce testo nel campo password
        onView(withId(R.id.editTextPassword))
            .perform(typeText(testPassword))
            .check(matches(withText(testPassword)))

        // Chiude la tastiera
        onView(withId(R.id.editTextPassword))
            .perform(closeSoftKeyboard())
    }

    /**
     * Test 3: Verifica navigazione da Login a Register
     */
    @Test
    fun testNavigationFromLoginToRegister() {
        // Clicca sul link per la registrazione
        onView(withId(R.id.textViewRegister))
            .perform(click())

        // Aspetta che l'activity si carichi e verifica che siamo nella schermata di registrazione
        Thread.sleep(1000)
        
        // Verifica elementi della schermata di registrazione
        onView(withId(R.id.textViewRegisterTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Registrazione")))

        onView(withId(R.id.editTextDisplayName))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextEmailRegister))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextPasswordRegister))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 4: Verifica elementi UI nella schermata di registrazione
     */
    @Test
    fun testRegisterScreenElements() {
        // Naviga alla schermata di registrazione
        onView(withId(R.id.textViewRegister))
            .perform(click())

        Thread.sleep(1000)

        // Verifica tutti gli elementi della registrazione
        onView(withId(R.id.imageViewLogoRegister))
            .check(matches(isDisplayed()))

        onView(withId(R.id.textViewRegisterTitle))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextDisplayName))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextEmailRegister))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextPasswordRegister))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonRegister))
            .check(matches(isDisplayed()))
            .check(matches(withText("Registrati")))
    }

    /**
     * Test 5: Verifica input fields nella schermata di registrazione
     */
    @Test
    fun testRegisterInputFields() {
        // Naviga alla schermata di registrazione
        onView(withId(R.id.textViewRegister))
            .perform(click())

        Thread.sleep(1000)

        val testDisplayName = "Test User"
        val testEmail = "testuser@example.com"
        val testPassword = "password123"

        // Test input del display name
        onView(withId(R.id.editTextDisplayName))
            .perform(typeText(testDisplayName))
            .check(matches(withText(testDisplayName)))

        // Test input dell'email
        onView(withId(R.id.editTextEmailRegister))
            .perform(typeText(testEmail))
            .check(matches(withText(testEmail)))

        // Test input della password
        onView(withId(R.id.editTextPasswordRegister))
            .perform(typeText(testPassword))
            .check(matches(withText(testPassword)))

        onView(withId(R.id.editTextPasswordRegister))
            .perform(closeSoftKeyboard())
    }

    /**
     * Test 6: Verifica navigazione da Register a Login
     */
    @Test
    fun testNavigationFromRegisterToLogin() {
        // Naviga alla schermata di registrazione
        onView(withId(R.id.textViewRegister))
            .perform(click())

        Thread.sleep(1000)

        // Clicca sul link per tornare al login
        onView(withId(R.id.textViewLoginLink))
            .perform(click())

        Thread.sleep(1000)

        // Verifica che siamo tornati alla schermata di login
        onView(withId(R.id.textViewLoginTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Accesso Utente")))
    }

    /**
     * Test 7: Verifica pressione bottone login (senza autenticazione)
     */
    @Test
    fun testLoginButtonPress() {
        // Inserisce credenziali di test
        onView(withId(R.id.editTextEmail))
            .perform(typeText("test@example.com"))

        onView(withId(R.id.editTextPassword))
            .perform(typeText("password123"))
            .perform(closeSoftKeyboard())

        // Clicca il bottone di login
        onView(withId(R.id.buttonLogin))
            .perform(click())

        // Il test verifica solo che il click funzioni e non causi crash
        // In una vera app, questo potrebbe mostrare un errore o loading
        Thread.sleep(2000)
    }

    /**
     * Test 8: Verifica pressione bottone registrazione (senza Firebase)
     */
    @Test
    fun testRegisterButtonPress() {
        // Naviga alla schermata di registrazione
        onView(withId(R.id.textViewRegister))
            .perform(click())

        Thread.sleep(1000)

        // Compila i campi
        onView(withId(R.id.editTextDisplayName))
            .perform(typeText("Test User"))

        onView(withId(R.id.editTextEmailRegister))
            .perform(typeText("test@example.com"))

        onView(withId(R.id.editTextPasswordRegister))
            .perform(typeText("password123"))
            .perform(closeSoftKeyboard())

        // Clicca il bottone di registrazione
        onView(withId(R.id.buttonRegister))
            .perform(click())

        // Il test verifica solo che il click funzioni senza crash
        Thread.sleep(2000)
    }

    /**
     * Test 9: Verifica form validation - campi vuoti
     */
    @Test
    fun testEmptyFieldsValidation() {
        // Tenta login con campi vuoti
        onView(withId(R.id.buttonLogin))
            .perform(click())

        Thread.sleep(1000)

        // Verifica che i campi siano ancora visibili (non si è navigato via)
        onView(withId(R.id.editTextEmail))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextPassword))
            .check(matches(isDisplayed()))
    }

    /**
     * Test 10: Verifica interazione con progress bar durante login
     */
    @Test
    fun testProgressBarVisibilityDuringLogin() {
        // Inserisce credenziali
        onView(withId(R.id.editTextEmail))
            .perform(typeText("test@example.com"))

        onView(withId(R.id.editTextPassword))
            .perform(typeText("password123"))
            .perform(closeSoftKeyboard())

        // Verifica che la progress bar non sia inizialmente visibile
        onView(withId(R.id.progressBarLogin))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // Clicca login
        onView(withId(R.id.buttonLogin))
            .perform(click())

        Thread.sleep(1000)

        // Nota: In un'app reale, qui potremmo verificare che la progress bar diventi visibile
        // durante l'autenticazione, ma senza Firebase non possiamo testare questo comportamento
    }
}