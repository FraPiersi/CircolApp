package com.example.circolapp.business

import org.junit.Test
import org.junit.Assert.*
import com.example.circolapp.model.User
import com.example.circolapp.model.Product
import com.example.circolapp.model.Movimento
import java.util.Date

/**
 * Test unitari per la logica di business delle transazioni
 */
class TransactionLogicTest {

    @Test
    fun `test successful payment with sufficient balance`() {
        
        val user = User(
            uid = "user123",
            username = "testuser",
            saldo = 100.0
        )
        val product = Product(
            id = "prod123",
            nome = "Test Product",
            importo = 25.0
        )

        
        val result = processPayment(user, product.importo)

        
        assertTrue(result.isSuccess)
        assertEquals(75.0, result.newBalance, 0.01)
    }

    @Test
    fun `test payment failure with insufficient balance`() {
        
        val user = User(
            uid = "user123",
            username = "testuser",
            saldo = 10.0
        )
        val amount = 25.0

        
        val result = processPayment(user, amount)

        
        assertFalse(result.isSuccess)
        assertEquals("Saldo insufficiente", result.errorMessage)
        assertEquals(10.0, result.newBalance, 0.01) // Saldo invariato
    }

    @Test
    fun `test recharge transaction`() {
        
        val user = User(
            uid = "user123",
            saldo = 50.0
        )
        val rechargeAmount = 25.0

        
        val result = processRecharge(user, rechargeAmount)

        
        assertTrue(result.isSuccess)
        assertEquals(75.0, result.newBalance, 0.01)
    }

    @Test
    fun `test invalid recharge with negative amount`() {
        
        val user = User(uid = "user123", saldo = 50.0)

        
        val result = processRecharge(user, -10.0)

        
        assertFalse(result.isSuccess)
        assertEquals("Importo non valido", result.errorMessage)
    }

    @Test
    fun `test movimento creation for payment`() {
        
        val amount = 25.0
        val description = "Pagamento prodotto"

        
        val movimento = createPaymentMovimento(amount, description)

        
        assertEquals(-25.0, movimento.importo, 0.01) // Negativo per pagamento
        assertEquals(description, movimento.descrizione)
        assertNotNull(movimento.data)
    }

    @Test
    fun `test movimento creation for recharge`() {
        
        val amount = 50.0
        val description = "Ricarica in cassa"

        
        val movimento = createRechargeMovimento(amount, description)

        
        assertEquals(50.0, movimento.importo, 0.01) // Positivo per ricarica
        assertEquals(description, movimento.descrizione)
        assertNotNull(movimento.data)
    }

    @Test
    fun `test product stock validation`() {
        
        val availableProduct = Product(
            id = "prod1",
            nome = "Available Product",
            numeroPezzi = 5
        )

        val outOfStockProduct = Product(
            id = "prod2",
            nome = "Out of Stock Product",
            numeroPezzi = 0
        )

        
        assertTrue(isProductAvailable(availableProduct))
        assertFalse(isProductAvailable(outOfStockProduct))
    }

    @Test
    fun `test tessera payment logic`() {
        
        val user = User(uid = "user123", saldo = 50.0)
        val tesseraPrice = 30.0

        
        val canPay = canPayForTessera(user, tesseraPrice)
        val result = processTesseraPayment(user, tesseraPrice)

        
        assertTrue(canPay)
        assertTrue(result.isSuccess)
        assertEquals(20.0, result.newBalance, 0.01)
    }

    // Data classes per i risultati
    data class TransactionResult(
        val isSuccess: Boolean,
        val newBalance: Double,
        val errorMessage: String = ""
    )

    // Funzioni di business logic simulate
    private fun processPayment(user: User, amount: Double): TransactionResult {
        return if (user.saldo >= amount) {
            TransactionResult(true, user.saldo - amount)
        } else {
            TransactionResult(false, user.saldo, "Saldo insufficiente")
        }
    }

    private fun processRecharge(user: User, amount: Double): TransactionResult {
        return if (amount > 0) {
            TransactionResult(true, user.saldo + amount)
        } else {
            TransactionResult(false, user.saldo, "Importo non valido")
        }
    }

    private fun createPaymentMovimento(amount: Double, description: String): Movimento {
        return Movimento(-amount, description, Date())
    }

    private fun createRechargeMovimento(amount: Double, description: String): Movimento {
        return Movimento(amount, description, Date())
    }

    private fun isProductAvailable(product: Product): Boolean {
        return product.numeroPezzi > 0
    }

    private fun canPayForTessera(user: User, price: Double): Boolean {
        return user.saldo >= price
    }

    private fun processTesseraPayment(user: User, price: Double): TransactionResult {
        return processPayment(user, price)
    }
}
