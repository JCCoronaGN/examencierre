package com.example.examencierre.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash de contraseñas usando PBKDF2-HMAC-SHA256 con salt aleatorio por usuario.
 * Las contraseñas nunca se almacenan en texto plano.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH_BITS = 256
    private const val ITERATIONS = 120_000
    const val SALT_LENGTH_BYTES = 16

    fun newSalt(): ByteArray = ByteArray(SALT_LENGTH_BYTES).also {
        SecureRandom().nextBytes(it)
    }

    fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    fun verify(password: String, salt: ByteArray, expectedHash: ByteArray): Boolean =
        MessageDigest.isEqual(hash(password, salt), expectedHash)
}
