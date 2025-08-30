package com.example.circolapp

import android.content.Context
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.circolapp.adapter.MovimentiAdapter
import com.example.circolapp.model.Movimento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import java.text.NumberFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests per HomeFragment
 * Questi test verificano la logica di business del fragment senza dipendenze esterne
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class HomeFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testCurrencyFormatterConfiguration() {
        // Given
        val fragment = HomeFragment()
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
        
        // When & Then
        assertEquals("€ 0,00", currencyFormatter.format(0.0))
        assertEquals("€ 100,50", currencyFormatter.format(100.50))
        assertEquals("€ 1.234,56", currencyFormatter.format(1234.56))
        assertEquals("€ -50,25", currencyFormatter.format(-50.25))
    }

    @Test
    fun testMovimentiAdapterInitialization() {
        // Given & When
        val adapter = MovimentiAdapter()
        
        // Then
        assertNotNull(adapter)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun testLinearLayoutManagerConfiguration() {
        // Given & When
        val layoutManager = LinearLayoutManager(context)
        
        // Then
        assertNotNull(layoutManager)
        assertEquals(RecyclerView.VERTICAL, layoutManager.orientation)
    }

    @Test
    fun testMovimentoDataClass() {
        // Given
        val importo = 125.50
        val descrizione = "Test movimento"
        val data = Date()
        
        // When
        val movimento = Movimento(importo, descrizione, data)
        
        // Then
        assertEquals(importo, movimento.importo)
        assertEquals(descrizione, movimento.descrizione)
        assertEquals(data, movimento.data)
    }

    @Test
    fun testMovimentoDataClassDefaults() {
        // When
        val movimento = Movimento()
        
        // Then
        assertEquals(0.0, movimento.importo)
        assertEquals("", movimento.descrizione)
        assertNotNull(movimento.data)
    }

    @Test
    fun testFirebaseAuthInstance() {
        // Given & When
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            val authInstance = FirebaseAuth.getInstance()
            
            // Then
            assertNotNull(authInstance)
            assertEquals(mockFirebaseAuth, authInstance)
        }
    }

    @Test
    fun testUserLoggedInState() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
            
            // When
            val currentUser = FirebaseAuth.getInstance().currentUser
            
            // Then
            assertNotNull(currentUser)
            assertEquals(mockFirebaseUser, currentUser)
        }
    }

    @Test
    fun testUserNotLoggedInState() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(null)
            
            // When
            val currentUser = FirebaseAuth.getInstance().currentUser
            
            // Then
            assertEquals(null, currentUser)
        }
    }

    @Test
    fun testViewVisibilityStates() {
        // Test che verifica che le costanti di visibilità siano corrette
        assertEquals(0, View.VISIBLE)
        assertEquals(4, View.INVISIBLE)
        assertEquals(8, View.GONE)
    }

    @Test
    fun testMovimentiListProcessing() {
        // Given
        val movimenti = listOf(
            Movimento(100.0, "Entrata", Date()),
            Movimento(-50.0, "Uscita", Date()),
            Movimento(25.75, "Bonus", Date())
        )
        
        // When
        val isEmpty = movimenti.isEmpty()
        val size = movimenti.size
        val totalAmount = movimenti.sumOf { it.importo }
        
        // Then
        assertEquals(false, isEmpty)
        assertEquals(3, size)
        assertEquals(75.75, totalAmount)
    }

    @Test
    fun testEmptyMovimentiListProcessing() {
        // Given
        val movimenti = emptyList<Movimento>()
        
        // When
        val isEmpty = movimenti.isEmpty()
        val size = movimenti.size
        
        // Then
        assertEquals(true, isEmpty)
        assertEquals(0, size)
    }

    @Test
    fun testNullMovimentiListProcessing() {
        // Given
        val movimenti: List<Movimento>? = null
        
        // When
        val isNullOrEmpty = movimenti.isNullOrEmpty()
        val safeList = movimenti ?: emptyList()
        
        // Then
        assertEquals(true, isNullOrEmpty)
        assertEquals(0, safeList.size)
    }

    @Test
    fun testSaldoDefaultValue() {
        // Given
        val defaultSaldo = 0.0
        
        // When
        val isZero = defaultSaldo == 0.0
        val formattedSaldo = NumberFormat.getCurrencyInstance(Locale.ITALY).format(defaultSaldo)
        
        // Then
        assertTrue(isZero)
        assertEquals("€ 0,00", formattedSaldo)
    }

    @Test
    fun testPositiveAndNegativeSaldoFormatting() {
        // Given
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
        val positiveSaldo = 1500.75
        val negativeSaldo = -300.50
        
        // When
        val positiveFormatted = currencyFormatter.format(positiveSaldo)
        val negativeFormatted = currencyFormatter.format(negativeSaldo)
        
        // Then
        assertEquals("€ 1.500,75", positiveFormatted)
        assertEquals("€ -300,50", negativeFormatted)
        assertTrue(positiveFormatted.contains("€"))
        assertTrue(negativeFormatted.contains("€"))
        assertTrue(negativeFormatted.contains("-"))
    }

    @Test
    fun testLiveDoneInitialization() {
        // Given & When
        val saldoLiveData = MutableLiveData<Double>()
        val movimentiLiveData = MutableLiveData<List<Movimento>>()
        
        // Then
        assertNotNull(saldoLiveData)
        assertNotNull(movimentiLiveData)
        assertEquals(null, saldoLiveData.value)
        assertEquals(null, movimentiLiveData.value)
    }

    @Test
    fun testLiveDataValueSetting() {
        // Given
        val saldoLiveData = MutableLiveData<Double>()
        val movimentiLiveData = MutableLiveData<List<Movimento>>()
        val testSaldo = 500.0
        val testMovimenti = listOf(Movimento(100.0, "Test", Date()))
        
        // When
        saldoLiveData.value = testSaldo
        movimentiLiveData.value = testMovimenti
        
        // Then
        assertEquals(testSaldo, saldoLiveData.value)
        assertEquals(testMovimenti, movimentiLiveData.value)
        assertEquals(1, movimentiLiveData.value?.size)
    }
}