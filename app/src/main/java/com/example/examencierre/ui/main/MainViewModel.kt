package com.example.examencierre.ui.main

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    photoRepository: PhotoRepository,
    private val userId: Long
) : ViewModel() {

    val photoCount: StateFlow<Int> = photoRepository.observePhotoCount(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val lastPhoto: StateFlow<PhotoEntity?> = photoRepository.observePhotos(userId)
        .map { photos -> photos.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!
                val session = ServiceLocator.sessionManager(application)
                MainViewModel(ServiceLocator.photoRepository(application), session.userId)
            }
        }
    }
}
