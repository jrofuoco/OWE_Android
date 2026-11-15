package xyz.iamdua.owe.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import xyz.iamdua.owe.ui.components.FreeTierAdBanner
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
import xyz.iamdua.owe.ui.theme.StatusPaidBg
import xyz.iamdua.owe.ui.theme.StatusPaidText
import xyz.iamdua.owe.ui.theme.StatusPendingBg
import xyz.iamdua.owe.ui.theme.StatusPendingText
import xyz.iamdua.owe.ui.theme.White

enum class ActivityFilter(val label: String) {
    ALL("All Activity"),
    PAYMENTS("Payments"),
    DEBTS("Debts Created"),
    PENDING("Pending")
}

enum class ActivityType {
    PAYMENT_SUBMITTED,
    PAYMENT_CONFIRMED,
    PAYMENT_REJECTED,
    DEBT_JOINED,
    DEBT_CREATED,
    DEBT_PAID
}

@Immutable
data class ActivityItemUiModel(
    val id: String,
    val debtId: String = id,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: ActivityType,
    val amountText: String? = null,
    val statusChipText: String? = null,
    val statusChipBg: Color? = null,
    val statusChipColor: Color? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    activities: List<ActivityItemUiModel> = emptyList(),
    collectedThisMonth: Double = 0.0,
    isRefreshing: Boolean = false,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onUpgradeToProClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(ActivityFilter.ALL) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 4
        }
    }

    androidx.compose.runtime.LaunchedEffect(shouldLoadMore, hasMore, isLoadingMore) {
        if (shouldLoadMore && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    val filteredList by remember(activities, selectedFilter) {
        derivedStateOf {
            when (selectedFilter) {
                ActivityFilter.ALL -> activities
                ActivityFilter.PAYMENTS -> activities.filter {
                    it.type == ActivityType.PAYMENT_SUBMITTED ||
                            it.type == ActivityType.PAYMENT_CONFIRMED ||
                            it.type == ActivityType.PAYMENT_REJECTED
                }
                ActivityFilter.DEBTS -> activities.filter {
                    it.type == ActivityType.DEBT_CREATED ||
                            it.type == ActivityType.DEBT_JOINED ||
                            it.type == ActivityType.DEBT_PAID
                }
                ActivityFilter.PENDING -> activities.filter {
                    it.type == ActivityType.PAYMENT_SUBMITTED
                }
            }
        }
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
                    text = "Recent Activity",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "Timeline of payments, loans, and updates",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrayText,
                        fontSize = 13.sp
                    )
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { selectedFilter = filter },
                            shape = RoundedCornerShape(100.dp),
                            color = if (isSelected) Dark12 else White,
                            border = if (isSelected) null else BorderStroke(1.dp, GrayBorder)
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) White else Dark12,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Overview Volume Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COLLECTED THIS MONTH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFD1FAE5),
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            )
                            Text(
                                text = formatActivityCurrency(collectedThisMonth),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = White,
                                    fontSize = 24.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = ForestGreenDark
                        ) {
                            Text(
                                text = "${filteredList.size} events",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Free Tier Ad Banner (hidden for OWE Pro)
            item {
                FreeTierAdBanner(
                    onUpgradeClick = onUpgradeToProClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Timeline Items
            items(
                items = filteredList,
                key = { it.id },
                contentType = { "activity_card" }
            ) { item ->
                ActivityTimelineItemCard(
                    item = item,
                    onClick = { onItemClick(item.debtId) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
            if (isLoadingMore) {
                item(key = "loading_more_activity") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ForestGreen,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun ActivityTimelineItemCard(
    item: ActivityItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Indicator
            val (icon, iconBg, iconTint) = when (item.type) {
                ActivityType.PAYMENT_SUBMITTED -> Triple(Icons.Rounded.Schedule, Color(0xFFFEF3C7), Color(0xFFD97706))
                ActivityType.PAYMENT_CONFIRMED -> Triple(Icons.Rounded.Payments, StatusActiveBg, StatusActiveText)
                ActivityType.PAYMENT_REJECTED -> Triple(Icons.Rounded.Close, StatusOverdueBg, StatusOverdueText)
                ActivityType.DEBT_JOINED -> Triple(Icons.Rounded.PersonAdd, GraySurface, Dark12)
                ActivityType.DEBT_CREATED -> Triple(Icons.Rounded.ReceiptLong, GraySurface, Dark12)
                ActivityType.DEBT_PAID -> Triple(Icons.Rounded.CheckCircle, StatusPaidBg, StatusPaidText)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark12,
                            fontSize = 15.sp
                        )
                    )

                    if (item.statusChipText != null && item.statusChipBg != null && item.statusChipColor != null) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = item.statusChipBg
                        ) {
                            Text(
                                text = item.statusChipText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = item.statusChipColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrayText,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA1A1AA),
                            fontSize = 11.5.sp
                        )
                    )

                    if (item.amountText != null) {
                        Text(
                            text = item.amountText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (item.type == ActivityType.PAYMENT_REJECTED) StatusOverdueText else Dark12,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

private val ActivityCurrencyFormatter = object : ThreadLocal<NumberFormat>() {
    override fun initialValue(): NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

private fun formatActivityCurrency(amount: Double): String {
    return "₱${ActivityCurrencyFormatter.get()!!.format(amount)}"
}

@Preview(showBackground = true)
@Composable
fun ActivityScreenPreview() {
    DebtCollectTheme {
        ActivityScreen()
    }
}
