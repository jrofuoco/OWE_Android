package xyz.iamdua.owe.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import xyz.iamdua.owe.data.model.NotificationDto
import xyz.iamdua.owe.data.repository.NotificationStore
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusOverdueBg
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.StatusPendingBg
import xyz.iamdua.owe.ui.theme.StatusPendingText
import xyz.iamdua.owe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {},
    onOpenDebt: (String) -> Unit = {}
) {
    val notifications by NotificationStore.notifications.collectAsState()
    val unreadCount by NotificationStore.unreadCount.collectAsState()
    val hasMore by NotificationStore.hasMore.collectAsState()
    val isLoadingMore by NotificationStore.isLoadingMore.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val shouldLoadMore by remember {
        androidx.compose.runtime.derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 4
        }
    }

    LaunchedEffect(shouldLoadMore, hasMore, isLoadingMore) {
        if (shouldLoadMore && hasMore && !isLoadingMore) {
            NotificationStore.loadMore()
        }
    }

    LaunchedEffect(Unit) {
        NotificationStore.refresh()
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark12
                        )
                    )
                }

                if (unreadCount > 0) {
                    TextButton(
                        onClick = { scope.launch { NotificationStore.markAllAsRead() } }
                    ) {
                        Text(
                            text = "Mark all read",
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                if (!isRefreshing) {
                    scope.launch {
                        isRefreshing = true
                        NotificationStore.refresh()
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(StatusActiveBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You're all caught up",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark12
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Payment updates, invite acceptances, and reminders will show up here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GrayText,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        NotificationItemCard(
                            item = item,
                            onClick = {
                                scope.launch { NotificationStore.markAsRead(item.id) }
                                item.debtId?.let { onOpenDebt(it) }
                            }
                        )
                    }
                    if (isLoadingMore) {
                        item(key = "loading_more_notifications") {
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
}

@Composable
private fun NotificationItemCard(
    item: NotificationDto,
    onClick: () -> Unit
) {
    val (icon, iconBg, iconTint) = notificationVisuals(item.type)
    val cardBg = if (item.isRead) White else StatusActiveBg.copy(alpha = 0.28f)
    val borderColor = if (item.isRead) GrayBorder else ForestGreen.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark12
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayText,
                        lineHeight = 18.sp
                    )
                )

                val dateLabel = item.createdAt?.let { xyz.iamdua.owe.data.model.DebtScheduleHelper.formatDisplayDate(it) }
                if (!dateLabel.isNullOrBlank() && dateLabel != "—") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GrayText.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

private fun notificationVisuals(type: String): Triple<ImageVector, Color, Color> = when (type) {
    "payment_confirmed", "debt_paid" -> Triple(Icons.Rounded.CheckCircle, StatusActiveBg, ForestGreen)
    "payment_rejected", "overdue" -> Triple(Icons.Rounded.Cancel, StatusOverdueBg, StatusOverdueText)
    "payment_submitted" -> Triple(Icons.Rounded.Payments, StatusPendingBg, StatusPendingText)
    "debt_joined" -> Triple(Icons.Rounded.PersonAdd, StatusActiveBg, ForestGreen)
    else -> Triple(Icons.Rounded.NotificationsActive, GraySurface, Dark12)
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    DebtCollectTheme {
        NotificationsScreen()
    }
}
