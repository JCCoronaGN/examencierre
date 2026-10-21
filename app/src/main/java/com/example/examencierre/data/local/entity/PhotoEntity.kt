package com.example.examencierre.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id"), Index("fecha_hora")]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "ruta")
    val ruta: String,
    @ColumnInfo(name = "comentario")
    val comentario: String?,
    @ColumnInfo(name = "latitud")
    val latitud: Double?,
    @ColumnInfo(name = "longitud")
    val longitud: Double?,
    @ColumnInfo(name = "altitud")
    val altitud: Double?,
    @ColumnInfo(name = "precision_metros")
    val precisionMetros: Float?,
    @ColumnInfo(name = "fecha_hora")
    val fechaHora: Long,
    @ColumnInfo(name = "creado_en")
    val creadoEn: Long = System.currentTimeMillis()
)
