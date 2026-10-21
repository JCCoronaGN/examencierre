package com.example.examencierre.data.repository

import com.example.examencierre.data.local.dao.PhotoDao
import com.example.examencierre.data.local.entity.PhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PhotoRepository(private val photoDao: PhotoDao) {

    fun observePhotos(userId: Long): Flow<List<PhotoEntity>> = photoDao.observeByUser(userId)

    fun observePhotoCount(userId: Long): Flow<Int> = photoDao.observeCountByUser(userId)

    suspend fun findById(id: Long): PhotoEntity? = withContext(Dispatchers.IO) {
        runCatching { photoDao.findById(id) }.getOrNull()
    }

    suspend fun findLastByUser(userId: Long): PhotoEntity? = withContext(Dispatchers.IO) {
        runCatching { photoDao.findLastByUser(userId) }.getOrNull()
    }

    suspend fun save(photo: PhotoEntity): Long = withContext(Dispatchers.IO) {
        photoDao.insert(photo)
    }

    suspend fun delete(photoId: Long) = withContext(Dispatchers.IO) {
        photoDao.deleteById(photoId)
    }
}
