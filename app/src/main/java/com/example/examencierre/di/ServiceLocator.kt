package com.example.examencierre.di

import android.content.Context
import com.example.examencierre.data.local.AppDatabase
import com.example.examencierre.data.repository.PhotoRepository
import com.example.examencierre.data.repository.UserRepository
import com.example.examencierre.data.session.SessionManager
import com.example.examencierre.data.storage.PhotoStorage

/**
 * Contenedor de dependencias simple (inyección manual) para evitar acoplar la app
 * a un framework de DI en un proyecto de este tamaño.
 */
object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var userRepository: UserRepository? = null

    @Volatile
    private var photoRepository: PhotoRepository? = null

    @Volatile
    private var sessionManager: SessionManager? = null

    @Volatile
    private var photoStorage: PhotoStorage? = null

    private fun database(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: AppDatabase.getInstance(context).also { database = it }
        }

    fun userRepository(context: Context): UserRepository =
        userRepository ?: synchronized(this) {
            userRepository ?: UserRepository(database(context).userDao()).also { userRepository = it }
        }

    fun photoRepository(context: Context): PhotoRepository =
        photoRepository ?: synchronized(this) {
            photoRepository ?: PhotoRepository(database(context).photoDao()).also { photoRepository = it }
        }

    fun sessionManager(context: Context): SessionManager =
        sessionManager ?: synchronized(this) {
            sessionManager ?: SessionManager(context).also { sessionManager = it }
        }

    fun photoStorage(context: Context): PhotoStorage =
        photoStorage ?: synchronized(this) {
            photoStorage ?: PhotoStorage(context.applicationContext.filesDir)
                .also { photoStorage = it }
        }
}
