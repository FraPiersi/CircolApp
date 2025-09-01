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
        
        val importo = 50.75
        val descrizione = "Ricarica in cassa"
        val data = Date()

        
        val movimento = Movimento(
            importo = importo,
            descrizione = descrizione,
            data = data
        )

        
        assertEquals(importo, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
        assertEquals(data, movimento.data)
    }

    @Test
    fun `test movimento creation with negative amount`() {
        
        val importo = -25.50
        val descrizione = "Pagamento in cassa"

        
        val movimento = Movimento(
            importo = importo,
            descrizione = descrizione
        )

        
        assertEquals(importo, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
        assertTrue(movimento.importo < 0)
    }

    @Test
    fun `test movimento default constructor`() {
        
        val movimento = Movimento()

        
        assertEquals(0.0, movimento.importo, 0.01)
        assertEquals("", movimento.descrizione)
        assertNotNull(movimento.data)
    }

    @Test
    fun `test movimento with zero amount`() {
        
        val descrizione = "Movimento a zero"

        
        val movimento = Movimento(
            importo = 0.0,
            descrizione = descrizione
        )

        
        assertEquals(0.0, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
    }

    @Test
    fun `test movimento data is not null by default`() {
        
        val movimento = Movimento(
            importo = 100.0,
            descrizione = "Test"
        )

        
        assertNotNull(movimento.data)
        assertTrue(movimento.data.time <= System.currentTimeMillis())
    }
}
