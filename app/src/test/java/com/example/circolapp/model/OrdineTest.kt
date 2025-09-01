package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * Test unitari per la classe Ordine
 */
class OrdineTest {

    @Test
    fun `test ordine creation with complete data`() {
        
        val uidUtente = "user123"
        val nomeProdotto = "Pizza Margherita"
        val prodottoId = "prod456"
        val richiesteAggiuntive = "Senza mozzarella"
        val timestamp = Date()
        val stato = "INVIATO"

        
        val ordine = Ordine(
            uidUtente = uidUtente,
            nomeProdotto = nomeProdotto,
            prodottoId = prodottoId,
            richiesteAggiuntive = richiesteAggiuntive,
            timestamp = timestamp,
            stato = stato
        )

        
        assertEquals(uidUtente, ordine.uidUtente)
        assertEquals(nomeProdotto, ordine.nomeProdotto)
        assertEquals(prodottoId, ordine.prodottoId)
        assertEquals(richiesteAggiuntive, ordine.richiesteAggiuntive)
        assertEquals(timestamp, ordine.timestamp)
        assertEquals(stato, ordine.stato)
    }

    @Test
    fun `test ordine default constructor`() {
        
        val ordine = Ordine()

        
        assertEquals("", ordine.uidUtente)
        assertEquals("", ordine.nomeProdotto)
        assertEquals("", ordine.prodottoId)
        assertNull(ordine.richiesteAggiuntive)
        assertNull(ordine.timestamp)
        assertEquals("INVIATO", ordine.stato)
    }

    @Test
    fun `test ordine state transitions`() {
        
        var ordine = Ordine(
            uidUtente = "user123",
            nomeProdotto = "Test Product",
            stato = "INVIATO"
        )

         - Test state progression
        assertEquals("INVIATO", ordine.stato)

        ordine.stato = "IN PREPARAZIONE"
        assertEquals("IN PREPARAZIONE", ordine.stato)

        ordine.stato = "PRONTO"
        assertEquals("PRONTO", ordine.stato)

        ordine.stato = "CONSEGNATO"
        assertEquals("CONSEGNATO", ordine.stato)
    }

    @Test
    fun `test ordine without additional requests`() {
        
        val ordine = Ordine(
            uidUtente = "user123",
            nomeProdotto = "Simple Product",
            prodottoId = "prod789"
        )

        
        assertNull(ordine.richiesteAggiuntive)
        assertEquals("INVIATO", ordine.stato)
    }

    @Test
    fun `test ordine validation`() {
        
        assertTrue(isValidOrdine("user123", "Product", "prod123"))
        assertFalse(isValidOrdine("", "Product", "prod123")) // utente vuoto
        assertFalse(isValidOrdine("user123", "", "prod123")) // prodotto vuoto
        assertFalse(isValidOrdine("user123", "Product", "")) // id prodotto vuoto
    }

    // Funzione di utilità per validazione
    private fun isValidOrdine(uidUtente: String, nomeProdotto: String, prodottoId: String): Boolean {
        return uidUtente.isNotBlank() && nomeProdotto.isNotBlank() && prodottoId.isNotBlank()
    }
}
