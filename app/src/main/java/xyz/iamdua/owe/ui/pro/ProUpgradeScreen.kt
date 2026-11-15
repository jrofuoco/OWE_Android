package xyz.iamdua.owe.ui.pro

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.iamdua.owe.data.local.ProPlan
import xyz.iamdua.owe.data.local.ProPreferences
import xyz.iamdua.owe.data.repository.AppConfigStore
import xyz.iamdua.owe.ui.components.AppFeedback
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.ForestGreenDark
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.White

private data class ProBenefit(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun ProUpgradeScreen(
    onNavigateBack: () -> Unit = {},
    onProActivated: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val runtimeConfig by AppConfigStore.config.collectAsState()

    LaunchedEffect(Unit) {
        AppConfigStore.refresh()
    }

    var isPro by remember { mutableStateOf(ProPreferences.isPro(context)) }
    var activePlan by remember { mutableStateOf(ProPreferences.getPlan(context)) }
    var selectedPlan by remember { mutableStateOf(ProPlan.LIFETIME) }
    var isProcessing by remember { mutableStateOf(false) }

    fun priceLabelFor(plan: ProPlan): String = when (plan) {
        ProPlan.MONTHLY -> if (runtimeConfig.isLoaded) "₱${runtimeConfig.proMonthlyPrice}" else "—"
        ProPlan.LIFETIME -> if (runtimeConfig.isLoaded) "₱${runtimeConfig.proLifetimePrice}" else "—"
    }

    val benefits = remember(runtimeConfig.freeActiveDebtLimit) {
        listOf(
            ProBenefit(
                icon = Icons.Outlined.AllInclusive,
                title = "Unlimited Active Debts",
                description = "Free accounts cap at ${runtimeConfig.freeActiveDebtLimit} active debts. Track as many loans as you need."
            ),
            ProBenefit(
                icon = Icons.Outlined.PictureAsPdf,
                title = "Export Statement as PDF",
                description = "Generate and share clean PDF repayment statements anytime."
            ),
            ProBenefit(
                icon = Icons.Outlined.ReceiptLong,
                title = "Payment Proof Uploads",
                description = "Attach GCash, Maya, or bank transfer receipt screenshots."
            ),
            ProBenefit(
                icon = Icons.Outlined.NotificationsActive,
                title = "Automated Due-Date Reminders",
                description = "Get alerted 3 days before, on due date, and when overdue."
            ),
            ProBenefit(
                icon = Icons.Outlined.Block,
                title = "Ad-Free Experience",
                description = "Zero ads across all screens for faster tracking."
            )
        )
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

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = StatusActiveBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OWE PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }
        },
        bottomBar = {
            if (!isPro) {
                Surface(
                    color = LightBg,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PrimaryPillButton(
                            text = "Upgrade to Pro — ${priceLabelFor(selectedPlan)}",
                            isLoading = isProcessing,
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    delay(500)
                                    ProPreferences.activatePro(context, selectedPlan)
                                    isPro = true
                                    activePlan = selectedPlan
                                    isProcessing = false
                                    AppFeedback.showSuccess("OWE Pro (${selectedPlan.title}) unlocked!")
                                    onProActivated()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = {
                                val existing = ProPreferences.getPlan(context)
                                if (existing != null) {
                                    ProPreferences.activatePro(context, existing)
                                    isPro = true
                                    activePlan = existing
                                    AppFeedback.showSuccess("Purchase restored!")
                                    onProActivated()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No previous Pro purchase found on this device.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !isProcessing
                        ) {
                            Text(
                                text = "Restore Purchase",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = ForestGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Hero Forest Green Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ForestGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ForestGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPro) "You're on OWE Pro" else "Upgrade to OWE Pro",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPro) {
                            "Active plan: ${activePlan?.title ?: "Pro"} • All premium features are unlocked."
                        } else {
                            "Remove the ${runtimeConfig.freeActiveDebtLimit}-debt limit, export PDF statements, and track repayments without ads."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFD1FAE5),
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isPro) {
                Text(
                    text = "CHOOSE YOUR PLAN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GrayText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProPlan.entries.forEach { plan ->
                        val isSelected = selectedPlan == plan
                        val borderColor = if (isSelected) ForestGreen else GrayBorder
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedPlan = plan },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StatusActiveBg.copy(alpha = 0.45f) else White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = plan.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Dark12
                                            )
                                        )
                                        if (plan == ProPlan.LIFETIME) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(100.dp),
                                                color = ForestGreen
                                            ) {
                                                Text(
                                                    text = "BEST VALUE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = plan.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                                    )
                                }

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = priceLabelFor(plan),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = plan.periodLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }

            // Benefits Section
            Text(
                text = "WHAT'S INCLUDED IN PRO",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GrayText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusActiveBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = benefit.icon,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = benefit.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Dark12,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = benefit.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GrayText,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                            if (isPro) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isPro) {
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedButton(
                    onClick = {
                        ProPreferences.clearPro(context)
                        isPro = false
                        activePlan = null
                        Toast.makeText(context, "Switched back to Free plan.", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, GrayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Switch to Free Plan (Preview)",
                        color = GrayText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProUpgradeScreenPreview() {
    DebtCollectTheme {
        ProUpgradeScreen()
    }
}
