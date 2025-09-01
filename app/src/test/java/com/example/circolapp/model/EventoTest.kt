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
        
        val id = "evento123"
        val nome = "Torneo di Calcio"
        val descrizione = "Torneo interno del circolo"
        val data = Date()
        val luogo = "Campo Sportivo"
        val partecipanti = listOf("user1", "user2", "user3")

        
        val evento = Evento(
            id = id,
            nome = nome,
            descrizione = descrizione,
            data = data,
            luogo = luogo,
            partecipanti = partecipanti
        )

        
        assertEquals(id, evento.id)
        assertEquals(nome, evento.nome)
        assertEquals(descrizione, evento.descrizione)
        assertEquals(data, evento.data)
        assertEquals(luogo, evento.luogo)
        assertEquals(partecipanti, evento.partecipanti)
    }

    @Test
    fun `test evento default constructor`() {
        
        val evento = Evento()

        
        assertEquals("", evento.id)
        assertEquals("", evento.nome)
        assertEquals("", evento.descrizione)
        assertNull(evento.data)
        assertEquals("", evento.luogo)
        assertTrue(evento.partecipanti.isEmpty())
    }

    @Test
    fun `test evento with future date`() {
        
        val futureDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 7)
        }.time

        
        val evento = Evento(
            id = "test",
            nome = "Evento Futuro",
            data = futureDate
        )

        
        assertTrue(evento.data!!.after(Date()))
    }

    @Test
    fun `test evento with past date`() {
        
        val pastDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -7)
        }.time

        
        val evento = Evento(
            id = "test",
            nome = "Evento Passato",
            data = pastDate
        )

        
        assertTrue(evento.data!!.before(Date()))
    }

    @Test
    fun `test evento without participants`() {
        
        val evento = Evento(
            id = "test",
            nome = "Evento Senza Partecipanti"
        )

        
        assertTrue(evento.partecipanti.isEmpty())
    }
}
