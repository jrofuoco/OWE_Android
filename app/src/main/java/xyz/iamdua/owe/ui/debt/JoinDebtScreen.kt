package xyz.iamdua.owe.ui.debt

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import xyz.iamdua.owe.data.model.PreviewDebtDto
import xyz.iamdua.owe.data.repository.DebtRepository
import xyz.iamdua.owe.ui.components.AppFeedback
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DarkSurface
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.LimeText
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun JoinDebtScreen(
    onNavigateBack: () -> Unit = {},
    onDebtAccepted: (String) -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    var previewDebt by remember { mutableStateOf<PreviewDebtDto?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var lookupError by remember { mutableStateOf<String?>(null) }
    var isAccepting by remember { mutableStateOf(false) }

    val isFound = previewDebt != null

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val debtRepository = remember { DebtRepository() }

    fun lookupCode(targetCode: String = code) {
        val cleaned = targetCode.trim().uppercase()
        if (cleaned.isEmpty()) return
        focusManager.clearFocus()
        scope.launch {
            isSearching = true
            lookupError = null
            debtRepository.previewDebtByCode(cleaned)
                .onSuccess { dto ->
                    isSearching = false
                    if (dto != null) {
                        previewDebt = dto
                    } else {
                        previewDebt = null
                        lookupError = "No active invitation found for \"$cleaned\"."
                    }
                }
                .onFailure { err ->
                    isSearching = false
                    previewDebt = null
                    lookupError = err.localizedMessage ?: "Could not look up code."
                }
        }
    }

    fun pasteFromClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text.toString().trim().uppercase()
            code = text
            previewDebt = null
            lookupError = null
            if (text.isNotBlank()) {
                lookupCode(text)
            }
        }
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(White)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Dark12
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Join Debt",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12
                    )
                )
            }
        },
        bottomBar = {
            if (isFound) {
                Surface(
                    color = LightBg,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = White,
                                contentColor = Dark12
                            ),
                            border = BorderStroke(1.dp, GrayBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Text(
                                text = "Decline",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Button(
                            enabled = !isAccepting,
                            onClick = {
                                scope.launch {
                                    isAccepting = true
                                    debtRepository.joinDebt(code)
                                        .onSuccess { joinedDebtId ->
                                            isAccepting = false
                                            AppFeedback.showSuccess("Joined debt successfully!")
                                            onDebtAccepted(joinedDebtId)
                                        }
                                        .onFailure { err ->
                                            isAccepting = false
                                            AppFeedback.showError(err.localizedMessage ?: "Invalid or expired code")
                                        }
                                }
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = White,
                                disabledContainerColor = ForestGreen.copy(alpha = 0.7f),
                                disabledContentColor = White
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(54.dp)
                        ) {
                            if (isAccepting) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Linking...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            } else {
                                Text(
                                    text = "Accept & Link Debt",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Enter the code shared by your lender",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayText,
                    fontSize = 14.5.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Code Input with Paste button
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.uppercase()
                    previewDebt = null
                    lookupError = null
                },
                placeholder = {
                    Text("e.g. DC-7K4P9Q", color = GrayText, fontFamily = FontFamily.Monospace)
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = Dark12,
                    unfocusedBorderColor = GrayBorder
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 20.sp,
                    color = Dark12
                ),
                trailingIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { pasteFromClipboard() },
                        color = GraySurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPaste,
                                contentDescription = "Paste",
                                tint = Dark12,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Dark12
                                )
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { lookupCode() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryPillButton(
                text = "Find Debt",
                isLoading = isSearching,
                enabled = code.trim().length >= 4 && !isSearching,
                onClick = { lookupCode() }
            )

            if (lookupError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = lookupError!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = xyz.iamdua.owe.ui.theme.StatusOverdueText,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Debt Invitation Found Preview
            AnimatedVisibility(
                visible = isFound,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(LimeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = LimeText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Debt Invitation Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = BorderStroke(1.5.dp, GrayBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            // Lender Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lender",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                                )
                                Text(
                                    text = previewDebt?.lenderName ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Dark12
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Title Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Title",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                                )
                                Text(
                                    text = previewDebt?.title ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Dark12
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Total Amount Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Amount",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                                )
                                Text(
                                    text = formatJoinCurrency(previewDebt?.totalAmount ?: 0.0),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Dark12,
                                        fontSize = 22.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Due Date Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Due Date",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                                )
                                Text(
                                    text = previewDebt?.dueDate ?: "No due date",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Dark12
                                    )
                                )
                            }

                            if (!previewDebt?.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))

                                // Note Row
                                Column {
                                    Text(
                                        text = "Note",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = previewDebt?.notes.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Dark12,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatJoinCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "₱${formatter.format(amount)}"
}

@Preview(showBackground = true)
@Composable
fun JoinDebtScreenPreview() {
    DebtCollectTheme {
        JoinDebtScreen()
    }
}
