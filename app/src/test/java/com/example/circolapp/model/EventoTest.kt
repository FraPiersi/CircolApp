package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date
import java.util.Calendar

/**
 * Test unitari per la classe Evento
 */
class EventoTest {

    @Test
    fun `test evento creation with complete data`() {
        assertEquals(id, evento.id)
        assertEquals(nome, evento.nome)
        assertEquals(descrizione, evento.descrizione)
        assertEquals(data, evento.data)
        assertEquals(luogo, evento.luogo)
        assertEquals(partecipanti, evento.partecipanti)
    }

    @Test
    fun `test evento default constructor`() {
        assertEquals("", evento.id)
        assertEquals("", evento.nome)
        assertEquals("", evento.descrizione)
        assertNull(evento.data)
        assertEquals("", evento.luogo)
        assertTrue(evento.partecipanti.isEmpty())
    }

    @Test
    fun `test evento with future date`() {
        assertTrue(evento.data!!.after(Date()))
    }

    @Test
    fun `test evento with past date`() {
        assertTrue(evento.data!!.before(Date()))
    }

    @Test
    fun `test evento without participants`() {
        assertTrue(evento.partecipanti.isEmpty())
    }
}
