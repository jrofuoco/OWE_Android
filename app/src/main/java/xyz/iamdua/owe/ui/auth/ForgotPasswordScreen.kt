package xyz.iamdua.owe.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.iamdua.owe.ui.components.DebtTextField
import xyz.iamdua.owe.ui.components.OtpInputField
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    if (uiState.showNewPasswordDialog) {
        NewPasswordDialog(
            newPassword = uiState.newPassword,
            newPasswordError = uiState.newPasswordError,
            confirmPassword = uiState.confirmPassword,
            confirmPasswordError = uiState.confirmPasswordError,
            isNewPasswordVisible = uiState.isNewPasswordVisible,
            isConfirmPasswordVisible = uiState.isConfirmPasswordVisible,
            isLoading = uiState.isLoading,
            canSubmit = uiState.canUpdatePassword,
            onNewPasswordChanged = viewModel::onNewPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onToggleNewPasswordVisibility = viewModel::toggleNewPasswordVisibility,
            onToggleConfirmPasswordVisibility = viewModel::toggleConfirmPasswordVisibility,
            onDismiss = viewModel::dismissNewPasswordDialog,
            onConfirm = {
                focusManager.clearFocus()
                viewModel.updatePassword(onNavigateBack)
            }
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(White)
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

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedContent(
                targetState = uiState.step,
                label = "ResetPasswordStep"
            ) { step ->
                when (step) {
                    ResetPasswordStep.ENTER_EMAIL -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Reset password",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                                color = Dark12
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter your email and we'll send a 6-digit verification code.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = GrayText
                            )

                            Spacer(modifier = Modifier.height(32.dp))

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
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.sendResetCode()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            PrimaryPillButton(
                                text = "Send Reset Code",
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.sendResetCode()
                                },
                                isLoading = uiState.isLoading,
                                enabled = uiState.canSendCode
                            )
                        }
                    }

                    ResetPasswordStep.ENTER_OTP -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Enter reset code",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                                color = Dark12
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter the 6-digit code sent to ${uiState.email.trim()}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = GrayText
                            )

                            AnimatedVisibility(
                                visible = uiState.infoMessage != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(StatusActiveBg)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = uiState.infoMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = StatusActiveText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            OtpInputField(
                                otp = uiState.otp,
                                onOtpChange = viewModel::onOtpChanged,
                                length = 6,
                                errorMessage = uiState.otpError,
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.verifyOtp()
                                }
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            PrimaryPillButton(
                                text = "Verify Code",
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.verifyOtp()
                                },
                                isLoading = uiState.isLoading,
                                enabled = uiState.canVerifyOtp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val resendLabel = if (uiState.resendCooldownSeconds > 0) {
                                "Resend code in ${uiState.resendCooldownSeconds}s"
                            } else {
                                "Didn't receive it? Resend code"
                            }

                            Text(
                                text = resendLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (uiState.canResend) Dark12 else GrayText,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = uiState.canResend) {
                                        viewModel.sendResetCode()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewPasswordDialog(
    newPassword: String,
    newPasswordError: String?,
    confirmPassword: String,
    confirmPasswordError: String?,
    isNewPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    isLoading: Boolean,
    canSubmit: Boolean,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onToggleNewPasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(White)
                .padding(24.dp)
        ) {
            Text(
                text = "Set new password",
                style = MaterialTheme.typography.headlineMedium,
                color = Dark12
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Create a strong password for your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(20.dp))

            DebtTextField(
                value = newPassword,
                onValueChange = onNewPasswordChanged,
                label = "New Password",
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
                    IconButton(onClick = onToggleNewPasswordVisibility) {
                        Icon(
                            imageVector = if (isNewPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                errorMessage = newPasswordError,
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            DebtTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = "Confirm Password",
                placeholder = "Re-enter new password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onToggleConfirmPasswordVisibility) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                errorMessage = confirmPasswordError,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onConfirm() }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryPillButton(
                text = "Update Password",
                onClick = onConfirm,
                isLoading = isLoading,
                enabled = canSubmit
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    DebtCollectTheme {
        ForgotPasswordScreen()
    }
}
