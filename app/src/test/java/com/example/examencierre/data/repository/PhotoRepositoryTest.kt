package com.example.examencierre.data.repository

import com.example.examencierre.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoRepositoryTest {

    private val photoDao = FakePhotoDao()
    private val repository = PhotoRepository(photoDao)

    private fun photo(userId: Long, path: String, time: Long) = PhotoEntity(
        userId = userId,
        ruta = path,
        comentario = "Comentario",
        latitud = 40.0,
        longitud = -3.0,
        altitud = 650.0,
        precisionMetros = 12.5f,
        fechaHora = time
    )

    @Test
    fun `save stores the photo and returns its id`() = runTest {
        val id = repository.save(photo(1L, "/data/photos/1.jpg", 100L))

        assertTrue(id > 0)
        assertEquals("/data/photos/1.jpg", repository.findById(id)?.ruta)
    }

    @Test
    fun `observePhotos only returns the photos of the given user`() = runTest {
        repository.save(photo(1L, "/data/photos/1.jpg", 100L))
        repository.save(photo(2L, "/data/photos/2.jpg", 200L))

        val photos = repository.observePhotos(1L).first()

        assertEquals(1, photos.size)
        assertEquals("/data/photos/1.jpg", photos.first().ruta)
    }

    @Test
    fun `observePhotoCount counts the photos of the user`() = runTest {
        repository.save(photo(1L, "/data/photos/1.jpg", 100L))
        repository.save(photo(1L, "/data/photos/2.jpg", 200L))

        assertEquals(2, repository.observePhotoCount(1L).first())
    }

    @Test
    fun `findLastByUser returns the most recent photo`() = runTest {
        repository.save(photo(1L, "/data/photos/old.jpg", 100L))
        repository.save(photo(1L, "/data/photos/new.jpg", 200L))

        val last = repository.findLastByUser(1L)

        assertEquals("/data/photos/new.jpg", last?.ruta)
    }

    @Test
    fun `delete removes the photo`() = runTest {
        val id = repository.save(photo(1L, "/data/photos/1.jpg", 100L))

        repository.delete(id)

        assertNull(repository.findById(id))
    }
}
