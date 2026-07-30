package com.spa.management.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.management.data.api.SessionManager
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onUsernameChange(v: String) {
        uiState = uiState.copy(username = v, error = null)
    }

    fun onPasswordChange(v: String) {
        uiState = uiState.copy(password = v, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        uiState = uiState.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = SpaRepository.login(uiState.username.trim(), uiState.password)) {
                is ApiResult.Success -> {
                    SessionManager.saveSession(
                        token = result.data.token,
                        staffName = result.data.staff.fullName,
                        role = result.data.staff.role
                    )
                    uiState = uiState.copy(loading = false)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(loading = false, error = result.message)
                }
            }
        }
    }
}
