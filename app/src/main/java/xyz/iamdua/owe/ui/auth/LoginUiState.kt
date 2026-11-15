package xyz.iamdua.owe.ui.auth

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val isSuccess: Boolean = false
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
