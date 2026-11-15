package xyz.iamdua.owe.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import xyz.iamdua.owe.ui.components.OwlLoadingSpinner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import xyz.iamdua.owe.ui.components.OweLogo
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.iamdua.owe.data.repository.PsgcItem
import xyz.iamdua.owe.ui.components.DebtTextField
import xyz.iamdua.owe.ui.components.OtpInputField
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var activeLocationPicker by remember { mutableStateOf<String?>(null) }
    var activeLegalSheet by remember { mutableStateOf<String?>(null) }

    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        launch {
            contentOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
    }

    activeLegalSheet?.let { sheetType ->
        LegalBottomSheet(
            title = if (sheetType == "terms") "Terms of Service" else "Privacy Policy",
            sections = if (sheetType == "terms") termsOfServiceSections else privacyPolicySections,
            onAgree = {
                viewModel.onTermsChanged(true)
                activeLegalSheet = null
            },
            onDismiss = { activeLegalSheet = null }
        )
    }

    activeLocationPicker?.let { pickerType ->
        val (title, items, selectedName, isLoading) = when (pickerType) {
            "province" -> LocationPickerConfig(
                title = "Select Province",
                items = uiState.provinces,
                selectedName = uiState.province,
                isLoading = uiState.isLoadingProvinces
            )
            "municipality" -> LocationPickerConfig(
                title = "Select Municipality / City",
                items = uiState.municipalities,
                selectedName = uiState.municipality,
                isLoading = uiState.isLoadingMunicipalities
            )
            else -> LocationPickerConfig(
                title = "Select Barangay",
                items = uiState.barangays,
                selectedName = uiState.barangay,
                isLoading = uiState.isLoadingBarangays
            )
        }

        LocationPickerBottomSheet(
            title = title,
            items = items,
            selectedName = selectedName,
            isLoading = isLoading,
            errorMessage = uiState.locationError,
            onRetry = { viewModel.retryCurrentLocationLoad(pickerType) },
            onSelect = { item ->
                when (pickerType) {
                    "province" -> viewModel.onProvinceSelected(item)
                    "municipality" -> viewModel.onMunicipalitySelected(item)
                    "barangay" -> viewModel.onBarangaySelected(item)
                }
                activeLocationPicker = null
            },
            onDismiss = { activeLocationPicker = null }
        )
    }

    if (uiState.showOtpDialog) {
        VerifyOtpDialog(
            email = uiState.email.trim(),
            otp = uiState.otp,
            otpError = uiState.otpError,
            infoMessage = uiState.otpInfoMessage,
            isLoading = uiState.isVerifyingOtp,
            isResending = uiState.isResendingOtp,
            canVerify = uiState.canVerifyOtp,
            canResend = uiState.canResendOtp,
            resendCooldownSeconds = uiState.resendCooldownSeconds,
            onOtpChanged = viewModel::onOtpChanged,
            onVerify = {
                focusManager.clearFocus()
                viewModel.verifyOtp(onRegisterSuccess)
            },
            onResend = viewModel::resendOtp,
            onDismiss = viewModel::dismissOtpDialog
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffsetY.value
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top bar: Back button + subtle OWE brand mark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(White)
                        .border(1.dp, GrayBorder, CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Dark12,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(White)
                        .border(1.dp, GrayBorder, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    OweLogo(modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OWE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Dark12
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp, top = 4.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(LimeAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Subtle pill badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StatusActiveBg)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "NEW ACCOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = StatusActiveText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = "Create account",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Dark12
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Start managing your loans and debts seamlessly.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = GrayText
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Section 1: Personal Information
            RegisterSectionCard(
                title = "Personal Information",
                icon = Icons.Outlined.Person
            ) {
                // First Name
                DebtTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChanged,
                    label = "First Name",
                    placeholder = "Juan",
                    errorMessage = uiState.firstNameError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Last Name
                DebtTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChanged,
                    label = "Last Name",
                    placeholder = "Dela Cruz",
                    errorMessage = uiState.lastNameError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Name (Optional) & Age Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DebtTextField(
                        value = uiState.middleName,
                        onValueChange = viewModel::onMiddleNameChanged,
                        label = "Middle Name (Optional)",
                        placeholder = "Reyes",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.weight(2f)
                    )

                    DebtTextField(
                        value = uiState.age,
                        onValueChange = viewModel::onAgeChanged,
                        label = "Age",
                        placeholder = "25",
                        errorMessage = uiState.ageError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Address Details
            RegisterSectionCard(
                title = "Address Details",
                icon = Icons.Outlined.LocationOn
            ) {
                // Province Dropdown
                LocationDropdownField(
                    label = "Province",
                    value = uiState.province,
                    placeholder = "Select province",
                    errorMessage = uiState.provinceError,
                    isLoading = uiState.isLoadingProvinces,
                    enabled = true,
                    onClick = {
                        focusManager.clearFocus()
                        if (uiState.provinces.isEmpty() && !uiState.isLoadingProvinces) {
                            viewModel.loadProvinces()
                        }
                        activeLocationPicker = "province"
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Municipality / City & Barangay Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LocationDropdownField(
                        label = "Municipality / City",
                        value = uiState.municipality,
                        placeholder = if (uiState.selectedProvinceCode == null) "Select province first" else "Select city/mun.",
                        errorMessage = uiState.municipalityError,
                        isLoading = uiState.isLoadingMunicipalities,
                        enabled = uiState.selectedProvinceCode != null,
                        onClick = {
                            focusManager.clearFocus()
                            activeLocationPicker = "municipality"
                        },
                        modifier = Modifier.weight(1f)
                    )

                    LocationDropdownField(
                        label = "Barangay",
                        value = uiState.barangay,
                        placeholder = if (uiState.selectedMunicipalityCode == null) "Select city first" else "Select barangay",
                        errorMessage = uiState.barangayError,
                        isLoading = uiState.isLoadingBarangays,
                        enabled = uiState.selectedMunicipalityCode != null,
                        onClick = {
                            focusManager.clearFocus()
                            activeLocationPicker = "barangay"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Street / House Address (Optional)
                DebtTextField(
                    value = uiState.streetAddress,
                    onValueChange = viewModel::onStreetAddressChanged,
                    label = "Street / House Address (Optional)",
                    placeholder = "123 Rizal St.",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Account & Security
            RegisterSectionCard(
                title = "Account & Security",
                icon = Icons.Outlined.Lock
            ) {
                // Email
                DebtTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Email Address",
                    placeholder = "juandelacruz@gmail.com",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    errorMessage = uiState.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone (Optional)
                DebtTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChanged,
                    label = "Phone Number (Optional)",
                    placeholder = "0917 123 4567",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    errorMessage = uiState.phoneError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                DebtTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Password",
                    placeholder = "At least 6 characters",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (uiState.isPasswordVisible) "Hide password" else "Show password",
                                tint = GrayText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    errorMessage = uiState.passwordError,
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password
                DebtTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    label = "Confirm Password",
                    placeholder = "Re-enter your password",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                            Icon(
                                imageVector = if (uiState.isConfirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (uiState.isConfirmPasswordVisible) "Hide password" else "Show password",
                                tint = GrayText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    errorMessage = uiState.confirmPasswordError,
                    visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.register()
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Terms and Privacy Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = uiState.agreedToTerms,
                    onCheckedChange = viewModel::onTermsChanged,
                    colors = CheckboxDefaults.colors(
                        checkedColor = ForestGreen,
                        checkmarkColor = White,
                        uncheckedColor = GrayBorder
                    )
                )

                val linkStyle = TextLinkStyles(
                    style = SpanStyle(
                        color = StatusActiveText,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                )

                val termsAnnotated = buildAnnotatedString {
                    append("I agree to the ")
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "terms",
                            styles = linkStyle,
                            linkInteractionListener = { activeLegalSheet = "terms" }
                        )
                    ) {
                        append("Terms of Service")
                    }
                    append(" & ")
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "privacy",
                            styles = linkStyle,
                            linkInteractionListener = { activeLegalSheet = "privacy" }
                        )
                    ) {
                        append("Privacy Policy")
                    }
                }

                Text(
                    text = termsAnnotated,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = GrayText
                )
            }

            AnimatedVisibility(
                visible = uiState.termsError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = uiState.termsError.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusOverdueText,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            PrimaryPillButton(
                text = "Create Account",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.register()
                },
                isLoading = uiState.isLoading,
                enabled = uiState.canSubmit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Dark12,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToLogin() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun VerifyOtpDialog(
    email: String,
    otp: String,
    otpError: String?,
    infoMessage: String?,
    isLoading: Boolean,
    isResending: Boolean,
    canVerify: Boolean,
    canResend: Boolean,
    resendCooldownSeconds: Int,
    onOtpChanged: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(White)
                .border(1.dp, GrayBorder, RoundedCornerShape(24.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with close button
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(StatusActiveBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = StatusActiveText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GraySurface)
                        .clickable(enabled = !isLoading) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = GrayText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StatusActiveBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "EMAIL VERIFICATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = StatusActiveText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Verify your email",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Dark12,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter the 6-digit code sent to",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GraySurface)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Dark12
                )
            }

            if (!infoMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusActiveBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = infoMessage,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = StatusActiveText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OtpInputField(
                otp = otp,
                onOtpChange = onOtpChanged,
                length = 6,
                errorMessage = otpError,
                onDone = onVerify
            )

            Spacer(modifier = Modifier.height(22.dp))

            PrimaryPillButton(
                text = "Verify Account",
                onClick = onVerify,
                isLoading = isLoading,
                enabled = canVerify
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isResending) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    OwlLoadingSpinner(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sending new code...",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = StatusActiveText
                    )
                }
            } else {
                val resendLabel = if (resendCooldownSeconds > 0) {
                    "Resend code in ${resendCooldownSeconds}s"
                } else {
                    "Didn't receive it? Resend"
                }

                Text(
                    text = resendLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (canResend) StatusActiveText else GrayText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = canResend) { onResend() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    DebtCollectTheme {
        RegisterScreen()
    }
}

private data class LocationPickerConfig(
    val title: String,
    val items: List<PsgcItem>,
    val selectedName: String,
    val isLoading: Boolean
)

@Composable
private fun LocationDropdownField(
    label: String,
    value: String,
    placeholder: String,
    errorMessage: String?,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        errorMessage != null -> StatusOverdueText
        else -> GrayBorder
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Dark12,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) White else LightBg)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = if (value.isNotBlank()) Dark12 else GrayText.copy(alpha = if (enabled) 0.7f else 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            if (isLoading) {
                OwlLoadingSpinner(
                    strokeWidth = 1.8.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Open $label picker",
                    tint = if (enabled) GrayText else GrayText.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = StatusOverdueText,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerBottomSheet(
    title: String,
    items: List<PsgcItem>,
    selectedName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onSelect: (PsgcItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 560.dp)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = Dark12
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search...",
                        color = GrayText.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StatusActiveText,
                    unfocusedBorderColor = GrayBorder,
                    focusedContainerColor = LightBg,
                    unfocusedContainerColor = LightBg,
                    focusedTextColor = Dark12,
                    unfocusedTextColor = Dark12
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OwlLoadingSpinner(
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                errorMessage != null && items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusOverdueText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to Retry",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusActiveText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusActiveBg)
                                .clickable { onRetry() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
                filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayText
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredItems, key = { it.code }) { item ->
                            val isSelected = item.name == selectedName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) StatusActiveBg else White)
                                    .clickable { onSelect(item) }
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) StatusActiveText else Dark12,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = StatusActiveText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private data class LegalSectionItem(
    val title: String,
    val body: String
)

private val termsOfServiceSections = listOf(
    LegalSectionItem(
        title = "1. Nature of the Service",
        body = "OWE is a personal record-keeping and debt-tracking tool designed to help lenders and borrowers document loan agreements, monitor balances and due dates, and record payment proofs. OWE is not a bank, lending company, payment processor, or escrow service, and does not hold or transfer funds."
    ),
    LegalSectionItem(
        title = "2. Eligibility",
        body = "You must be at least 18 years old and provide accurate registration details (including your legal name, age, email address, and location). You may maintain only one active account unless authorized."
    ),
    LegalSectionItem(
        title = "3. Accounts & Security",
        body = "You are responsible for safeguarding your login credentials and OTP verification codes, as well as all activity under your account."
    ),
    LegalSectionItem(
        title = "4. Loans, Invite Codes & Payments",
        body = "Creating or joining a debt record via invite code reflects an agreement between participating users. Falsifying payment records or submitting fraudulent proof is strictly prohibited. OWE is not responsible for enforcing collections or resolving financial disputes between users."
    ),
    LegalSectionItem(
        title = "5. Acceptable Use",
        body = "You agree not to use OWE for unlawful or deceptive transactions, harassment, impersonation, or attempts to bypass security controls. We may suspend or terminate accounts that violate these Terms."
    ),
    LegalSectionItem(
        title = "6. Pro Subscription",
        body = "If optional paid features are offered, pricing and billing cycles will be displayed prior to purchase. Subscriptions are non-refundable once the billing period begins unless required by law."
    ),
    LegalSectionItem(
        title = "7. Privacy & Intellectual Property",
        body = "Your profile details, loan records, and payment logs are stored solely to provide and improve OWE. The OWE name, logo, and software are the intellectual property of DUA."
    ),
    LegalSectionItem(
        title = "8. Disclaimer & Liability",
        body = "OWE is provided \"AS IS\" and \"AS AVAILABLE\". To the fullest extent permitted by law, DUA is not liable for unpaid debts, financial losses, or disputes between users."
    ),
    LegalSectionItem(
        title = "9. Contact",
        body = "For questions or support regarding these Terms, contact DUA at joshuaorlina08@gmail.com."
    )
)

private val privacyPolicySections = listOf(
    LegalSectionItem(
        title = "1. Information We Collect",
        body = "We collect the profile details you provide during registration (first, middle, and last name, age, email address, optional phone number, and Philippine address), along with loan records, payment logs, and uploaded payment proofs."
    ),
    LegalSectionItem(
        title = "2. How We Use Your Information",
        body = "Your data is used solely to verify your account, connect lenders and borrowers via invite codes, track repayment progress and due dates, and keep the service secure. We never sell or rent your personal data."
    ),
    LegalSectionItem(
        title = "3. Data Storage & Security",
        body = "Account and loan data are stored securely using Supabase with Row Level Security (RLS) policies so only authorized users can access their records. Location dropdown lists are cached locally on your device."
    ),
    LegalSectionItem(
        title = "4. Data Sharing",
        body = "Your name and relevant loan details are visible only to the other party participating in a debt record you create or join, or when required by applicable law."
    ),
    LegalSectionItem(
        title = "5. Your Rights & Deletion",
        body = "You may update your profile in the app or request deletion of your account and personal data at any time by emailing joshuaorlina08@gmail.com."
    ),
    LegalSectionItem(
        title = "6. Age Requirement",
        body = "OWE is strictly intended for users 18 years of age and older. We do not knowingly collect data from anyone under 18."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalBottomSheet(
    title: String,
    sections: List<LegalSectionItem>,
    onAgree: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Dark12
                    )
                    Text(
                        text = "OWE · Effective November 13, 2026",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = GrayText
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GrayBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                sections.forEach { section ->
                    LegalSection(title = section.title, body = section.body)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryPillButton(
                text = "I Understand & Agree",
                onClick = onAgree
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LegalSection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = Dark12
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            color = GrayText
        )
    }
}

@Composable
private fun RegisterSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .border(1.dp, GrayBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(StatusActiveBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StatusActiveText,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Dark12
            )
        }
        content()
    }
}
