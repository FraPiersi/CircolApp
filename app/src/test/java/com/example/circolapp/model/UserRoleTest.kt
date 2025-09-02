package com.example.circolapp.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitari per l'enum UserRole
 */
class UserRoleTest {

    @Test
    fun `test user role enum values`() {
        // Act & Assert
        assertEquals(3, UserRole.values().size)
        assertTrue(UserRole.values().contains(UserRole.USER))
        assertTrue(UserRole.values().contains(UserRole.ADMIN))
        assertTrue(UserRole.values().contains(UserRole.UNKNOWN))
    }

    @Test
    fun `test user role string representation`() {
        // Act & Assert
        assertEquals("USER", UserRole.USER.name)
        assertEquals("ADMIN", UserRole.ADMIN.name)
        assertEquals("UNKNOWN", UserRole.UNKNOWN.name)
    }

    @Test
    fun `test user role valueOf`() {
        // Act & Assert
        assertEquals(UserRole.USER, UserRole.valueOf("USER"))
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"))
        assertEquals(UserRole.UNKNOWN, UserRole.valueOf("UNKNOWN"))
    }

    @Test
    fun `test user role comparison`() {
        // Act & Assert
        assertEquals(UserRole.USER, UserRole.USER)
        assertNotEquals(UserRole.USER, UserRole.ADMIN)
        assertNotEquals(UserRole.ADMIN, UserRole.UNKNOWN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test invalid user role throws exception`() {
        // Act
        UserRole.valueOf("INVALID_ROLE")
    }
}
