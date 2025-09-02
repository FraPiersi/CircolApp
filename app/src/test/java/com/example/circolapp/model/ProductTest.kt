package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitari per la classe Product
 */
class ProductTest {

    @Test
    fun `test product creation with valid data`() {
        // Arrange
        val id = "test123"
        val nome = "Prodotto Test"
        val descrizione = "Descrizione del prodotto"
        val numeroPezzi = 10
        val importo = 25.50
        val ordinabile = true

        // Act
        val product = Product(
            id = id,
            nome = nome,
            descrizione = descrizione,
            numeroPezzi = numeroPezzi,
            importo = importo,
            ordinabile = ordinabile
        )

        // Assert
        assertEquals(id, product.id)
        assertEquals(nome, product.nome)
        assertEquals(descrizione, product.descrizione)
        assertEquals(numeroPezzi, product.numeroPezzi)
        assertEquals(importo, product.importo, 0.01)
        assertTrue(product.ordinabile)
    }

    @Test
    fun `test product default constructor`() {
        // Act
        val product = Product()

        // Assert
        assertEquals("", product.id)
        assertEquals("", product.nome)
        assertEquals("", product.descrizione)
        assertEquals(0, product.numeroPezzi)
        assertEquals(0.0, product.importo, 0.01)
        assertNull(product.imageUrl)
        assertTrue(product.ordinabile)
    }

    @Test
    fun `test product with negative price should maintain value`() {
        // Arrange & Act
        val product = Product(
            id = "test",
            nome = "Test",
            descrizione = "Test",
            numeroPezzi = 5,
            importo = -10.0
        )

        // Assert
        assertEquals(-10.0, product.importo, 0.01)
    }

    @Test
    fun `test product availability when stock is zero`() {
        // Arrange & Act
        val product = Product(
            id = "test",
            nome = "Test",
            numeroPezzi = 0,
            ordinabile = true
        )

        // Assert
        assertEquals(0, product.numeroPezzi)
        assertTrue(product.ordinabile) // Il prodotto può essere ordinabile anche senza stock
    }
}
