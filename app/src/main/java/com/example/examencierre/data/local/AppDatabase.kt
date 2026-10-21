package com.example.examencierre.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.examencierre.data.local.dao.PhotoDao
import com.example.examencierre.data.local.dao.UserDao
import com.example.examencierre.data.local.entity.PhotoEntity
import com.example.examencierre.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun photoDao(): PhotoDao

    companion object {
        private const val DATABASE_NAME = "examencierre.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
