package xyz.iamdua.owe.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import xyz.iamdua.owe.data.repository.AnnouncementDto
import xyz.iamdua.owe.data.repository.AppConfigStore
import xyz.iamdua.owe.data.repository.NotificationStore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.iamdua.owe.ui.components.HomeSkeletonLoader
import xyz.iamdua.owe.ui.components.OwlLoadingSpinner
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
import xyz.iamdua.owe.ui.activity.ActivityScreen
import xyz.iamdua.owe.ui.components.FreeTierAdBanner
import xyz.iamdua.owe.ui.debt.DebtsScreen
import xyz.iamdua.owe.ui.profile.ProfileScreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToCreateDebt: () -> Unit = {},
    onNavigateToJoinDebt: () -> Unit = {},
    onNavigateToDebtDetails: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProUpgrade: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onTabSelected = viewModel::selectTab,
        onFilterSelected = viewModel::selectFilter,
        onBottomNavSelected = viewModel::selectBottomNav,
        onDebtClick = onNavigateToDebtDetails,
        onBannerClick = onNavigateToNotifications,
        onCreateDebtClick = onNavigateToCreateDebt,
        onJoinDebtClick = onNavigateToJoinDebt,
        onProfileClick = { viewModel.selectBottomNav(BottomNavItem.PROFILE) },
        onNotificationsClick = onNavigateToNotifications,
        onNavigateToProUpgrade = onNavigateToProUpgrade,
        onRefresh = { viewModel.refresh(forceProfiles = true) },
        onLoadMoreDebts = viewModel::loadMoreDebts,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onTabSelected: (DebtTab) -> Unit = {},
    onFilterSelected: (DebtFilter) -> Unit = {},
    onBottomNavSelected: (BottomNavItem) -> Unit = {},
    onDebtClick: (String) -> Unit = {},
    onBannerClick: () -> Unit = {},
    onCreateDebtClick: () -> Unit = {},
    onJoinDebtClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onNavigateToProUpgrade: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onLoadMoreDebts: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        containerColor = LightBg,
        topBar = {
            if (uiState.selectedBottomNav == BottomNavItem.HOME) {
                HomeTopBar(
                    userName = uiState.userName,
                    pendingCount = uiState.summary.pendingConfirmationsCount,
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick
                )
            }
        },
        floatingActionButton = {
            if (uiState.selectedBottomNav == BottomNavItem.HOME) {
                if (uiState.selectedTab == DebtTab.LENT) {
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
                } else {
                    ExtendedFloatingActionButton(
                        onClick = onJoinDebtClick,
                        containerColor = ForestGreen,
                        contentColor = White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(100.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = "Join Debt",
                                tint = White
                            )
                        },
                        text = {
                            Text(
                                text = "Join Debt",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    )
                }
            }
        },
        bottomBar = {
            HomeBottomNavigationBar(
                selectedItem = uiState.selectedBottomNav,
                onItemSelected = onBottomNavSelected
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.selectedBottomNav,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 150)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 120))
            },
            label = "TabScreenTransition",
            modifier = Modifier.fillMaxSize()
        ) { targetNav ->
            when (targetNav) {
                BottomNavItem.HOME -> {
                    if (uiState.isInitialLoading) {
                        HomeSkeletonLoader(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing && !uiState.isInitialLoading,
                            onRefresh = onRefresh,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // 0. Dynamic Announcement Banner (if any active in Supabase)
                        item {
                            val announcements by AppConfigStore.announcements.collectAsState()
                            var dismissedIds by remember { mutableStateOf(emptySet<String>()) }
                            val activeAnnouncement = announcements.firstOrNull { it.id !in dismissedIds }
                            if (activeAnnouncement != null) {
                                AnnouncementCard(
                                    announcement = activeAnnouncement,
                                    onDismiss = { dismissedIds = dismissedIds + activeAnnouncement.id },
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // 1. Segmented Lent / Owe Tab Switcher
                        item {
                            HomeTabSwitcher(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = onTabSelected,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        // 2. Outstanding Hero Summary Card
                        item {
                            HomeHeroCard(
                                uiState = uiState,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        // 2b. Free Tier Ad Banner (hidden for OWE Pro)
                        item {
                            FreeTierAdBanner(
                                onUpgradeClick = onNavigateToProUpgrade,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }

                        // 3. Pending Confirmation Banner (if any)
                        if (uiState.selectedTab == DebtTab.LENT && uiState.summary.pendingConfirmationsCount > 0) {
                            item {
                                PendingConfirmationBanner(
                                    pendingCount = uiState.summary.pendingConfirmationsCount,
                                    onClick = onBannerClick,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // 4. Filter Chips
                        item {
                            FilterChipsRow(
                                selectedFilter = uiState.selectedFilter,
                                onFilterSelected = onFilterSelected,
                                modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
                            )
                        }

                        // 5. Debt Cards List
                        if (uiState.displayedDebts.isEmpty()) {
                            item(key = "empty_debts", contentType = "empty") {
                                EmptyDebtsState(
                                    isLentTab = uiState.selectedTab == DebtTab.LENT,
                                    filter = uiState.selectedFilter,
                                    onCreateClick = onCreateDebtClick,
                                    onJoinClick = onJoinDebtClick,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)
                                )
                            }
                        } else {
                            items(
                                items = uiState.displayedDebts,
                                key = { it.id },
                                contentType = { "debt_card" }
                            ) { debt ->
                                DebtCardItem(
                                    debt = debt,
                                    onClick = { onDebtClick(debt.id) },
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }
                        }
                        }
                        }
                    }
                }
                BottomNavItem.DEBTS -> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        DebtsScreen(
                            debts = uiState.allDebts,
                            isRefreshing = uiState.isRefreshing && !uiState.isInitialLoading,
                            isLoadingMore = uiState.isLoadingMoreDebts,
                            hasMore = uiState.hasMoreDebts,
                            onLoadMore = onLoadMoreDebts,
                            onRefresh = onRefresh,
                            onDebtClick = onDebtClick,
                            onCreateDebtClick = onCreateDebtClick,
                            onJoinDebtClick = onJoinDebtClick,
                            onUpgradeToProClick = onNavigateToProUpgrade
                        )
                    }
                }
                BottomNavItem.ACTIVITY -> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ActivityScreen(
                            activities = uiState.activities,
                            collectedThisMonth = uiState.summary.collectedThisMonthAmount,
                            isRefreshing = uiState.isRefreshing && !uiState.isInitialLoading,
                            isLoadingMore = uiState.isLoadingMoreDebts,
                            hasMore = uiState.hasMoreDebts,
                            onLoadMore = onLoadMoreDebts,
                            onRefresh = onRefresh,
                            onItemClick = onDebtClick,
                            onUpgradeToProClick = onNavigateToProUpgrade
                        )
                    }
                }
                BottomNavItem.PROFILE -> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ProfileScreen(
                            fullName = uiState.userFullName,
                            email = uiState.userEmail,
                            phone = uiState.userPhone,
                            onUpgradeToProClick = onNavigateToProUpgrade,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ANNOUNCEMENT CARD
// ---------------------------------------------------------------------------
@Composable
fun AnnouncementCard(
    announcement: AnnouncementDto,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (announcement.type) {
        "warning", "maintenance" -> StatusOverdueBg
        "promo" -> StatusActiveBg
        else -> Color(0xFFEFF6FF)
    }
    val accentColor = when (announcement.type) {
        "warning", "maintenance" -> StatusOverdueText
        "promo" -> ForestGreen
        else -> Color(0xFF1D4ED8)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = announcement.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Dark12,
                        lineHeight = 18.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Dismiss",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// TOP BAR
// ---------------------------------------------------------------------------
@Composable
fun HomeTopBar(
    userName: String,
    pendingCount: Int,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onProfileClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            // User Avatar Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrayText,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = "Hi, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Dark12
                    )
                )
            }
        }

        // Notification Icon with Badge
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(White)
        ) {
            val unreadNotifications by NotificationStore.unreadCount.collectAsState()
            val totalBadge = unreadNotifications + pendingCount
            androidx.compose.runtime.LaunchedEffect(Unit) {
                NotificationStore.refresh()
            }
            BadgedBox(
                badge = {
                    if (totalBadge > 0) {
                        Badge(
                            containerColor = StatusOverdueText,
                            contentColor = White,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Dark12,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB SWITCHER (Lent / Owe)
// ---------------------------------------------------------------------------
@Composable
fun HomeTabSwitcher(
    selectedTab: DebtTab,
    onTabSelected: (DebtTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(100.dp),
        color = GraySurface,
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DebtTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) Dark12 else Color.Transparent,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) White else GrayText,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .background(containerColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HERO SUMMARY CARD
// ---------------------------------------------------------------------------
@Composable
fun HomeHeroCard(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ForestGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            if (uiState.selectedTab == DebtTab.LENT) {
                // LENT HERO CARD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OUTSTANDING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFD1FAE5),
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )

                    // Trend badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = ForestGreenDark
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.summary.weeklyCollectedText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Outstanding amount
                Text(
                    text = formatCurrency(uiState.summary.outstandingAmount),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Collected progress bar
                val progress = if (uiState.summary.totalLentAmount > 0) {
                    (uiState.summary.totalCollectedAmount / uiState.summary.totalLentAmount).toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = White,
                    trackColor = ForestGreenDark,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sub-metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Lent",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(uiState.summary.totalLentAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFD4D4D8),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Collected",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFD1FAE5),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(uiState.summary.totalCollectedAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // OWE HERO CARD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL OWED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFD1FAE5),
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = ForestGreenDark
                    ) {
                        Text(
                            text = "Next: ${uiState.summary.nextDueDateText}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = formatCurrency(uiState.summary.totalOwedAmount),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                val oweProgress = if (uiState.summary.totalOwedAmount + uiState.summary.totalPaidAmount > 0) {
                    (uiState.summary.totalPaidAmount / (uiState.summary.totalOwedAmount + uiState.summary.totalPaidAmount)).toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { oweProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = White,
                    trackColor = ForestGreenDark,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Paid",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(uiState.summary.totalPaidAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFD4D4D8),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFD1FAE5),
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(uiState.summary.totalOwedAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PENDING CONFIRMATION BANNER
// ---------------------------------------------------------------------------
@Composable
fun PendingConfirmationBanner(
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "$pendingCount payments waiting for confirmation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Review",
                tint = Color(0xFFB45309),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// FILTER CHIPS ROW
// ---------------------------------------------------------------------------
@Composable
fun FilterChipsRow(
    selectedFilter: DebtFilter,
    onFilterSelected: (DebtFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DebtFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(100.dp),
                color = if (isSelected) Dark12 else White,
                border = if (isSelected) null else BorderStroke(1.dp, GrayBorder),
                shadowElevation = if (isSelected) 2.dp else 0.dp
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

// ---------------------------------------------------------------------------
// DEBT CARD ITEM
// ---------------------------------------------------------------------------
@Composable
fun DebtCardItem(
    debt: DebtItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Avatar + Name + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GraySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = debt.avatarInitials,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Dark12
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = debt.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayText,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                StatusChip(status = debt.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount and Due Date
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formatCurrency(debt.remainingAmount),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Dark12
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "of ${formatCurrency(debt.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayText,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = if (debt.status == DebtStatus.OVERDUE) StatusOverdueText else GrayText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = debt.dueDateText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (debt.status == DebtStatus.OVERDUE) StatusOverdueText else GrayText,
                            fontWeight = if (debt.status == DebtStatus.OVERDUE) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini progress bar
            LinearProgressIndicator(
                progress = { debt.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = when (debt.status) {
                    DebtStatus.OVERDUE -> StatusOverdueText
                    DebtStatus.PAID -> StatusPaidText
                    else -> LimeAccent
                },
                trackColor = GraySurface,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STATUS CHIP
// ---------------------------------------------------------------------------
@Composable
fun StatusChip(
    status: DebtStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        DebtStatus.ACTIVE -> Triple(StatusActiveBg, StatusActiveText, "Active")
        DebtStatus.OVERDUE -> Triple(StatusOverdueBg, StatusOverdueText, "Overdue")
        DebtStatus.AWAITING -> Triple(StatusPendingBg, StatusPendingText, "Awaiting")
        DebtStatus.PAID -> Triple(StatusPaidBg, StatusPaidText, "Paid")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// EMPTY STATE
// ---------------------------------------------------------------------------
@Composable
fun EmptyDebtsState(
    isLentTab: Boolean,
    filter: DebtFilter,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GraySurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLentTab) Icons.Outlined.ReceiptLong else Icons.Rounded.Key,
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (filter == DebtFilter.ALL) {
                if (isLentTab) "No debts recorded yet" else "No debts owed yet"
            } else {
                "No ${filter.label.lowercase()} debts"
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Dark12
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isLentTab) {
                "Start tracking money you lend to friends or family."
            } else {
                "Join an existing debt with an invite code from your lender."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = GrayText,
                fontSize = 13.5.sp
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// FLOATING CAPSULE BOTTOM NAVIGATION BAR (iOS / Modern Pill Style)
// ---------------------------------------------------------------------------
private val ActivePillGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1B8A42),
        Color(0xFF137333)
    )
)

@Composable
fun HomeBottomNavigationBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = White,
            border = BorderStroke(1.dp, Color(0xFFECEEF2)),
            shadowElevation = 6.dp,
            tonalElevation = 0.dp,
            modifier = Modifier.wrapContentWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BottomNavItem.entries.forEach { item ->
                    val isSelected = item == selectedItem
                    val icon = when (item) {
                        BottomNavItem.HOME -> if (isSelected) Icons.Rounded.Home else Icons.Outlined.Home
                        BottomNavItem.DEBTS -> if (isSelected) Icons.Rounded.ReceiptLong else Icons.Outlined.ReceiptLong
                        BottomNavItem.ACTIVITY -> Icons.Rounded.CalendarToday
                        BottomNavItem.PROFILE -> if (isSelected) Icons.Rounded.Person else Icons.Outlined.Person
                    }

                    if (isSelected) {
                        // Expanded green pill for the active tab
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(brush = ActivePillGradient)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onItemSelected(item) }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = item.label,
                                    tint = White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = White
                                )
                            }
                        }
                    } else {
                        // Minimalist circular icon for inactive tabs
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onItemSelected(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.label,
                                tint = Dark12,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CURRENCY FORMATTER
// ---------------------------------------------------------------------------
private val CurrencyFormatter = object : ThreadLocal<NumberFormat>() {
    override fun initialValue(): NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

private fun formatCurrency(amount: Double): String {
    return "₱${CurrencyFormatter.get()!!.format(amount)}"
}

// ---------------------------------------------------------------------------
// PREVIEWS
// ---------------------------------------------------------------------------
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenPreview() {
    DebtCollectTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                userName = "Juan",
                selectedTab = DebtTab.LENT,
                selectedFilter = DebtFilter.ACTIVE,
                summary = HomeSummaryUiModel(
                    outstandingAmount = 48500.0,
                    weeklyCollectedText = "+₱3,000 collected this week",
                    totalLentAmount = 75000.0,
                    totalCollectedAmount = 26500.0,
                    totalOwedAmount = 16500.0,
                    totalPaidAmount = 6000.0,
                    nextDueDateText = "Dec 20 (₱4,000)",
                    pendingConfirmationsCount = 2
                ),
                lentDebts = listOf(
                    DebtItemUiModel(
                        id = "1",
                        personName = "Maria Santos",
                        title = "Motorcycle loan",
                        remainingAmount = 12000.0,
                        totalAmount = 25000.0,
                        status = DebtStatus.ACTIVE,
                        dueDateText = "Due Dec 15",
                        isLent = true
                    ),
                    DebtItemUiModel(
                        id = "2",
                        personName = "Carlo Reyes",
                        title = "Emergency cash",
                        remainingAmount = 8500.0,
                        totalAmount = 10000.0,
                        status = DebtStatus.OVERDUE,
                        dueDateText = "Overdue · Nov 2",
                        isLent = true
                    ),
                    DebtItemUiModel(
                        id = "3",
                        personName = "Ana Cruz",
                        title = "Laptop share",
                        remainingAmount = 5000.0,
                        totalAmount = 15000.0,
                        status = DebtStatus.AWAITING,
                        dueDateText = "Awaiting borrower",
                        isLent = true
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenOwePreview() {
    DebtCollectTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                userName = "Juan",
                selectedTab = DebtTab.OWE,
                selectedFilter = DebtFilter.ACTIVE,
                summary = HomeSummaryUiModel(
                    outstandingAmount = 48500.0,
                    totalOwedAmount = 16500.0,
                    totalPaidAmount = 6000.0,
                    nextDueDateText = "Dec 20 (₱4,000)"
                ),
                owedDebts = listOf(
                    DebtItemUiModel(
                        id = "101",
                        personName = "David Kim",
                        title = "Apartment deposit",
                        remainingAmount = 10000.0,
                        totalAmount = 15000.0,
                        status = DebtStatus.ACTIVE,
                        dueDateText = "Due Dec 20",
                        isLent = false
                    ),
                    DebtItemUiModel(
                        id = "102",
                        personName = "Lisa Vance",
                        title = "Group trip flight",
                        remainingAmount = 6500.0,
                        totalAmount = 6500.0,
                        status = DebtStatus.ACTIVE,
                        dueDateText = "Due Jan 5",
                        isLent = false
                    )
                )
            )
        )
    }
}
