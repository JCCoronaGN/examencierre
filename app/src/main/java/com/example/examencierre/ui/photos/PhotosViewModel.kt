package com.example.examencierre.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.examencierre.data.local.entity.PhotoEntity
import com.example.examencierre.data.repository.PhotoRepository
import com.example.examencierre.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PhotosViewModel(
    photoRepository: PhotoRepository,
    userId: Long
) : ViewModel() {

    val photos: StateFlow<List<PhotoEntity>> = photoRepository.observePhotos(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!
                val session = ServiceLocator.sessionManager(application)
                PhotosViewModel(ServiceLocator.photoRepository(application), session.userId)
            }
        }
    }
}
