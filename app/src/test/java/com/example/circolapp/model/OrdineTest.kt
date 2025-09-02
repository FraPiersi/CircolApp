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
        assertEquals(uidUtente, ordine.uidUtente)
        assertEquals(nomeProdotto, ordine.nomeProdotto)
        assertEquals(prodottoId, ordine.prodottoId)
        assertEquals(richiesteAggiuntive, ordine.richiesteAggiuntive)
        assertEquals(timestamp, ordine.timestamp)
        assertEquals(stato, ordine.stato)
    }

    @Test
    fun `test ordine default constructor`() {
        assertEquals("", ordine.uidUtente)
        assertEquals("", ordine.nomeProdotto)
        assertEquals("", ordine.prodottoId)
        assertNull(ordine.richiesteAggiuntive)
        assertNull(ordine.timestamp)
        assertEquals("INVIATO", ordine.stato)
    }

    @Test
    fun `test ordine state transitions`() {
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
        assertNull(ordine.richiesteAggiuntive)
        assertEquals("INVIATO", ordine.stato)
    }

    @Test
    fun `test ordine validation`() {
        // Test per validazione ordine
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
