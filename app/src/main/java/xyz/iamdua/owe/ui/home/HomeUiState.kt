package xyz.iamdua.owe.ui.home

import androidx.compose.runtime.Immutable
import xyz.iamdua.owe.ui.activity.ActivityItemUiModel

enum class DebtTab(val title: String) {
    LENT("Money I Lent"),
    OWE("Money I Owe")
}

enum class DebtFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    OVERDUE("Overdue"),
    AWAITING("Awaiting"),
    PAID("Paid")
}

enum class DebtStatus {
    ACTIVE,
    OVERDUE,
    AWAITING,
    PAID
}

enum class BottomNavItem(val label: String) {
    HOME("Home"),
    DEBTS("Debts"),
    ACTIVITY("Activity"),
    PROFILE("Profile")
}

@Immutable
data class DebtItemUiModel(
    val id: String,
    val personName: String,
    val title: String,
    val remainingAmount: Double,
    val totalAmount: Double,
    val status: DebtStatus,
    val dueDateText: String,
    val isLent: Boolean = true,
    val avatarInitials: String = personName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
) {
    val progress: Float = if (totalAmount > 0) ((totalAmount - remainingAmount) / totalAmount).toFloat().coerceIn(0f, 1f) else 0f
}

@Immutable
data class HomeSummaryUiModel(
    val outstandingAmount: Double = 0.0,
    val weeklyCollectedText: String = "₱0 collected",
    val totalLentAmount: Double = 0.0,
    val totalCollectedAmount: Double = 0.0,
    val totalOwedAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val nextDueDateText: String = "No upcoming due",
    val pendingConfirmationsCount: Int = 0,
    val collectedThisMonthAmount: Double = 0.0
)

@Immutable
data class HomeUiState(
    val userName: String = "",
    val userFullName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val selectedTab: DebtTab = DebtTab.LENT,
    val selectedFilter: DebtFilter = DebtFilter.ACTIVE,
    val searchQuery: String = "",
    val summary: HomeSummaryUiModel = HomeSummaryUiModel(),
    val lentDebts: List<DebtItemUiModel> = emptyList(),
    val owedDebts: List<DebtItemUiModel> = emptyList(),
    val activities: List<ActivityItemUiModel> = emptyList(),
    val selectedBottomNav: BottomNavItem = BottomNavItem.HOME,
    val isRefreshing: Boolean = false,
    val isInitialLoading: Boolean = false,
    val isLoadingMoreDebts: Boolean = false,
    val hasMoreDebts: Boolean = true
) {
    val allDebts: List<DebtItemUiModel> = if (lentDebts.isEmpty() && owedDebts.isEmpty()) emptyList() else lentDebts + owedDebts

    val displayedDebts: List<DebtItemUiModel> = run {
        val list = if (selectedTab == DebtTab.LENT) lentDebts else owedDebts
        val filteredByStatus = when (selectedFilter) {
            DebtFilter.ALL -> list
            DebtFilter.ACTIVE -> list.filter { it.status == DebtStatus.ACTIVE }
            DebtFilter.OVERDUE -> list.filter { it.status == DebtStatus.OVERDUE }
            DebtFilter.AWAITING -> list.filter { it.status == DebtStatus.AWAITING }
            DebtFilter.PAID -> list.filter { it.status == DebtStatus.PAID }
        }
        if (searchQuery.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter {
                it.personName.contains(searchQuery, ignoreCase = true) ||
                        it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}
