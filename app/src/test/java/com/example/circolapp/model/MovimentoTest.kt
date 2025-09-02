package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date
import java.util.Calendar

/**
 * Test unitari per la classe Movimento
 */
class MovimentoTest {

    @Test
    fun `test movimento creation with positive amount`() {
        assertEquals(importo, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
        assertEquals(data, movimento.data)
    }

    @Test
    fun `test movimento creation with negative amount`() {
        assertEquals(importo, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
        assertTrue(movimento.importo < 0)
    }

    @Test
    fun `test movimento default constructor`() {
        assertEquals(0.0, movimento.importo, 0.01)
        assertEquals("", movimento.descrizione)
        assertNotNull(movimento.data)
    }

    @Test
    fun `test movimento with zero amount`() {
        assertEquals(0.0, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
    }

    @Test
    fun `test movimento data is not null by default`() {
        assertNotNull(movimento.data)
        assertTrue(movimento.data.time <= System.currentTimeMillis())
    }
}
