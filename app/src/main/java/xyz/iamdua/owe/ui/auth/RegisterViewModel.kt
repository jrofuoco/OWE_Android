package xyz.iamdua.owe.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.iamdua.owe.data.repository.AuthRepository
import xyz.iamdua.owe.data.repository.PsgcItem
import xyz.iamdua.owe.data.repository.PsgcRepository

class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = AuthRepository()
    private val psgcRepository: PsgcRepository = PsgcRepository(application)

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        loadProvinces()
    }

    fun loadProvinces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProvinces = true, locationError = null) }
            psgcRepository.getProvinces()
                .onSuccess { list ->
                    _uiState.update { it.copy(provinces = list, isLoadingProvinces = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingProvinces = false,
                            locationError = "Unable to load provinces. Check your connection."
                        )
                    }
                }
        }
    }

    private fun loadMunicipalities(provinceCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMunicipalities = true, locationError = null) }
            psgcRepository.getCitiesMunicipalities(provinceCode)
                .onSuccess { list ->
                    _uiState.update { it.copy(municipalities = list, isLoadingMunicipalities = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingMunicipalities = false,
                            locationError = "Unable to load cities/municipalities. Tap to retry."
                        )
                    }
                }
        }
    }

    private fun loadBarangays(cityOrMunicipalityCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBarangays = true, locationError = null) }
            psgcRepository.getBarangays(cityOrMunicipalityCode)
                .onSuccess { list ->
                    _uiState.update { it.copy(barangays = list, isLoadingBarangays = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingBarangays = false,
                            locationError = "Unable to load barangays. Tap to retry."
                        )
                    }
                }
        }
    }

    fun retryCurrentLocationLoad(target: String) {
        when (target) {
            "province" -> loadProvinces()
            "municipality" -> _uiState.value.selectedProvinceCode?.let { loadMunicipalities(it) }
            "barangay" -> _uiState.value.selectedMunicipalityCode?.let { loadBarangays(it) }
        }
    }

    fun onFirstNameChanged(value: String) {
        _uiState.update { it.copy(firstName = value, firstNameError = null, generalError = null) }
    }

    fun onMiddleNameChanged(value: String) {
        _uiState.update { it.copy(middleName = value, generalError = null) }
    }

    fun onLastNameChanged(value: String) {
        _uiState.update { it.copy(lastName = value, lastNameError = null, generalError = null) }
    }

    fun onAgeChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(age = digits, ageError = null, generalError = null) }
    }

    fun onProvinceSelected(item: PsgcItem) {
        _uiState.update {
            it.copy(
                province = item.name,
                selectedProvinceCode = item.code,
                provinceError = null,
                municipality = "",
                selectedMunicipalityCode = null,
                municipalities = emptyList(),
                municipalityError = null,
                barangay = "",
                barangays = emptyList(),
                barangayError = null,
                generalError = null
            )
        }
        loadMunicipalities(item.code)
    }

    fun onMunicipalitySelected(item: PsgcItem) {
        _uiState.update {
            it.copy(
                municipality = item.name,
                selectedMunicipalityCode = item.code,
                municipalityError = null,
                barangay = "",
                barangays = emptyList(),
                barangayError = null,
                generalError = null
            )
        }
        loadBarangays(item.code)
    }

    fun onBarangaySelected(item: PsgcItem) {
        _uiState.update {
            it.copy(
                barangay = item.name,
                barangayError = null,
                generalError = null
            )
        }
    }

    fun onStreetAddressChanged(value: String) {
        _uiState.update { it.copy(streetAddress = value, generalError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPhoneChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '+' || it == ' ' }
        _uiState.update { it.copy(phone = filtered, phoneError = null, generalError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onTermsChanged(agreed: Boolean) {
        _uiState.update { it.copy(agreedToTerms = agreed, termsError = null) }
    }

    fun register() {
        val s = _uiState.value
        var hasError = false

        var firstErr: String? = null
        var lastErr: String? = null
        var ageErr: String? = null
        var provErr: String? = null
        var muniErr: String? = null
        var brgyErr: String? = null
        var emailErr: String? = null
        var phoneErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null
        var termsErr: String? = null

        if (s.firstName.trim().isEmpty()) {
            firstErr = "First name is required"
            hasError = true
        }
        if (s.lastName.trim().isEmpty()) {
            lastErr = "Last name is required"
            hasError = true
        }

        val parsedAge = s.age.toIntOrNull()
        if (parsedAge == null || parsedAge < 18 || parsedAge > 120) {
            ageErr = "Must be 18 or older"
            hasError = true
        }

        if (s.province.trim().isEmpty()) {
            provErr = "Province is required"
            hasError = true
        }
        if (s.municipality.trim().isEmpty()) {
            muniErr = "Municipality / City is required"
            hasError = true
        }
        if (s.barangay.trim().isEmpty()) {
            brgyErr = "Barangay is required"
            hasError = true
        }

        val emailTrimmed = s.email.trim()
        if (emailTrimmed.isEmpty()) {
            emailErr = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            emailErr = "Enter a valid email"
            hasError = true
        }

        val phoneDigits = s.phone.filter { it.isDigit() }
        if (phoneDigits.isNotEmpty() && phoneDigits.length < 10) {
            phoneErr = "Enter a valid mobile number (e.g. 09171234567)"
            hasError = true
        }

        if (s.password.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }
        if (s.confirmPassword != s.password) {
            confirmErr = "Passwords do not match"
            hasError = true
        }
        if (!s.agreedToTerms) {
            termsErr = "You must accept the Terms and Privacy Policy"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    firstNameError = firstErr,
                    lastNameError = lastErr,
                    ageError = ageErr,
                    provinceError = provErr,
                    municipalityError = muniErr,
                    barangayError = brgyErr,
                    emailError = emailErr,
                    phoneError = phoneErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr,
                    termsError = termsErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            authRepository.signUp(
                firstName = s.firstName,
                middleName = s.middleName,
                lastName = s.lastName,
                age = parsedAge!!,
                province = s.province,
                municipality = s.municipality,
                barangay = s.barangay,
                streetAddress = s.streetAddress,
                email = emailTrimmed,
                phone = s.phone,
                password = s.password
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showOtpDialog = true,
                        otp = "",
                        otpError = null,
                        otpInfoMessage = null
                    )
                }
                startOtpCooldown()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = friendlyAuthErrorMessage(error, "Registration failed.")
                    )
                }
            }
        }
    }

    fun onOtpChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(otp = digitsOnly, otpError = null, otpInfoMessage = null) }
    }

    fun dismissOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = false) }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.otp.length !in 6..8) {
            _uiState.update { it.copy(otpError = "Enter the 6 to 8-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingOtp = true, otpError = null, otpInfoMessage = null) }
            authRepository.verifySignupOtp(s.email, s.otp)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            showOtpDialog = false,
                            isSuccess = true
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            otpError = friendlyAuthErrorMessage(error, "Invalid or expired code.")
                        )
                    }
                }
        }
    }

    fun resendOtp() {
        val email = _uiState.value.email.trim()
        if (email.isBlank() || !_uiState.value.canResendOtp) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResendingOtp = true, otpError = null, otpInfoMessage = null) }
            authRepository.resendSignupOtp(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isResendingOtp = false,
                            otpInfoMessage = "A new code was sent to $email"
                        )
                    }
                    startOtpCooldown()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isResendingOtp = false,
                            otpError = friendlyAuthErrorMessage(error, "Failed to resend code.")
                        )
                    }
                }
        }
    }

    private fun startOtpCooldown() {
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
