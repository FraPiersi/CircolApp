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
        // Arrange
        val importo = 50.75
        val descrizione = "Ricarica in cassa"
        val data = Date()

        // Act
        val movimento = Movimento(
            importo = importo,
            descrizione = descrizione,
            data = data
        )

        // Assert
        assertEquals(importo, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
        assertEquals(data, movimento.data)
    }

    @Test
    fun `test movimento creation with negative amount`() {
        // Arrange
        val importo = -25.50
        val descrizione = "Pagamento in cassa"

        // Act
        val movimento = Movimento(
            importo = importo,
            descrizione = descrizione
        )

        // Assert
        assertEquals(importo, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
        assertTrue(movimento.importo < 0)
    }

    @Test
    fun `test movimento default constructor`() {
        // Act
        val movimento = Movimento()

        // Assert
        assertEquals(0.0, movimento.importo, 0.01)
        assertEquals("", movimento.descrizione)
        assertNotNull(movimento.data)
    }

    @Test
    fun `test movimento with zero amount`() {
        // Arrange
        val descrizione = "Movimento a zero"

        // Act
        val movimento = Movimento(
            importo = 0.0,
            descrizione = descrizione
        )

        // Assert
        assertEquals(0.0, movimento.importo, 0.01)
        assertEquals(descrizione, movimento.descrizione)
    }

    @Test
    fun `test movimento data is not null by default`() {
        // Act
        val movimento = Movimento(
            importo = 100.0,
            descrizione = "Test"
        )

        // Assert
        assertNotNull(movimento.data)
        assertTrue(movimento.data.time <= System.currentTimeMillis())
    }
}
