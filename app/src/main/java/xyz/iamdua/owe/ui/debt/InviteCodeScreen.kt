package xyz.iamdua.owe.ui.debt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DarkSurface
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
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
fun InviteCodeScreen(
    initialCode: String = "DC-7K4P9Q",
    onNavigateBack: () -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    var code by remember { mutableStateOf(initialCode) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun copyCodeToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DebtCollect Invite Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied code $code to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareCode() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "You've been invited to a debt in DebtCollect. Join using invite code: $code\nhttps://debtcollect.app/join?code=$code"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Debt Code via")
        context.startActivity(shareIntent)
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Dark12
                    )
                }

                Text(
                    text = "Share Invite Code",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12
                    )
                )

                Spacer(modifier = Modifier.size(40.dp))
            }
        },
        bottomBar = {
            Surface(
                color = LightBg,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    PrimaryPillButton(
                        text = "Done & View Debt",
                        onClick = onDoneClick
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Success Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(LimeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = LimeText,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Debt Created Successfully",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Dark12
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Share this code with the borrower so they can join and view repayment terms.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrayText,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Big Code Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.5.dp, GrayBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BORROWER INVITE CODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GrayText,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Code with Copy Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { copyCodeToClipboard() },
                        color = GraySurface,
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp,
                                    color = Dark12,
                                    fontSize = 26.sp
                                )
                            )

                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = Dark12,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expiry info
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = GrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Expires in 7 days (Nov 19)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Share via Section
            Text(
                text = "Share via",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Dark12,
                    fontSize = 15.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShareChannelButton(
                    label = "Messenger",
                    icon = Icons.Outlined.Message,
                    onClick = { shareCode() }
                )
                ShareChannelButton(
                    label = "WhatsApp",
                    icon = Icons.Outlined.Share,
                    onClick = { shareCode() }
                )
                ShareChannelButton(
                    label = "SMS",
                    icon = Icons.Outlined.Message,
                    onClick = { shareCode() }
                )
                ShareChannelButton(
                    label = "Copy Link",
                    icon = Icons.Outlined.Link,
                    onClick = { copyCodeToClipboard() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Regenerate Code button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable {
                        code = "DC-" + (100000..999999).random().toString(36).uppercase()
                        Toast.makeText(context, "Code regenerated: $code", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(100.dp),
                color = White,
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = Dark12,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Regenerate Code",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Dark12,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ShareChannelButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(White)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Dark12,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Dark12
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InviteCodeScreenPreview() {
    DebtCollectTheme {
        InviteCodeScreen()
    }
}
