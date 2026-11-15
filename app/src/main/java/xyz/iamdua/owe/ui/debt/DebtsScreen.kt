package xyz.iamdua.owe.ui.debt

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.FreeTierAdBanner
import xyz.iamdua.owe.ui.home.DebtCardItem
import xyz.iamdua.owe.ui.home.DebtFilter
import xyz.iamdua.owe.ui.home.DebtItemUiModel
import xyz.iamdua.owe.ui.home.DebtStatus
import xyz.iamdua.owe.ui.home.EmptyDebtsState
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.White

enum class DebtsTypeFilter(val label: String) {
    ALL("All Debts"),
    LENT("Money Lent"),
    OWED("Money Owed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    debts: List<DebtItemUiModel> = emptyList(),
    isRefreshing: Boolean = false,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onDebtClick: (String) -> Unit = {},
    onCreateDebtClick: () -> Unit = {},
    onJoinDebtClick: () -> Unit = {},
    onUpgradeToProClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf(DebtsTypeFilter.ALL) }
    var selectedStatusFilter by remember { mutableStateOf(DebtFilter.ALL) }
    var isSearchExpanded by remember { mutableStateOf(false) }
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

    val activeCount = remember(debts) { debts.count { it.status == DebtStatus.ACTIVE } }
    val overdueCount = remember(debts) { debts.count { it.status == DebtStatus.OVERDUE } }
    val paidCount = remember(debts) { debts.count { it.status == DebtStatus.PAID } }
    val awaitingLentCount = remember(debts) { debts.count { it.isLent && it.status == DebtStatus.AWAITING } }

    val filteredDebts by remember(debts, searchQuery, selectedTypeFilter, selectedStatusFilter) {
        derivedStateOf {
            debts.filter { item ->
                val typeMatches = when (selectedTypeFilter) {
                    DebtsTypeFilter.ALL -> true
                    DebtsTypeFilter.LENT -> item.isLent
                    DebtsTypeFilter.OWED -> !item.isLent
                }
                val statusMatches = when (selectedStatusFilter) {
                    DebtFilter.ALL -> true
                    DebtFilter.ACTIVE -> item.status == DebtStatus.ACTIVE
                    DebtFilter.OVERDUE -> item.status == DebtStatus.OVERDUE
                    DebtFilter.AWAITING -> item.status == DebtStatus.AWAITING
                    DebtFilter.PAID -> item.status == DebtStatus.PAID
                }
                val queryMatches = searchQuery.isBlank() ||
                        item.personName.contains(searchQuery, ignoreCase = true) ||
                        item.title.contains(searchQuery, ignoreCase = true)

                typeMatches && statusMatches && queryMatches
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "All Debts",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12,
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = "${filteredDebts.size} debts found",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayText,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(White)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Dark12
                            )
                        }
                    }
                }

                if (isSearchExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, title...", color = GrayText) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = Dark12,
                            unfocusedBorderColor = GrayBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateDebtClick,
                containerColor = ForestGreen,
                contentColor = White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(100.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Debt",
                        tint = White
                    )
                },
                text = {
                    Text(
                        text = "New Debt",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            )
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
            // Type Filter Tabs (All, Money Lent, Money Owed)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DebtsTypeFilter.entries.forEach { type ->
                        val isSelected = type == selectedTypeFilter
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { selectedTypeFilter = type },
                            shape = RoundedCornerShape(100.dp),
                            color = if (isSelected) Dark12 else GraySurface,
                            border = if (isSelected) null else BorderStroke(1.dp, GrayBorder)
                        ) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) White else Dark12,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Free Tier Ad Banner (hidden for OWE Pro)
            item {
                FreeTierAdBanner(
                    onUpgradeClick = onUpgradeToProClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Awaiting Borrower quick-filter banner
            if (awaitingLentCount > 0 && (selectedTypeFilter != DebtsTypeFilter.LENT || selectedStatusFilter != DebtFilter.AWAITING)) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedTypeFilter = DebtsTypeFilter.LENT
                                selectedStatusFilter = DebtFilter.AWAITING
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = xyz.iamdua.owe.ui.theme.StatusPendingBg,
                        border = BorderStroke(1.dp, xyz.iamdua.owe.ui.theme.StatusPendingText.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HourglassEmpty,
                                    contentDescription = null,
                                    tint = xyz.iamdua.owe.ui.theme.StatusPendingText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "$awaitingLentCount ${if (awaitingLentCount == 1) "debt" else "debts"} awaiting a borrower",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Dark12
                                        )
                                    )
                                    Text(
                                        text = "Tap to view & share invite code",
                                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                                    )
                                }
                            }
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            )
                        }
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DebtFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedStatusFilter
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { selectedStatusFilter = filter },
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

            // Quick Stats Pill Row
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Active", style = MaterialTheme.typography.bodySmall.copy(color = GrayText))
                            Text(
                                text = "$activeCount",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Dark12)
                            )
                        }
                        Box(modifier = Modifier.size(width = 1.dp, height = 24.dp).background(GrayBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Overdue", style = MaterialTheme.typography.bodySmall.copy(color = GrayText))
                            Text(
                                text = "$overdueCount",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB91C1C)
                                )
                            )
                        }
                        Box(modifier = Modifier.size(width = 1.dp, height = 24.dp).background(GrayBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Fully Paid", style = MaterialTheme.typography.bodySmall.copy(color = GrayText))
                            Text(
                                text = "$paidCount",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Dark12)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Debts List
            if (filteredDebts.isEmpty()) {
                item(key = "empty_debts", contentType = "empty") {
                    EmptyDebtsState(
                        isLentTab = selectedTypeFilter != DebtsTypeFilter.OWED,
                        filter = selectedStatusFilter,
                        onCreateClick = onCreateDebtClick,
                        onJoinClick = onJoinDebtClick,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)
                    )
                }
            } else {
                items(
                    items = filteredDebts,
                    key = { it.id },
                    contentType = { "debt_card" }
                ) { debt ->
                    DebtCardItem(
                        debt = debt,
                        onClick = { onDebtClick(debt.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                if (isLoadingMore) {
                    item(key = "loading_more_debts") {
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

@Preview(showBackground = true)
@Composable
fun DebtsScreenPreview() {
    DebtCollectTheme {
        DebtsScreen()
    }
}
