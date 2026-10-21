package com.example.examencierre.data.repository

import com.example.examencierre.data.local.dao.PhotoDao
import com.example.examencierre.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria del DAO de fotos para las pruebas unitarias.
 */
class FakePhotoDao : PhotoDao {

    private val photos = linkedMapOf<Long, PhotoEntity>()
    private val photosFlow = MutableStateFlow<List<PhotoEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(photo: PhotoEntity): Long {
        val id = nextId++
        photos[id] = photo.copy(id = id)
        photosFlow.value = photos.values.sortedByDescending { it.fechaHora }
        return id
    }

    override fun observeByUser(userId: Long): Flow<List<PhotoEntity>> =
        photosFlow.map { list -> list.filter { it.userId == userId } }

    override suspend fun findById(id: Long): PhotoEntity? = photos[id]

    override suspend fun findLastByUser(userId: Long): PhotoEntity? =
        photos.values.filter { it.userId == userId }.maxByOrNull { it.fechaHora }

    override fun observeCountByUser(userId: Long): Flow<Int> =
        photosFlow.map { list -> list.count { it.userId == userId } }

    override suspend fun deleteById(id: Long) {
        photos.remove(id)
        photosFlow.value = photos.values.sortedByDescending { it.fechaHora }
    }
}
