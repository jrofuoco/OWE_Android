package xyz.iamdua.owe.ui.auth

enum class ResetPasswordStep {
    ENTER_EMAIL,
    ENTER_OTP
}

data class ForgotPasswordUiState(
    val step: ResetPasswordStep = ResetPasswordStep.ENTER_EMAIL,
    val email: String = "",
    val emailError: String? = null,
    val otp: String = "",
    val otpError: String? = null,
    val showNewPasswordDialog: Boolean = false,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val infoMessage: String? = null
) {
    val canSendCode: Boolean
        get() = email.isNotBlank() && !isLoading

    val canVerifyOtp: Boolean
        get() = otp.length in 6..8 && !isLoading

    val canUpdatePassword: Boolean
        get() = newPassword.isNotBlank() && confirmPassword.isNotBlank() && !isLoading

    val canResend: Boolean
        get() = resendCooldownSeconds == 0 && !isLoading
}
