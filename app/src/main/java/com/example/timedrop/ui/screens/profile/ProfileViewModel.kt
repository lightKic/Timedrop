package com.example.timedrop.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.User
import com.example.timedrop.data.SyncRepository
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
    private val syncRepository = SyncRepository(AppDatabase.getDatabase(application))
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUser(email: String) {
        if (email.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
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
                
                // Sync to Firebase
                syncRepository.updateUserProfile(
                    fullName = fullName,
                    alias = alias ?: "",
                    photoBase64 = updatedUser.photoUri?.let { uri -> imageToBase64(uri) }
                )
                
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
                val internalUri = saveImageToInternalStorage(photoUri)
                if (internalUri != null) {
                    val updatedUser = currentUser.copy(photoUri = internalUri)
                    userDao.insertUser(updatedUser)
                    
                    // Sync to Firebase
                    syncRepository.updateUserProfile(
                        fullName = updatedUser.fullName,
                        alias = updatedUser.alias ?: "",
                        photoBase64 = imageToBase64(internalUri)
                    )
                    
                    _uiState.value = _uiState.value.copy(user = updatedUser)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage)
            }
        }
    }

    private fun imageToBase64(path: String): String? {
        return try {
            val file = java.io.File(path)
            if (!file.exists()) return null
            val bitmap = android.graphics.BitmapFactory.decodeFile(path)
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToInternalStorage(uriString: String): String? {
        return try {
            val context = getApplication<Application>().applicationContext
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = java.io.File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            
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
