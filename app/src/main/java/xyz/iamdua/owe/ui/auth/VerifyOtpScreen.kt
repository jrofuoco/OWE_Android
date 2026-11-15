package xyz.iamdua.owe.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.iamdua.owe.ui.components.OwlLoadingSpinner
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
fun VerifyOtpScreen(
    email: String,
    onNavigateBack: () -> Unit = {},
    onVerified: () -> Unit = {},
    viewModel: VerifyOtpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(email) {
        viewModel.initEmail(email)
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

            Text(
                text = "Verify your email",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                color = Dark12
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to $email",
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
                    viewModel.verify(onVerified)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryPillButton(
                text = "Verify Account",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.verify(onVerified)
                },
                isLoading = uiState.isLoading,
                enabled = uiState.canVerify
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isResending) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 6.dp)
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
                        color = Dark12
                    )
                }
            } else {
                val resendLabel = if (uiState.resendCooldownSeconds > 0) {
                    "Resend code in ${uiState.resendCooldownSeconds}s"
                } else {
                    "Didn't receive the code? Resend"
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
                            viewModel.resendCode()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyOtpScreenPreview() {
    DebtCollectTheme {
        VerifyOtpScreen(email = "juandelacruz@gmail.com")
    }
}
