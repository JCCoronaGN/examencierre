package com.example.examencierre.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.examencierre.data.local.entity.UserEntity
import com.example.examencierre.data.repository.AuthError
import com.example.examencierre.data.repository.AuthResult
import com.example.examencierre.data.repository.UserRepository
import com.example.examencierre.di.ServiceLocator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegisterEvent {
    data class Success(val user: UserEntity) : RegisterEvent
    data class Error(val error: AuthError) : RegisterEvent
}

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()

    fun register(
        nombre: String,
        apellidos: String,
        correo: String,
        password: String
    ) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = userRepository.register(nombre, apellidos, correo, password)) {
                is AuthResult.Success -> {
                    _isLoading.value = false
                    _events.emit(RegisterEvent.Success(result.user))
                }

                is AuthResult.Failure -> {
                    _isLoading.value = false
                    _events.emit(RegisterEvent.Error(result.error))
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RegisterViewModel(ServiceLocator.userRepository(this[APPLICATION_KEY]!!))
            }
        }
    }
}
