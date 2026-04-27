package com.example.timedrop.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUser(email: String) {
        if (email.isBlank()) return
        
        viewModelScope.launch {
            _uiState.compareAndSet(_uiState.value, _uiState.value.copy(isLoading = true, error = null))
            try {
                val user = userDao.findUserByEmail(email)
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun updateProfile(fullName: String, alias: String?) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val updatedUser = currentUser.copy(fullName = fullName, alias = alias)
                userDao.insertUser(updatedUser)
                _uiState.value = _uiState.value.copy(user = updatedUser, isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage)
            }
        }
    }

    fun updatePhoto(photoUri: String) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            try {
                // Save to internal storage to ensure persistence after app restart
                val internalUri = saveImageToInternalStorage(photoUri)
                if (internalUri != null) {
                    val updatedUser = currentUser.copy(photoUri = internalUri)
                    userDao.insertUser(updatedUser)
                    _uiState.value = _uiState.value.copy(user = updatedUser)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage)
            }
        }
    }

    private fun saveImageToInternalStorage(uriString: String): String? {
        return try {
            val context = getApplication<Application>().applicationContext
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = java.io.File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            
            // Clean up old photos if needed (optional but good practice)
            context.filesDir.listFiles { f -> f.name.startsWith("profile_photo_") }?.forEach { it.delete() }
            
            val outputStream = java.io.FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
