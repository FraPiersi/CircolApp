package com.example.circolapp

import com.example.circolapp.model.Movimento
import org.junit.Test
import java.text.NumberFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test unitari per la logica di business di HomeFragment
 * Questi test non dipendono da Android Framework e possono essere eseguiti velocemente
 */
class HomeFragmentLogicTest {

    @Test
    fun testCurrencyFormattingItalian() {
        // Given
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
        
        // When & Then
        assertEquals("0,00 €", currencyFormatter.format(0.0))
        assertEquals("100,50 €", currencyFormatter.format(100.50))
        assertEquals("1.234,56 €", currencyFormatter.format(1234.56))
        assertEquals("-50,25 €", currencyFormatter.format(-50.25))
    }

    @Test
    fun testMovimentoCreation() {
        // Given
        val importo = 125.50
        val descrizione = "Pagamento test"
        val data = Date()
        
        // When
        val movimento = Movimento(importo, descrizione, data)
        
        // Then
        assertEquals(importo, movimento.importo)
        assertEquals(descrizione, movimento.descrizione)
        assertEquals(data, movimento.data)
    }

    @Test
    fun testMovimentoDefaultValues() {
        // When
        val movimento = Movimento()
        
        // Then
        assertEquals(0.0, movimento.importo)
        assertEquals("", movimento.descrizione)
        assertTrue(movimento.data != null)
    }

    @Test
    fun testMovimentiListOperations() {
        // Given
        val movimenti = listOf(
            Movimento(100.0, "Entrata", Date()),
            Movimento(-50.0, "Uscita", Date()),
            Movimento(25.75, "Bonus", Date())
        )
        
        // When
        val totalAmount = movimenti.sumOf { it.importo }
        val entrateCount = movimenti.count { it.importo > 0 }
        val usciteCount = movimenti.count { it.importo < 0 }
        
        // Then
        assertEquals(75.75, totalAmount)
        assertEquals(2, entrateCount)
        assertEquals(1, usciteCount)
        assertEquals(3, movimenti.size)
        assertFalse(movimenti.isEmpty())
    }

    @Test
    fun testEmptyMovimentiList() {
        // Given
        val movimenti = emptyList<Movimento>()
        
        // When
        val totalAmount = movimenti.sumOf { it.importo }
        
        // Then
        assertEquals(0.0, totalAmount)
        assertEquals(0, movimenti.size)
        assertTrue(movimenti.isEmpty())
    }

    @Test
    fun testNullSafeMovimentiHandling() {
        // Given
        val movimenti: List<Movimento>? = null
        
        // When
        val safeMovimenti = movimenti ?: emptyList()
        val isNullOrEmpty = movimenti.isNullOrEmpty()
        
        // Then
        assertTrue(isNullOrEmpty)
        assertEquals(0, safeMovimenti.size)
        assertTrue(safeMovimenti.isEmpty())
    }

    @Test
    fun testSaldoValidation() {
        // Given
        val saldoPositivo = 1000.0
        val saldoNegativo = -500.0
        val saldoZero = 0.0
        
        // When & Then
        assertTrue(saldoPositivo > 0)
        assertTrue(saldoNegativo < 0)
        assertTrue(saldoZero == 0.0)
        
        // Test formatting
        val formatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
        assertTrue(formatter.format(saldoPositivo).contains("€"))
        assertTrue(formatter.format(saldoNegativo).contains("-"))
        assertTrue(formatter.format(saldoZero).contains("0,00"))
    }

    @Test
    fun testMovimentoFiltering() {
        // Given
        val movimenti = listOf(
            Movimento(100.0, "Stipendio", Date()),
            Movimento(-30.0, "Spesa", Date()),
            Movimento(50.0, "Bonus", Date()),
            Movimento(-20.0, "Benzina", Date()),
            Movimento(0.0, "Neutro", Date())
        )
        
        // When
        val entrate = movimenti.filter { it.importo > 0 }
        val uscite = movimenti.filter { it.importo < 0 }
        val neutri = movimenti.filter { it.importo == 0.0 }
        
        // Then
        assertEquals(2, entrate.size)
        assertEquals(2, uscite.size)
        assertEquals(1, neutri.size)
        assertEquals(150.0, entrate.sumOf { it.importo })
        assertEquals(-50.0, uscite.sumOf { it.importo })
    }

    @Test
    fun testMovimentoSorting() {
        // Given
        val date1 = Date(1000)
        val date2 = Date(2000)
        val date3 = Date(3000)
        
        val movimenti = listOf(
            Movimento(100.0, "Secondo", date2),
            Movimento(200.0, "Terzo", date3),
            Movimento(50.0, "Primo", date1)
        )
        
        // When
        val sortedByDate = movimenti.sortedBy { it.data }
        val sortedByImporto = movimenti.sortedBy { it.importo }
        
        // Then
        assertEquals("Primo", sortedByDate.first().descrizione)
        assertEquals("Terzo", sortedByDate.last().descrizione)
        assertEquals(50.0, sortedByImporto.first().importo)
        assertEquals(200.0, sortedByImporto.last().importo)
    }

    @Test
    fun testStringResourceSimulation() {
        // Simuliamo le operazioni che il fragment fa con le stringhe
        // Given
        val saldoValue = 1250.50
        val formatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
        
        // When
        val formattedSaldo = formatter.format(saldoValue)
        val saldoString = "Saldo: $formattedSaldo" // Simula getString(R.string.saldo_format, ...)
        
        // Then
        assertTrue(saldoString.contains("Saldo:"))
        assertTrue(saldoString.contains("€"))
        assertTrue(saldoString.contains("1.250"))
    }

    @Test
    fun testViewVisibilityLogic() {
        // Simuliamo la logica di visibilità delle view
        // Given
        val userLoggedIn = true
        val movimenti = listOf(Movimento(100.0, "Test", Date()))
        val isLoading = false
        
        // When
        val shouldShowProgress = isLoading
        val shouldShowNoData = userLoggedIn && !isLoading && movimenti.isEmpty()
        val shouldShowRecyclerView = userLoggedIn && !isLoading && movimenti.isNotEmpty()
        val shouldShowLoginMessage = !userLoggedIn
        
        // Then
        assertFalse(shouldShowProgress)
        assertFalse(shouldShowNoData)
        assertTrue(shouldShowRecyclerView)
        assertFalse(shouldShowLoginMessage)
    }

    @Test
    fun testViewVisibilityLogicNotLoggedIn() {
        // Given
        val userLoggedIn = false
        val movimenti = emptyList<Movimento>()
        val isLoading = false
        
        // When
        val shouldShowProgress = isLoading
        val shouldShowNoData = userLoggedIn && !isLoading && movimenti.isEmpty()
        val shouldShowRecyclerView = userLoggedIn && !isLoading && movimenti.isNotEmpty()
        val shouldShowLoginMessage = !userLoggedIn
        
        // Then
        assertFalse(shouldShowProgress)
        assertFalse(shouldShowNoData)
        assertFalse(shouldShowRecyclerView)
        assertTrue(shouldShowLoginMessage)
    }

    @Test
    fun testViewVisibilityLogicEmptyData() {
        // Given
        val userLoggedIn = true
        val movimenti = emptyList<Movimento>()
        val isLoading = false
        
        // When
        val shouldShowProgress = isLoading
        val shouldShowNoData = userLoggedIn && !isLoading && movimenti.isEmpty()
        val shouldShowRecyclerView = userLoggedIn && !isLoading && movimenti.isNotEmpty()
        val shouldShowLoginMessage = !userLoggedIn
        
        // Then
        assertFalse(shouldShowProgress)
        assertTrue(shouldShowNoData)
        assertFalse(shouldShowRecyclerView)
        assertFalse(shouldShowLoginMessage)
    }
}