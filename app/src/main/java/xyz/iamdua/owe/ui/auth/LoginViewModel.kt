package xyz.iamdua.owe.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.iamdua.owe.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRememberMeChanged(checked: Boolean) {
        _uiState.update { it.copy(rememberMe = checked) }
    }

    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validation
        var hasError = false
        var emailErr: String? = null
        var passErr: String? = null

        val emailTrimmed = currentState.email.trim()
        if (emailTrimmed.isEmpty()) {
            emailErr = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            emailErr = "Enter a valid email address"
            hasError = true
        }

        if (currentState.password.isEmpty()) {
            passErr = "Password is required"
            hasError = true
        } else if (currentState.password.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }

        if (hasError) {
            _uiState.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            authRepository.signIn(emailTrimmed, currentState.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = friendlyAuthErrorMessage(
                                error = error,
                                fallback = "Login failed. Please check your credentials."
                            )
                        )
                    }
                }
        }
    }
}
