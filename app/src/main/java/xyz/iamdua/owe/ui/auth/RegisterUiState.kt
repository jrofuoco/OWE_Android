package xyz.iamdua.owe.ui.auth

import xyz.iamdua.owe.data.repository.PsgcItem

data class RegisterUiState(
    val firstName: String = "",
    val firstNameError: String? = null,
    val middleName: String = "",
    val lastName: String = "",
    val lastNameError: String? = null,
    val age: String = "",
    val ageError: String? = null,
    val province: String = "",
    val provinceError: String? = null,
    val selectedProvinceCode: String? = null,
    val provinces: List<PsgcItem> = emptyList(),
    val isLoadingProvinces: Boolean = false,
    val municipality: String = "",
    val municipalityError: String? = null,
    val selectedMunicipalityCode: String? = null,
    val municipalities: List<PsgcItem> = emptyList(),
    val isLoadingMunicipalities: Boolean = false,
    val barangay: String = "",
    val barangayError: String? = null,
    val barangays: List<PsgcItem> = emptyList(),
    val isLoadingBarangays: Boolean = false,
    val locationError: String? = null,
    val streetAddress: String = "",
    val email: String = "",
    val emailError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val agreedToTerms: Boolean = false,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val isSuccess: Boolean = false,
    val showOtpDialog: Boolean = false,
    val otp: String = "",
    val otpError: String? = null,
    val isVerifyingOtp: Boolean = false,
    val isResendingOtp: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val otpInfoMessage: String? = null
) {
    val canSubmit: Boolean
        get() = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                age.isNotBlank() &&
                province.isNotBlank() &&
                municipality.isNotBlank() &&
                barangay.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                agreedToTerms &&
                !isLoading

    val canVerifyOtp: Boolean
        get() = otp.length in 6..8 && !isVerifyingOtp

    val canResendOtp: Boolean
        get() = resendCooldownSeconds == 0 && !isResendingOtp && !isVerifyingOtp
}
