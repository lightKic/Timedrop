package com.example.timedrop.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.timedrop.data.settings.SettingsDataStore

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val acceptTerms: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class SignUpViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val settingsStore = SettingsDataStore(application)

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onAcceptTermsChange(accepted: Boolean) {
        _uiState.value = _uiState.value.copy(acceptTerms = accepted)
    }

    fun signUp() {
        val state = _uiState.value
        if (state.fullName.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill all fields")
            return
        }
        if (!state.acceptTerms) {
            _uiState.value = _uiState.value.copy(error = "Please accept the terms")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = User(
                    fullName = state.fullName,
                    email = state.email,
                    password = state.password
                )
                userDao.insertUser(user)
                settingsStore.setCurrentUserEmail(state.email)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
