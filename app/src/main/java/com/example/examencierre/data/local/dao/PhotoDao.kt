package com.example.examencierre.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.examencierre.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Query("SELECT * FROM photos WHERE user_id = :userId ORDER BY fecha_hora DESC, id DESC")
    fun observeByUser(userId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PhotoEntity?

    @Query("SELECT * FROM photos WHERE user_id = :userId ORDER BY fecha_hora DESC, id DESC LIMIT 1")
    suspend fun findLastByUser(userId: Long): PhotoEntity?

    @Query("SELECT COUNT(*) FROM photos WHERE user_id = :userId")
    fun observeCountByUser(userId: Long): Flow<Int>

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
