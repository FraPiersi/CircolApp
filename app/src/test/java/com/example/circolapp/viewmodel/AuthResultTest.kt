package com.example.circolapp.viewmodel

import org.junit.Test
import org.junit.Assert.*
import com.example.circolapp.model.UserRole
import com.google.firebase.auth.FirebaseUser
import org.mockito.kotlin.mock

/**
 * Test unitari per AuthResult sealed class e validazioni di autenticazione
 */
class AuthResultTest {

    @Test
    fun `test AuthResult Success creation`() {
        assertEquals(userRole, result.userRole)
        assertEquals(mockFirebaseUser, result.user)
    }

    @Test
    fun `test AuthResult Error creation`() {
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `test AuthResult Loading and Idle states`() {

        assertNotNull(loadingResult)
        assertNotNull(idleResult)

        // Test che sono diversi tra loro
        assertNotEquals(loadingResult, idleResult)

        // Test più semplice per evitare warning del compilatore
        assertEquals(AuthResult.Loading, loadingResult)
        assertEquals(AuthResult.Idle, idleResult)
    }

    @Test
    fun `test email validation logic`() {
        // Test per logica di validazione email (simula il comportamento del ViewModel)

        // Valid emails
        assertTrue(isValidEmail("test@example.com"))
        assertTrue(isValidEmail("user.name@domain.co.uk"))

        // Invalid emails
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("invalid-email"))
        assertFalse(isValidEmail("@domain.com"))
        assertFalse(isValidEmail("user@"))
    }

    @Test
    fun `test password validation logic`() {
        // Test per logica di validazione password

        // Valid passwords
        assertTrue(isValidPassword("password123"))
        assertTrue(isValidPassword("mySecurePass"))

        // Invalid passwords
        assertFalse(isValidPassword(""))
        assertFalse(isValidPassword("   "))
        assertFalse(isValidPassword("123")) // troppo corta
    }

    @Test
    fun `test user role mapping from string`() {
        // Test per mappatura ruoli da stringa
        assertEquals(UserRole.ADMIN, mapStringToUserRole("admin"))
        assertEquals(UserRole.ADMIN, mapStringToUserRole("ADMIN"))
        assertEquals(UserRole.USER, mapStringToUserRole("user"))
        assertEquals(UserRole.USER, mapStringToUserRole("USER"))
        assertEquals(UserRole.USER, mapStringToUserRole("unknown"))
        assertEquals(UserRole.USER, mapStringToUserRole(null))
    }

    // Funzioni di utilità che simulano la logica del ViewModel
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        // Semplice regex per validazione email (senza dipendenza da Android)
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.trim().length >= 6
    }

    private fun mapStringToUserRole(roleString: String?): UserRole {
        return when (roleString?.lowercase()) {
            UserRole.ADMIN.name.lowercase() -> UserRole.ADMIN
            UserRole.USER.name.lowercase() -> UserRole.USER
            else -> UserRole.USER
        }
    }
}
