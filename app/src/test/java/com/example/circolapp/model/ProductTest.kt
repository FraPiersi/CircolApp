package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitari per la classe Product
 */
class ProductTest {

    @Test
    fun `test product creation with valid data`() {
        assertEquals(id, product.id)
        assertEquals(nome, product.nome)
        assertEquals(descrizione, product.descrizione)
        assertEquals(numeroPezzi, product.numeroPezzi)
        assertEquals(importo, product.importo, 0.01)
        assertTrue(product.ordinabile)
    }

    @Test
    fun `test product default constructor`() {
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
        assertEquals(-10.0, product.importo, 0.01)
    }

    @Test
    fun `test product availability when stock is zero`() {
 & Act
        assertEquals(0, product.numeroPezzi)
        assertTrue(product.ordinabile)    }
}
