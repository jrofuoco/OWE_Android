package xyz.iamdua.owe.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.iamdua.owe.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, infoMessage = null) }
    }

    fun onOtpChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(otp = digitsOnly, otpError = null, infoMessage = null) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, newPasswordError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun dismissNewPasswordDialog() {
        _uiState.update { it.copy(showNewPasswordDialog = false) }
    }

    fun sendResetCode() {
        val emailTrimmed = _uiState.value.email.trim()
        if (emailTrimmed.isEmpty()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null, infoMessage = null) }
            authRepository.sendPasswordReset(emailTrimmed)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = ResetPasswordStep.ENTER_OTP,
                            infoMessage = "6-digit code sent to $emailTrimmed"
                        )
                    }
                    startCooldown()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            emailError = friendlyAuthErrorMessage(error, "Failed to send reset code.")
                        )
                    }
                }
        }
    }

    fun verifyOtp() {
        val s = _uiState.value
        if (s.otp.length !in 6..8) {
            _uiState.update { it.copy(otpError = "Enter the 6 to 8-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null) }
            authRepository.verifyRecoveryOtp(s.email, s.otp)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showNewPasswordDialog = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpError = friendlyAuthErrorMessage(error, "Invalid or expired code.")
                        )
                    }
                }
        }
    }

    fun updatePassword(onSuccess: () -> Unit) {
        val s = _uiState.value
        var hasError = false
        var passErr: String? = null
        var confirmErr: String? = null

        if (s.newPassword.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }
        if (s.confirmPassword != s.newPassword) {
            confirmErr = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            _uiState.update { it.copy(newPasswordError = passErr, confirmPasswordError = confirmErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.updatePassword(s.newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, showNewPasswordDialog = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            newPasswordError = friendlyAuthErrorMessage(error, "Failed to update password.")
                        )
                    }
                }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (sec in 60 downTo 1) {
                _uiState.update { it.copy(resendCooldownSeconds = sec) }
                delay(1000)
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }
}
