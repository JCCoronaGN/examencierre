package com.example.examencierre.data.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `hash is deterministic for the same salt and password`() {
        val salt = PasswordHasher.newSalt()

        val first = PasswordHasher.hash("secreto123", salt)
        val second = PasswordHasher.hash("secreto123", salt)

        assertArrayEquals(first, second)
    }

    @Test
    fun `hash differs when the salt differs`() {
        val first = PasswordHasher.hash("secreto123", PasswordHasher.newSalt())
        val second = PasswordHasher.hash("secreto123", PasswordHasher.newSalt())

        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun `hash differs when the password differs`() {
        val salt = PasswordHasher.newSalt()

        val first = PasswordHasher.hash("secreto123", salt)
        val second = PasswordHasher.hash("secreto124", salt)

        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun `verify accepts the original password`() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("secreto123", salt)

        assertTrue(PasswordHasher.verify("secreto123", salt, hash))
    }

    @Test
    fun `verify rejects a wrong password`() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("secreto123", salt)

        assertFalse(PasswordHasher.verify("otraClave", salt, hash))
    }

    @Test
    fun `newSalt generates different values`() {
        assertNotEquals(
            PasswordHasher.newSalt().toList(),
            PasswordHasher.newSalt().toList()
        )
    }
}
