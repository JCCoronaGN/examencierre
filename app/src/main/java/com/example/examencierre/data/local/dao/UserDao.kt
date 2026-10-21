package com.example.examencierre.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examencierre.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE correo = :correo LIMIT 1")
    suspend fun findByCorreo(correo: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE correo = :correo)")
    suspend fun existsByCorreo(correo: String): Boolean

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY nombre ASC, apellidos ASC")
    suspend fun getAll(): List<UserEntity>
}
