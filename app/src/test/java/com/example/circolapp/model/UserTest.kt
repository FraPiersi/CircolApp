package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date
import java.util.Calendar

/**
 * Test unitari per la classe User
 */
class UserTest {

    @Test
    fun `test user creation with complete data`() {
        // Arrange
        val uid = "user123"
        val username = "testuser"
        val nome = "Mario Rossi"
        val saldo = 50.75
        val photoUrl = "https://example.com/photo.jpg"
        val numeroTessera = "CARD001"
        val dataScadenza = Date()

        // Act
        val user = User(
            uid = uid,
            username = username,
            nome = nome,
            saldo = saldo,
            photoUrl = photoUrl,
            hasTessera = true,
            numeroTessera = numeroTessera,
            dataScadenzaTessera = dataScadenza
        )

        // Assert
        assertEquals(uid, user.uid)
        assertEquals(username, user.username)
        assertEquals(nome, user.nome)
        assertEquals(saldo, user.saldo, 0.01)
        assertEquals(photoUrl, user.photoUrl)
        assertTrue(user.hasTessera)
        assertEquals(numeroTessera, user.numeroTessera)
        assertEquals(dataScadenza, user.dataScadenzaTessera)
        assertFalse(user.richiestaRinnovoInCorso)
    }

    @Test
    fun `test user default constructor`() {
        // Act
        val user = User()

        // Assert
        assertEquals("", user.uid)
        assertEquals("", user.username)
        assertEquals("", user.nome)
        assertEquals(0.0, user.saldo, 0.01)
        assertNull(user.photoUrl)
        assertFalse(user.hasTessera)
        assertNull(user.numeroTessera)
        assertNull(user.dataScadenzaTessera)
        assertFalse(user.richiestaRinnovoInCorso)
    }

    @Test
    fun `test user with expired card`() {
        // Arrange
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time

        // Act
        val user = User(
            uid = "user123",
            hasTessera = true,
            numeroTessera = "CARD001",
            dataScadenzaTessera = yesterday
        )

        // Assert
        assertTrue(user.hasTessera)
        assertTrue(user.dataScadenzaTessera!!.before(Date()))
    }

    @Test
    fun `test user balance operations`() {
        // Arrange
        val initialBalance = 100.0
        val user = User(uid = "test", saldo = initialBalance)

        // Act & Assert
        assertEquals(initialBalance, user.saldo, 0.01)

        // Simuliamo operazioni sul saldo (in realtà andrebbero fatte tramite repository)
        val newBalance = user.saldo - 25.50
        assertEquals(74.50, newBalance, 0.01)
    }

    @Test
    fun `test user with renewal request in progress`() {
        // Act
        val user = User(
            uid = "test",
            hasTessera = true,
            richiestaRinnovoInCorso = true
        )

        // Assert
        assertTrue(user.hasTessera)
        assertTrue(user.richiestaRinnovoInCorso)
    }
}
