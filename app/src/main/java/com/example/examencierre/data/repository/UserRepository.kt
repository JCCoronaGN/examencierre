package com.example.examencierre.data.repository

import com.example.examencierre.data.local.dao.UserDao
import com.example.examencierre.data.local.entity.UserEntity
import com.example.examencierre.data.security.HexCodec
import com.example.examencierre.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AuthError {
    EMAIL_ALREADY_REGISTERED,
    INVALID_CREDENTIALS,
    INVALID_DATA,
    DATABASE_ERROR
}

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

class UserRepository(private val userDao: UserDao) {

    suspend fun register(
        nombre: String,
        apellidos: String,
        correo: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = correo.trim().lowercase()
            if (userDao.existsByCorreo(normalizedEmail)) {
                return@withContext AuthResult.Failure(AuthError.EMAIL_ALREADY_REGISTERED)
            }
            val salt = PasswordHasher.newSalt()
            val hash = PasswordHasher.hash(password, salt)
            val id = userDao.insert(
                UserEntity(
                    nombre = nombre.trim(),
                    apellidos = apellidos.trim(),
                    correo = normalizedEmail,
                    passwordHash = HexCodec.encode(hash),
                    passwordSalt = HexCodec.encode(salt)
                )
            )
            AuthResult.Success(
                UserEntity(
                    id = id,
                    nombre = nombre.trim(),
                    apellidos = apellidos.trim(),
                    correo = normalizedEmail,
                    passwordHash = HexCodec.encode(hash),
                    passwordSalt = HexCodec.encode(salt)
                )
            )
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.DATABASE_ERROR)
        }
    }

    suspend fun login(correo: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val user = userDao.findByCorreo(correo.trim().lowercase())
                    ?: return@withContext AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
                val valid = PasswordHasher.verify(
                    password = password,
                    salt = HexCodec.decode(user.passwordSalt),
                    expectedHash = HexCodec.decode(user.passwordHash)
                )
                if (valid) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
                }
            } catch (e: Exception) {
                AuthResult.Failure(AuthError.DATABASE_ERROR)
            }
        }

    suspend fun findById(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        runCatching { userDao.findById(id) }.getOrNull()
    }
}
