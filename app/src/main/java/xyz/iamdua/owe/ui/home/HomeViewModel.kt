package xyz.iamdua.owe.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.iamdua.owe.data.model.DebtScheduleHelper
import xyz.iamdua.owe.data.model.DebtSummaryDto
import xyz.iamdua.owe.data.model.PaymentDto
import xyz.iamdua.owe.data.remote.SupabaseClientProvider
import xyz.iamdua.owe.data.repository.DebtRepository
import xyz.iamdua.owe.data.repository.PaymentRepository
import xyz.iamdua.owe.data.repository.ProfileRepository
import xyz.iamdua.owe.ui.activity.ActivityItemUiModel
import xyz.iamdua.owe.ui.activity.ActivityType
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.StatusOverdueBg
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.StatusPaidBg
import xyz.iamdua.owe.ui.theme.StatusPaidText
import xyz.iamdua.owe.ui.theme.StatusPendingBg
import xyz.iamdua.owe.ui.theme.StatusPendingText
import io.github.jan.supabase.auth.auth
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val debtRepository: DebtRepository = DebtRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isInitialLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var lastLoadTimeMs = 0L

    init {
        hydrateFromCacheImmediate()
        loadData()
    }

    private fun hydrateFromCacheImmediate() {
        val uid = debtRepository.currentUserId() ?: return
        val cachedDebts = DebtRepository.getCachedDebts()
        val cachedProfile = ProfileRepository.getCachedProfile(uid)
        if (cachedDebts.isEmpty() && cachedProfile == null) return

        val otherIds = cachedDebts.flatMap { listOfNotNull(it.lenderId, it.borrowerId) }
            .filter { it != uid }
            .distinct()
        val profilesMap = otherIds.mapNotNull { id ->
            ProfileRepository.getCachedProfile(id)?.let { id to it }
        }.toMap()
        val notesMap = DebtRepository.getAllCachedNotes()
        val allPayments = PaymentRepository.getAllCachedPayments()

        applyDataToUiState(
            uid = uid,
            profile = cachedProfile,
            debts = cachedDebts,
            profilesMap = profilesMap,
            allPayments = allPayments,
            notesMap = notesMap,
            isRefreshing = false
        )
    }

    fun selectTab(tab: DebtTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectFilter(filter: DebtFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectBottomNav(item: BottomNavItem) {
        _uiState.update { it.copy(selectedBottomNav = item) }
        if (item == BottomNavItem.PROFILE) {
            refresh(forceProfiles = true)
        }
    }

    fun refresh(forceProfiles: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceProfiles && now - lastLoadTimeMs < 3_000L) return
        loadData(forceProfiles = true)
    }

    fun loadMoreDebts() {
        val current = _uiState.value
        if (current.isLoadingMoreDebts || !current.hasMoreDebts || current.isRefreshing) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            _uiState.update { it.copy(isLoadingMoreDebts = true) }
            try {
                val uid = debtRepository.currentUserId()
                val currentOffset = current.allDebts.size
                val nextBatch = debtRepository.getMyDebts(offset = currentOffset, limit = 25).getOrNull().orEmpty()
                val hasMore = nextBatch.size >= 25
                if (nextBatch.isNotEmpty()) {
                    val otherUserIds = nextBatch.flatMap { listOfNotNull(it.lenderId, it.borrowerId) }
                        .filter { it != uid }
                        .distinct()
                    val debtIds = nextBatch.map { it.id }

                    val (profilesMap, newPayments, notesMap) = coroutineScope {
                        val profilesDeferred = async {
                            if (otherUserIds.isEmpty()) emptyMap()
                            else profileRepository.getProfilesByIds(otherUserIds).getOrNull().orEmpty()
                        }
                        val paymentsDeferred = async {
                            if (debtIds.isEmpty()) emptyList()
                            else paymentRepository.getPaymentsForDebts(debtIds).getOrNull().orEmpty()
                        }
                        val notesDeferred = async {
                            if (debtIds.isEmpty()) emptyMap()
                            else debtRepository.getNotesForDebts(debtIds).getOrNull().orEmpty()
                        }
                        Triple(profilesDeferred.await(), paymentsDeferred.await(), notesDeferred.await())
                    }

                    val allCachedDebts = DebtRepository.getCachedDebts()
                    val allCachedNotes = DebtRepository.getAllCachedNotes() + notesMap
                    val allCachedPayments = PaymentRepository.getAllCachedPayments()
                    val profile = uid?.let { ProfileRepository.getCachedProfile(it) }

                    applyDataToUiState(
                        uid = uid,
                        profile = profile,
                        debts = allCachedDebts,
                        profilesMap = profilesMap,
                        allPayments = allCachedPayments,
                        notesMap = allCachedNotes,
                        isRefreshing = false,
                        hasMoreDebts = hasMore
                    )
                } else {
                    _uiState.update { it.copy(hasMoreDebts = false) }
                }
            } finally {
                _uiState.update { it.copy(isLoadingMoreDebts = false) }
            }
        }
    }

    private fun loadData(forceProfiles: Boolean = true) {
        if (_uiState.value.isRefreshing) return
        lastLoadTimeMs = System.currentTimeMillis()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            _uiState.update { it.copy(isRefreshing = true) }

            val uid = debtRepository.currentUserId()
            val (profile, debts) = coroutineScope {
                val profileDeferred = async {
                    profileRepository.getMyProfile(forceRefresh = forceProfiles).getOrNull()
                }
                val debtsDeferred = async { debtRepository.getMyDebts().getOrNull().orEmpty() }
                profileDeferred.await() to debtsDeferred.await()
            }

            val otherUserIds = debts.flatMap { listOfNotNull(it.lenderId, it.borrowerId) }
                .filter { it != uid }
                .distinct()
            val debtIds = debts.map { it.id }

            val (profilesMap, allPayments, notesMap) = coroutineScope {
                val profilesDeferred = async {
                    if (otherUserIds.isEmpty()) emptyMap()
                    else profileRepository.getProfilesByIds(otherUserIds, forceRefresh = forceProfiles).getOrNull().orEmpty()
                }
                val paymentsDeferred = async {
                    if (debtIds.isEmpty()) emptyList()
                    else paymentRepository.getPaymentsForDebts(debtIds).getOrNull().orEmpty()
                }
                val notesDeferred = async {
                    if (debtIds.isEmpty()) emptyMap()
                    else debtRepository.getNotesForDebts(debtIds, forceRefresh = forceProfiles).getOrNull().orEmpty()
                }
                Triple(profilesDeferred.await(), paymentsDeferred.await(), notesDeferred.await())
            }

            applyDataToUiState(
                uid = uid,
                profile = profile,
                debts = debts,
                profilesMap = profilesMap,
                allPayments = allPayments,
                notesMap = notesMap,
                isRefreshing = false,
                hasMoreDebts = debts.size >= 25
            )
        }
    }

    private fun applyDataToUiState(
        uid: String?,
        profile: xyz.iamdua.owe.data.model.ProfileDto?,
        debts: List<DebtSummaryDto>,
        profilesMap: Map<String, xyz.iamdua.owe.data.model.ProfileDto>,
        allPayments: List<PaymentDto>,
        notesMap: Map<String, String>,
        isRefreshing: Boolean,
        hasMoreDebts: Boolean = _uiState.value.hasMoreDebts
    ) {
        val lentDtos = debts.filter { it.lenderId == uid }
        val owedDtos = debts.filter { it.borrowerId == uid }

        val lentItems = lentDtos.map { dto ->
            val counterparty = dto.borrowerId?.let { profilesMap[it]?.fullName } ?: "Awaiting borrower"
            dto.toUiModel(personName = counterparty, isLent = true, notes = notesMap[dto.id])
        }

        val owedItems = owedDtos.map { dto ->
            val counterparty = profilesMap[dto.lenderId]?.fullName ?: "Lender"
            dto.toUiModel(personName = counterparty, isLent = false, notes = notesMap[dto.id])
        }

        val joinedLentDtos = lentDtos.filter {
            it.borrowerId != null && it.status != "awaiting_borrower" && it.status != "cancelled"
        }
        val totalLent = joinedLentDtos.sumOf { it.totalAmount }
        val totalCollected = joinedLentDtos.sumOf { it.totalPaid }
        val outstanding = joinedLentDtos.sumOf { it.remainingBalance }
        val pendingCount = lentDtos.sumOf { it.pendingPaymentsCount }

        val totalOwed = owedDtos.filter { it.status != "cancelled" }.sumOf { it.remainingBalance }
        val totalPaid = owedDtos.sumOf { it.totalPaid }
        val nextDue = owedDtos
            .filter { it.status == "active" }
            .mapNotNull { dto ->
                val schedule = DebtScheduleHelper.computeSchedule(dto, notesMap[dto.id])
                schedule.nextDueDate?.let { it to schedule.nextDueAmount }
            }
            .minByOrNull { it.first }
            ?.let { (date, amount) ->
                "${DebtScheduleHelper.formatIsoDate(date)} (₱${String.format("%,.0f", amount)})"
            }
            ?: "No upcoming due"

        val currentMonthPrefix = LocalDate.now().toString().substring(0, 7)
        val debtById = debts.associateBy { it.id }
        val collectedThisMonth = allPayments
            .filter { payment ->
                val debt = debtById[payment.debtId]
                debt?.lenderId == uid &&
                    payment.status == "confirmed" &&
                    payment.paymentDate.startsWith(currentMonthPrefix)
            }
            .sumOf { it.amount }

        val activityItems = buildActivityItems(
            debts = debts,
            payments = allPayments,
            profilesMap = profilesMap,
            currentUserId = uid
        )

        val authEmail = SupabaseClientProvider.client.auth.currentUserOrNull()?.email.orEmpty()
        _uiState.update {
            it.copy(
                userName = profile?.fullName?.substringBefore(" ") ?: it.userName.ifBlank { "User" },
                userFullName = profile?.fullName ?: it.userFullName,
                userEmail = authEmail.ifBlank { it.userEmail },
                userPhone = profile?.phone ?: it.userPhone,
                isRefreshing = isRefreshing,
                isInitialLoading = false,
                hasMoreDebts = hasMoreDebts,
                lentDebts = lentItems,
                owedDebts = owedItems,
                activities = activityItems,
                summary = HomeSummaryUiModel(
                    outstandingAmount = outstanding,
                    weeklyCollectedText = "₱${String.format("%,.0f", totalCollected)} collected",
                    totalLentAmount = totalLent,
                    totalCollectedAmount = totalCollected,
                    totalOwedAmount = totalOwed,
                    totalPaidAmount = totalPaid,
                    nextDueDateText = nextDue,
                    pendingConfirmationsCount = pendingCount,
                    collectedThisMonthAmount = collectedThisMonth
                )
            )
        }
    }

    private fun DebtSummaryDto.toUiModel(
        personName: String,
        isLent: Boolean,
        notes: String? = null
    ): DebtItemUiModel {
        val schedule = DebtScheduleHelper.computeSchedule(this, notes)
        val today = LocalDate.now()
        val isInstallmentOverdue = status == "active" &&
            schedule.nextDueDate != null &&
            schedule.nextDueDate.isBefore(today)

        val mappedStatus = when {
            isOverdue || isInstallmentOverdue -> DebtStatus.OVERDUE
            status == "awaiting_borrower" -> DebtStatus.AWAITING
            status == "paid" -> DebtStatus.PAID
            else -> DebtStatus.ACTIVE
        }
        val nextDueStr = schedule.nextDueDate?.let { DebtScheduleHelper.formatDisplayDate(it) }
        val progressSuffix = if (schedule.durationMonths != null && schedule.paidInstallments > 0) {
            " (${schedule.paidInstallments}/${schedule.durationMonths} paid)"
        } else {
            ""
        }
        val dueText = when (mappedStatus) {
            DebtStatus.AWAITING -> "Awaiting borrower"
            DebtStatus.PAID -> "Fully Paid"
            DebtStatus.OVERDUE -> "Overdue · ${nextDueStr ?: dueDate?.let { DebtScheduleHelper.formatDisplayDate(it) } ?: ""}"
            DebtStatus.ACTIVE -> when {
                nextDueStr != null && schedule.durationMonths != null -> "Next due $nextDueStr$progressSuffix"
                nextDueStr != null -> "Due $nextDueStr"
                else -> "No due date"
            }
        }
        return DebtItemUiModel(
            id = id,
            personName = personName,
            title = title,
            remainingAmount = remainingBalance,
            totalAmount = totalAmount,
            status = mappedStatus,
            dueDateText = dueText,
            isLent = isLent
        )
    }

    private fun buildActivityItems(
        debts: List<DebtSummaryDto>,
        payments: List<PaymentDto>,
        profilesMap: Map<String, xyz.iamdua.owe.data.model.ProfileDto>,
        currentUserId: String?
    ): List<ActivityItemUiModel> {
        val debtById = debts.associateBy { it.id }
        val currencyFormatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val paymentEvents = payments.mapNotNull { payment ->
            val debt = debtById[payment.debtId] ?: return@mapNotNull null
            val isLender = debt.lenderId == currentUserId
            val methodLabel = payment.method.replaceFirstChar { it.uppercase() }
            val formattedAmount = "₱${currencyFormatter.format(payment.amount)}"
            val sortKey = payment.createdAt ?: payment.paymentDate

            val (type, title, chipText, chipBg, chipColor) = when (payment.status) {
                "confirmed" -> ActivityEventStyle(
                    type = ActivityType.PAYMENT_CONFIRMED,
                    title = "Payment confirmed",
                    chipText = "Confirmed",
                    chipBg = StatusActiveBg,
                    chipColor = StatusActiveText
                )
                "rejected" -> ActivityEventStyle(
                    type = ActivityType.PAYMENT_REJECTED,
                    title = "Payment rejected",
                    chipText = "Rejected",
                    chipBg = StatusOverdueBg,
                    chipColor = StatusOverdueText
                )
                else -> ActivityEventStyle(
                    type = ActivityType.PAYMENT_SUBMITTED,
                    title = if (isLender) "Payment awaiting review" else "Payment submitted",
                    chipText = "Pending",
                    chipBg = StatusPendingBg,
                    chipColor = StatusPendingText
                )
            }

            sortKey to ActivityItemUiModel(
                id = "payment_${payment.id}",
                debtId = payment.debtId,
                title = title,
                description = "${debt.title} • $methodLabel",
                timestamp = DebtScheduleHelper.formatDisplayDate(payment.paymentDate),
                type = type,
                amountText = if (isLender) "+$formattedAmount" else "-$formattedAmount",
                statusChipText = chipText,
                statusChipBg = chipBg,
                statusChipColor = chipColor
            )
        }

        val debtEvents = debts.map { debt ->
            val isLender = debt.lenderId == currentUserId
            val counterparty = if (isLender) {
                debt.borrowerId?.let { profilesMap[it]?.fullName } ?: "Awaiting borrower"
            } else {
                profilesMap[debt.lenderId]?.fullName ?: "Lender"
            }
            val formattedTotal = "₱${currencyFormatter.format(debt.totalAmount)}"
            val sortKey = debt.createdAt ?: debt.dateGiven

            val (type, title, chipText, chipBg, chipColor) = when {
                debt.status == "paid" -> ActivityEventStyle(
                    type = ActivityType.DEBT_PAID,
                    title = "${debt.title} paid off",
                    chipText = "Paid",
                    chipBg = StatusPaidBg,
                    chipColor = StatusPaidText
                )
                isLender -> ActivityEventStyle(
                    type = ActivityType.DEBT_CREATED,
                    title = "Created ${debt.title}",
                    chipText = if (debt.status == "awaiting_borrower") "Awaiting" else "Active",
                    chipBg = if (debt.status == "awaiting_borrower") StatusPendingBg else StatusActiveBg,
                    chipColor = if (debt.status == "awaiting_borrower") StatusPendingText else StatusActiveText
                )
                else -> ActivityEventStyle(
                    type = ActivityType.DEBT_JOINED,
                    title = "Joined ${debt.title}",
                    chipText = "Joined",
                    chipBg = StatusActiveBg,
                    chipColor = StatusActiveText
                )
            }

            sortKey to ActivityItemUiModel(
                id = "debt_${debt.id}",
                debtId = debt.id,
                title = title,
                description = "$counterparty • $formattedTotal",
                timestamp = DebtScheduleHelper.formatDisplayDate(debt.dateGiven),
                type = type,
                amountText = formattedTotal,
                statusChipText = chipText,
                statusChipBg = chipBg,
                statusChipColor = chipColor
            )
        }

        return (paymentEvents + debtEvents)
            .sortedByDescending { it.first }
            .map { it.second }
    }
}

private data class ActivityEventStyle(
    val type: ActivityType,
    val title: String,
    val chipText: String,
    val chipBg: androidx.compose.ui.graphics.Color,
    val chipColor: androidx.compose.ui.graphics.Color
)
