package xyz.iamdua.owe.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import xyz.iamdua.owe.data.local.ProPreferences
import xyz.iamdua.owe.data.repository.AppConfigStore
import xyz.iamdua.owe.data.repository.AuthRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.OweConfirmDialog
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DarkSurface
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.ForestGreenDark
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.LimeText
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.StatusOverdueBg
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.White

@Composable
fun ProfileScreen(
    fullName: String = "",
    email: String = "",
    phone: String = "",
    onUpgradeToProClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var remindersEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    val context = LocalContext.current
    val isProFlow by ProPreferences.isProFlow.collectAsState()
    val runtimeConfig by AppConfigStore.config.collectAsState()
    val isPro = remember(isProFlow) { ProPreferences.isPro(context) }
    val displayName = fullName.ifBlank { "User" }
    val displayEmail = email.ifBlank { "No email" }
    val displayPhone = phone.ifBlank { "Not set" }
    val initials = remember(fullName) {
        fullName.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "U" }
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Profile & Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12,
                        fontSize = 24.sp
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Profile Summary Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with ForestGreen
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(ForestGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = White,
                                    fontSize = 28.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12,
                                fontSize = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = displayEmail,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayText,
                                fontSize = 13.5.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Verified Chip
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = StatusActiveBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusActiveText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Verified Account",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StatusActiveText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OWE Pro Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpgradeToProClick() },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WorkspacePremium,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPro) "OWE Pro Active" else "Upgrade to OWE Pro",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = White,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isPro) "Unlimited debts & PDF export unlocked" else "Unlimited debts, PDF export & no ads",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFD1FAE5),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = ForestGreenDark
                        ) {
                            Text(
                                text = when {
                                    isPro -> "PRO"
                                    runtimeConfig.isLoaded -> "₱${runtimeConfig.proMonthlyPrice}/mo"
                                    else -> "—"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 4: About
            item {
                SettingsSectionTitle("ABOUT")
                SettingsCardContainer {
                    SettingsRowItem(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Send a message to support",
                        onClick = { showSupportDialog = true }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Outlined.Description,
                        title = "Terms & Privacy Policy",
                        onClick = { showPrivacyDialog = true }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Outlined.AccountCircle,
                        title = "App Version",
                        subtitle = "v1.0.0 (Build 1)"
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Logout Button
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .clickable { showLogoutDialog = true },
                    shape = RoundedCornerShape(100.dp),
                    color = White,
                    border = BorderStroke(1.dp, StatusOverdueBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Logout",
                            tint = StatusOverdueText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StatusOverdueText,
                                fontSize = 15.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        OweConfirmDialog(
            icon = Icons.AutoMirrored.Outlined.Logout,
            title = "Log Out?",
            message = "Are you sure you want to log out of your DebtCollect account?",
            confirmText = "Log Out",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showSupportDialog) {
        SupportTicketDialog(
            userEmail = email,
            onDismiss = { showSupportDialog = false }
        )
    }

    if (showPrivacyDialog) {
        TermsPrivacyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
private fun SupportTicketDialog(
    userEmail: String,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = White,
        title = {
            Text(
                text = "Help & Support",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Dark12
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Describe your issue or question and we'll get back to you.",
                    style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                )
                androidx.compose.material3.OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                enabled = !isSubmitting && subject.isNotBlank() && message.isNotBlank(),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        runCatching {
                            val client = xyz.iamdua.owe.data.remote.SupabaseClientProvider.client
                            val uid = client.auth.currentUserOrNull()?.id
                                ?: error("Please sign in again.")
                            client.from("support_tickets").insert(
                                kotlinx.serialization.json.buildJsonObject {
                                    put("user_id", kotlinx.serialization.json.JsonPrimitive(uid))
                                    put("email", kotlinx.serialization.json.JsonPrimitive(userEmail))
                                    put("subject", kotlinx.serialization.json.JsonPrimitive(subject.trim()))
                                    put("message", kotlinx.serialization.json.JsonPrimitive(message.trim()))
                                }
                            )
                        }.onSuccess {
                            isSubmitting = false
                            xyz.iamdua.owe.ui.components.AppFeedback.showSuccess("Support ticket sent!")
                            onDismiss()
                        }.onFailure { err ->
                            isSubmitting = false
                            xyz.iamdua.owe.ui.components.AppFeedback.showError(
                                err.message ?: "Failed to send support ticket."
                            )
                        }
                    }
                }
            ) {
                Text(if (isSubmitting) "Sending..." else "Send", color = Dark12, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel", color = GrayText)
            }
        }
    )
}

@Composable
private fun TermsPrivacyDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = {
            Text(
                text = "Terms & Privacy Policy",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Dark12
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. Debt Records & Agreements",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Dark12)
                )
                Text(
                    text = "OWE helps you track personal loans, invite codes, and payment confirmations. Recorded entries reflect agreements between you and your counterparty.",
                    style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                )
                Text(
                    text = "2. Data & Privacy",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Dark12)
                )
                Text(
                    text = "Your account details, debt records, and payment proofs are stored securely and used solely to verify your account, connect lenders and borrowers, and sync your ledger.",
                    style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                )
                Text(
                    text = "3. Support & Account Deletion",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Dark12)
                )
                Text(
                    text = "You may contact us anytime via Help & Support for questions, corrections, or account assistance.",
                    style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Close", color = Dark12, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = GrayText,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        ),
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCardContainer(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GraySurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Dark12, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Dark12,
                        fontSize = 14.5.sp
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayText,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GraySurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Dark12, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Dark12,
                        fontSize = 14.5.sp
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayText,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Dark12,
                uncheckedThumbColor = GrayText,
                uncheckedTrackColor = GrayBorder
            )
        )
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp)
            .height(1.dp)
            .background(GrayBorder.copy(alpha = 0.6f))
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    DebtCollectTheme {
        ProfileScreen()
    }
}
