package xyz.iamdua.owe.ui.auth

data class VerifyOtpUiState(
    val email: String = "",
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldownSeconds: Int = 60,
    val infoMessage: String? = null
) {
    val canVerify: Boolean
        get() = otp.length in 6..8 && !isLoading

    val canResend: Boolean
        get() = resendCooldownSeconds == 0 && !isResending && !isLoading
}
