package com.example.examencierre.ui.login

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

sealed interface LoginEvent {
    data class Success(val user: UserEntity) : LoginEvent
    data class Error(val error: AuthError) : LoginEvent
}

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun login(correo: String, password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = userRepository.login(correo, password)) {
                is AuthResult.Success -> {
                    _isLoading.value = false
                    _events.emit(LoginEvent.Success(result.user))
                }

                is AuthResult.Failure -> {
                    _isLoading.value = false
                    _events.emit(LoginEvent.Error(result.error))
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(ServiceLocator.userRepository(this[APPLICATION_KEY]!!))
            }
        }
    }
}
