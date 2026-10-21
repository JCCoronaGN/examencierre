package com.example.examencierre.ui.camera

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.examencierre.data.repository.PhotoRepository
import com.example.examencierre.di.ServiceLocator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface CameraEvent {
    data class PhotoSaved(val photoId: Long) : CameraEvent
    data object SaveError : CameraEvent
}

class CameraViewModel(private val photoRepository: PhotoRepository) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

    fun savePhoto(
        userId: Long,
        file: File,
        comentario: String?,
        location: Location?
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            val result = runCatching {
                photoRepository.save(
                    com.example.examencierre.data.local.entity.PhotoEntity(
                        userId = userId,
                        ruta = file.absolutePath,
                        comentario = comentario?.takeIf { it.isNotBlank() },
                        latitud = location?.latitude,
                        longitud = location?.longitude,
                        altitud = location?.altitude,
                        precisionMetros = location?.accuracy,
                        fechaHora = file.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()
                    )
                )
            }
            _isSaving.value = false
            _events.emit(
                result.fold(
                    onSuccess = { CameraEvent.PhotoSaved(it) },
                    onFailure = { CameraEvent.SaveError }
                )
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CameraViewModel(ServiceLocator.photoRepository(this[APPLICATION_KEY]!!))
            }
        }
    }
}
