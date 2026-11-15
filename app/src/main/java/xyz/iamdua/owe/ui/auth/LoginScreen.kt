package xyz.iamdua.owe.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import xyz.iamdua.owe.ui.components.OwlLoadingSpinner
import xyz.iamdua.owe.ui.components.OweLogo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.iamdua.owe.R
import xyz.iamdua.owe.ui.components.DebtTextField
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.StatusOverdueBg
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val loginPrefs = remember(context) {
        context.getSharedPreferences("owe_login_prefs", android.content.Context.MODE_PRIVATE)
    }

    LaunchedEffect(Unit) {
        val savedRemember = loginPrefs.getBoolean("remember_me", false)
        val savedEmail = loginPrefs.getString("saved_email", "") ?: ""
        if (savedRemember && savedEmail.isNotBlank()) {
            viewModel.onEmailChanged(savedEmail)
            viewModel.onRememberMeChanged(true)
        }
    }

    val headerAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(32f) }
    val formAlpha = remember { Animatable(0f) }
    val formOffsetY = remember { Animatable(60f) }

    LaunchedEffect(Unit) {
        launch {
            headerAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        launch {
            headerOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        delay(120)
        launch {
            formAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            formOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .displayCutoutPadding()
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
            Spacer(modifier = Modifier.height(32.dp))

            // Centered Brand Header: Owl Logo + OWE + Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = headerAlpha.value
                        translationY = headerOffsetY.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OweLogo(
                    modifier = Modifier.size(150.dp)
                        .padding(end = 10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OWE",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Dark12
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp, top = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LimeAccent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StatusActiveBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "BORROW · TRACK · SETTLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = StatusActiveText
                    )
                }
            }

            // General Error Banner (if any)
            AnimatedVisibility(
                visible = uiState.generalError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(StatusOverdueBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.generalError.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusOverdueText
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = formAlpha.value
                        translationY = formOffsetY.value
                    }
            ) {
            // Email Input
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

            Spacer(modifier = Modifier.height(18.dp))

            // Password Input
            DebtTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Password",
                placeholder = "••••••••",
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login {
                            loginPrefs.edit().apply {
                                if (uiState.rememberMe) {
                                    putBoolean("remember_me", true)
                                    putString("saved_email", uiState.email.trim())
                                } else {
                                    remove("remember_me")
                                    remove("saved_email")
                                }
                            }.apply()
                            onLoginSuccess()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Remember Me & Forgot Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.onRememberMeChanged(!uiState.rememberMe) }
                        .padding(end = 6.dp)
                ) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = viewModel::onRememberMeChanged,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Dark12,
                            uncheckedColor = GrayText,
                            checkmarkColor = White
                        )
                    )
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Dark12
                    )
                }

                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = StatusActiveText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToForgotPassword() }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Login Pill Button (matched to Onboarding CTA)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login {
                        loginPrefs.edit().apply {
                            if (uiState.rememberMe) {
                                putBoolean("remember_me", true)
                                putString("saved_email", uiState.email.trim())
                            } else {
                                remove("remember_me")
                                remove("saved_email")
                            }
                        }.apply()
                        onLoginSuccess()
                    }
                },
                enabled = uiState.canSubmit && !uiState.isLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Dark12,
                    contentColor = White,
                    disabledContainerColor = GrayBorder,
                    disabledContentColor = GrayText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uiState.isLoading) {
                    OwlLoadingSpinner(
                        color = androidx.compose.ui.graphics.Color(0xFF22C55E),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtle Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = GrayBorder
                )
                Text(
                    text = "or",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = GrayBorder
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = StatusActiveText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToRegister() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "DUA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                ),
                color = GrayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    DebtCollectTheme {
        LoginScreen()
    }
}
