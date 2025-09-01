package com.example.circolapp.utils

import org.junit.Test
import org.junit.Assert.*
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Test unitari per le utilità di validazione e formattazione
 */
class ValidationUtilsTest {

    @Test
    fun `test product validation with valid data`() {
        
        assertTrue(isValidProduct("Prodotto Test", "Descrizione", 10, 25.0))
        assertTrue(isValidProduct("P", "D", 1, 0.01))
    }

    @Test
    fun `test product validation with invalid data`() {
        
        assertFalse(isValidProduct("", "Descrizione", 10, 25.0)) // nome vuoto
        assertFalse(isValidProduct("Prodotto", "", 10, 25.0)) // descrizione vuota
        assertFalse(isValidProduct("Prodotto", "Descrizione", -1, 25.0)) // quantità negativa
        assertFalse(isValidProduct("Prodotto", "Descrizione", 10, -1.0)) // prezzo negativo
    }

    @Test
    fun `test balance calculation with movements`() {
        
        val movements = listOf(
            TestMovement(50.0, "Ricarica"),
            TestMovement(-25.0, "Pagamento"),
            TestMovement(100.0, "Ricarica"),
            TestMovement(-30.0, "Acquisto")
        )

        
        val balance = calculateBalance(movements)

        
        assertEquals(95.0, balance, 0.01)
    }

    @Test
    fun `test balance calculation with empty movements`() {
        
        val balance = calculateBalance(emptyList())

        
        assertEquals(0.0, balance, 0.01)
    }

    @Test
    fun `test date formatting`() {
        
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15, 14, 30, 0)
        val date = calendar.time

        
        val formattedDate = formatDate(date)

        
        assertEquals("15/01/2025 14:30", formattedDate)
    }

    @Test
    fun `test tessera expiration check`() {
        
        val futureDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 30)
        }.time

        val pastDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -30)
        }.time

        
        assertFalse(isTesseraExpired(futureDate))
        assertTrue(isTesseraExpired(pastDate))
        assertTrue(isTesseraExpired(null))
    }

    @Test
    fun `test price formatting`() {
        
        assertEquals("€ 25,50", formatPrice(25.5))
        assertEquals("€ 0,00", formatPrice(0.0))
        assertEquals("€ 1.234,56", formatPrice(1234.56))
    }

    
    data class TestMovement(val importo: Double, val descrizione: String)

    private fun isValidProduct(nome: String, descrizione: String, quantita: Int, prezzo: Double): Boolean {
        return nome.isNotBlank() && descrizione.isNotBlank() && quantita >= 0 && prezzo >= 0
    }

    private fun calculateBalance(movements: List<TestMovement>): Double {
        return movements.sumOf { it.importo }
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN)
        return formatter.format(date)
    }

    private fun isTesseraExpired(dataScadenza: Date?): Boolean {
        return dataScadenza?.before(Date()) ?: true
    }

    private fun formatPrice(price: Double): String {
        return String.format(Locale.ITALIAN, "€ %,.2f", price)
    }
}
