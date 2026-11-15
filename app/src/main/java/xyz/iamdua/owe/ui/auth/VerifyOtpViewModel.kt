package xyz.iamdua.owe.ui.auth

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

class VerifyOtpViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyOtpUiState())
    val uiState: StateFlow<VerifyOtpUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun initEmail(email: String) {
        if (_uiState.value.email != email) {
            _uiState.update { it.copy(email = email) }
            startCooldown()
        }
    }

    fun onOtpChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(otp = digitsOnly, otpError = null, infoMessage = null) }
    }

    fun verify(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.otp.length !in 6..8) {
            _uiState.update { it.copy(otpError = "Enter the 6 to 8-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null, infoMessage = null) }
            authRepository.verifySignupOtp(s.email, s.otp)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
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

    fun resendCode() {
        val email = _uiState.value.email
        if (email.isBlank() || !_uiState.value.canResend) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, otpError = null, infoMessage = null) }
            authRepository.resendSignupOtp(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            infoMessage = "A new code was sent to $email"
                        )
                    }
                    startCooldown()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            otpError = friendlyAuthErrorMessage(error, "Failed to resend code.")
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
