package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitari per la classe Product
 */
class ProductTest {

    @Test
    fun `test product creation with valid data`() {
        
        val id = "test123"
        val nome = "Prodotto Test"
        val descrizione = "Descrizione del prodotto"
        val numeroPezzi = 10
        val importo = 25.50
        val ordinabile = true

        
        val product = Product(
            id = id,
            nome = nome,
            descrizione = descrizione,
            numeroPezzi = numeroPezzi,
            importo = importo,
            ordinabile = ordinabile
        )

        
        assertEquals(id, product.id)
        assertEquals(nome, product.nome)
        assertEquals(descrizione, product.descrizione)
        assertEquals(numeroPezzi, product.numeroPezzi)
        assertEquals(importo, product.importo, 0.01)
        assertTrue(product.ordinabile)
    }

    @Test
    fun `test product default constructor`() {
        
        val product = Product()

        
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
         & Act
        val product = Product(
            id = "test",
            nome = "Test",
            descrizione = "Test",
            numeroPezzi = 5,
            importo = -10.0
        )

        
        assertEquals(-10.0, product.importo, 0.01)
    }

    @Test
    fun `test product availability when stock is zero`() {
         & Act
        val product = Product(
            id = "test",
            nome = "Test",
            numeroPezzi = 0,
            ordinabile = true
        )

        
        assertEquals(0, product.numeroPezzi)
        assertTrue(product.ordinabile) // Il prodotto può essere ordinabile anche senza stock
    }
}
