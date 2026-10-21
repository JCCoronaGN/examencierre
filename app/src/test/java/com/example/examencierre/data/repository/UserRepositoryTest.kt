package com.example.examencierre.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryTest {

    private val userDao = FakeUserDao()
    private val repository = UserRepository(userDao)

    @Test
    fun `register creates the user and normalizes the email`() = runTest {
        val result = repository.register("Ana", "Ruiz", "  ANA@Example.COM ", "secreto123")

        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("ana@example.com", user.correo)
        assertEquals("Ana", user.nombre)
        assertTrue(user.id > 0)
    }

    @Test
    fun `register does not store the password in plain text`() = runTest {
        val result = repository.register("Ana", "Ruiz", "ana@example.com", "secreto123")

        val user = (result as AuthResult.Success).user
        assertTrue(!user.passwordHash.contains("secreto123"))
        assertTrue(user.passwordSalt.isNotEmpty())
    }

    @Test
    fun `register fails when the email is already registered`() = runTest {
        repository.register("Ana", "Ruiz", "ana@example.com", "secreto123")

        val result = repository.register("Otra", "Persona", "ana@example.com", "otraclave")

        assertEquals(
            AuthError.EMAIL_ALREADY_REGISTERED,
            (result as AuthResult.Failure).error
        )
    }

    @Test
    fun `login succeeds with the correct credentials`() = runTest {
        repository.register("Ana", "Ruiz", "ana@example.com", "secreto123")

        val result = repository.login("ana@example.com", "secreto123")

        assertTrue(result is AuthResult.Success)
        assertEquals("Ana", (result as AuthResult.Success).user.nombre)
    }

    @Test
    fun `login fails with a wrong password`() = runTest {
        repository.register("Ana", "Ruiz", "ana@example.com", "secreto123")

        val result = repository.login("ana@example.com", "miclave")

        assertEquals(AuthError.INVALID_CREDENTIALS, (result as AuthResult.Failure).error)
    }

    @Test
    fun `login fails when the user does not exist`() = runTest {
        val result = repository.login("desconocido@example.com", "secreto123")

        assertEquals(AuthError.INVALID_CREDENTIALS, (result as AuthResult.Failure).error)
    }

    @Test
    fun `findById returns the registered user`() = runTest {
        val registered = repository.register("Ana", "Ruiz", "ana@example.com", "secreto123")
        val id = (registered as AuthResult.Success).user.id

        val found = repository.findById(id)

        assertNotNull(found)
        assertEquals("ana@example.com", found?.correo)
    }
}
