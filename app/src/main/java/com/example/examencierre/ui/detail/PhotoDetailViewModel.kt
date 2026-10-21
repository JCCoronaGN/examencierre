package com.example.examencierre.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.examencierre.data.local.entity.PhotoEntity
import com.example.examencierre.data.repository.PhotoRepository
import com.example.examencierre.data.storage.PhotoStorage
import com.example.examencierre.di.ServiceLocator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PhotoDetailEvent {
    data object Deleted : PhotoDetailEvent
    data object DeleteError : PhotoDetailEvent
}

sealed interface PhotoDetailUiState {
    data object Loading : PhotoDetailUiState
    data class Found(val photo: PhotoEntity) : PhotoDetailUiState
    data object NotFound : PhotoDetailUiState
}

class PhotoDetailViewModel(
    private val photoRepository: PhotoRepository,
    private val photoStorage: PhotoStorage
) : ViewModel() {

    private val _state = MutableStateFlow<PhotoDetailUiState>(PhotoDetailUiState.Loading)
    val state: StateFlow<PhotoDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PhotoDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PhotoDetailEvent> = _events.asSharedFlow()

    fun load(photoId: Long) {
        viewModelScope.launch {
            val photo = photoRepository.findById(photoId)
            _state.value = if (photo != null) {
                PhotoDetailUiState.Found(photo)
            } else {
                PhotoDetailUiState.NotFound
            }
        }
    }

    fun delete(photo: PhotoEntity) {
        viewModelScope.launch {
            val result = runCatching {
                photoStorage.delete(photoStorage.fileFor(photo.ruta))
                photoRepository.delete(photo.id)
            }
            _events.emit(
                result.fold(
                    onSuccess = { PhotoDetailEvent.Deleted },
                    onFailure = { PhotoDetailEvent.DeleteError }
                )
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!
                PhotoDetailViewModel(
                    ServiceLocator.photoRepository(application),
                    ServiceLocator.photoStorage(application)
                )
            }
        }
    }
}
