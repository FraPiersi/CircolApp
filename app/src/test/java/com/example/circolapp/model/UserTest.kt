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
        val uid = "user123"
        val username = "testuser"
        val nome = "Mario Rossi"
        val saldo = 50.75
        val photoUrl = "https://example.com/photo.jpg"
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
        assertTrue(user.hasTessera)
        assertTrue(user.dataScadenzaTessera!!.before(Date()))
    }

    @Test
    fun `test user balance operations`() {
        assertEquals(initialBalance, user.saldo, 0.01)

        // Simuliamo operazioni sul saldo (in realtà andrebbero fatte tramite repository)
        assertTrue(user.hasTessera)
        assertTrue(user.richiestaRinnovoInCorso)
    }
}
