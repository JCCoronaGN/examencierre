package com.example.examencierre.data.storage

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Gestiona el almacenamiento de las fotografías dentro de la carpeta privada de la app.
 */
class PhotoStorage(private val appFilesDir: File) {

    private val photosDirectory: File
        get() = File(appFilesDir, PHOTOS_FOLDER).apply { if (!exists()) mkdirs() }

    fun createPhotoFile(timestamp: Long = System.currentTimeMillis()): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date(timestamp))
        val suffix = UUID.randomUUID().toString().take(8)
        return File(photosDirectory, "IMG_${stamp}_$suffix.jpg")
    }

    fun delete(file: File) {
        runCatching { if (file.exists()) file.delete() }
    }

    fun fileFor(path: String): File = File(path)

    companion object {
        const val PHOTOS_FOLDER = "photos"
    }
}
