package com.example.examencierre.data.repository

import com.example.examencierre.data.local.dao.UserDao
import com.example.examencierre.data.local.entity.UserEntity

/**
 * Implementación en memoria del DAO de usuarios para las pruebas unitarias.
 */
class FakeUserDao : UserDao {

    private val users = linkedMapOf<Long, UserEntity>()
    private var nextId = 1L

    override suspend fun insert(user: UserEntity): Long {
        val id = nextId++
        users[id] = user.copy(id = id)
        return id
    }

    override suspend fun findByCorreo(correo: String): UserEntity? =
        users.values.firstOrNull { it.correo == correo }

    override suspend fun existsByCorreo(correo: String): Boolean =
        users.values.any { it.correo == correo }

    override suspend fun findById(id: Long): UserEntity? = users[id]

    override suspend fun getAll(): List<UserEntity> = users.values.toList()
}
