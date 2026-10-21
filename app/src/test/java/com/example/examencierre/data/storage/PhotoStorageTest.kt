package com.example.examencierre.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PhotoStorageTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `createPhotoFile creates the file inside the photos folder of the app`() {
        val storage = PhotoStorage(temporaryFolder.root)

        val file = storage.createPhotoFile()

        assertEquals("photos", file.parentFile?.name)
        assertTrue(file.name.startsWith("IMG_"))
        assertTrue(file.name.endsWith(".jpg"))
    }

    @Test
    fun `createPhotoFile generates a different name for each photo`() {
        val storage = PhotoStorage(temporaryFolder.root)

        val first = storage.createPhotoFile()
        val second = storage.createPhotoFile()

        assertFalse(first.name == second.name)
    }

    @Test
    fun `delete removes the photo file`() {
        val storage = PhotoStorage(temporaryFolder.root)
        val file = storage.createPhotoFile()
        file.parentFile?.mkdirs()
        file.createNewFile()

        storage.delete(file)

        assertFalse(file.exists())
    }
}
