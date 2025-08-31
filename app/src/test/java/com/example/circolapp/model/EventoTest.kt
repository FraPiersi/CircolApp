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
        // Arrange
        val id = "evento123"
        val nome = "Torneo di Calcio"
        val descrizione = "Torneo interno del circolo"
        val data = Date()
        val luogo = "Campo Sportivo"
        val partecipanti = listOf("user1", "user2", "user3")

        // Act
        val evento = Evento(
            id = id,
            nome = nome,
            descrizione = descrizione,
            data = data,
            luogo = luogo,
            partecipanti = partecipanti
        )

        // Assert
        assertEquals(id, evento.id)
        assertEquals(nome, evento.nome)
        assertEquals(descrizione, evento.descrizione)
        assertEquals(data, evento.data)
        assertEquals(luogo, evento.luogo)
        assertEquals(partecipanti, evento.partecipanti)
    }

    @Test
    fun `test evento default constructor`() {
        // Act
        val evento = Evento()

        // Assert
        assertEquals("", evento.id)
        assertEquals("", evento.nome)
        assertEquals("", evento.descrizione)
        assertNull(evento.data)
        assertEquals("", evento.luogo)
        assertTrue(evento.partecipanti.isEmpty())
    }

    @Test
    fun `test evento with future date`() {
        // Arrange
        val futureDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 7)
        }.time

        // Act
        val evento = Evento(
            id = "test",
            nome = "Evento Futuro",
            data = futureDate
        )

        // Assert
        assertTrue(evento.data!!.after(Date()))
    }

    @Test
    fun `test evento with past date`() {
        // Arrange
        val pastDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -7)
        }.time

        // Act
        val evento = Evento(
            id = "test",
            nome = "Evento Passato",
            data = pastDate
        )

        // Assert
        assertTrue(evento.data!!.before(Date()))
    }

    @Test
    fun `test evento without participants`() {
        // Act
        val evento = Evento(
            id = "test",
            nome = "Evento Senza Partecipanti"
        )

        // Assert
        assertTrue(evento.partecipanti.isEmpty())
    }
}
