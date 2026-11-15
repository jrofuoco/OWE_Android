package xyz.iamdua.owe.ui.debt

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import xyz.iamdua.owe.data.model.DebtScheduleHelper
import xyz.iamdua.owe.data.model.DebtSummaryDto
import xyz.iamdua.owe.data.repository.DebtRepository
import xyz.iamdua.owe.data.repository.PaymentRepository
import xyz.iamdua.owe.data.repository.ProfileRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.AppFeedback
import xyz.iamdua.owe.ui.components.DebtDetailsSkeletonLoader
import xyz.iamdua.owe.ui.components.OwlLoadingSpinner
import xyz.iamdua.owe.ui.components.PrimaryPillButton
import xyz.iamdua.owe.ui.home.DebtStatus
import xyz.iamdua.owe.ui.home.StatusChip
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
import xyz.iamdua.owe.ui.theme.StatusOverdueBg
import xyz.iamdua.owe.ui.theme.StatusOverdueText
import xyz.iamdua.owe.ui.theme.StatusPendingBg
import xyz.iamdua.owe.ui.theme.StatusPendingText
import xyz.iamdua.owe.ui.theme.White
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Immutable
data class PaymentRecordUiModel(
    val id: String,
    val amount: Double,
    val method: String,
    val referenceNo: String? = null,
    val dateText: String,
    val status: PaymentStatus,
    val rejectReason: String? = null,
    val submittedByName: String = "Borrower",
    val note: String? = null
)

enum class PaymentStatus {
    PENDING,
    CONFIRMED,
    REJECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailsScreen(
    debtId: String = "1",
    isLender: Boolean = (debtId != "101" && debtId != "102" && debtId != "103"),
    onNavigateBack: () -> Unit = {},
    onNavigateToSubmitPayment: () -> Unit = {},
    onShareInviteCode: (String) -> Unit = {}
) {
    var selectedReviewPayment by remember { mutableStateOf<PaymentRecordUiModel?>(null) }
    var isReviewing by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val debtRepository = remember { DebtRepository() }
    val profileRepository = remember { ProfileRepository() }
    val paymentRepository = remember { PaymentRepository() }
    val currentUserId = remember { debtRepository.currentUserId() }
    val cachedDebt = remember(debtId) { DebtRepository.getCachedDebt(debtId) }
    val cachedCounterparty = remember(debtId, cachedDebt) {
        if (cachedDebt == null) {
            "Awaiting borrower"
        } else {
            val otherId = if (cachedDebt.lenderId == currentUserId) cachedDebt.borrowerId else cachedDebt.lenderId
            if (otherId == null) {
                "Awaiting borrower"
            } else {
                ProfileRepository.getCachedProfile(otherId)?.fullName?.takeIf { it.isNotBlank() }
                    ?: if (cachedDebt.lenderId == currentUserId) "Borrower" else "Lender"
            }
        }
    }
    val cachedPayments = remember(debtId) {
        PaymentRepository.getCachedPayments(debtId).orEmpty().map { paymentDto ->
            val submitterName = ProfileRepository.getCachedProfile(paymentDto.submittedBy)?.fullName
                ?.takeIf { it.isNotBlank() }
                ?: if (paymentDto.submittedBy == cachedDebt?.borrowerId) cachedCounterparty else "Borrower"
            PaymentRecordUiModel(
                id = paymentDto.id,
                amount = paymentDto.amount,
                method = paymentDto.method.replaceFirstChar { it.uppercase() },
                referenceNo = paymentDto.referenceNo,
                dateText = paymentDto.paymentDate,
                status = when (paymentDto.status) {
                    "confirmed" -> PaymentStatus.CONFIRMED
                    "rejected" -> PaymentStatus.REJECTED
                    else -> PaymentStatus.PENDING
                },
                rejectReason = paymentDto.rejectReason,
                submittedByName = submitterName,
                note = paymentDto.note
            )
        }
    }

    var debtSummary by remember(debtId) { mutableStateOf(cachedDebt) }
    var debtNotes by remember(debtId) { mutableStateOf(DebtRepository.getCachedNotes(debtId)) }
    var counterpartyName by remember(debtId) { mutableStateOf(cachedCounterparty) }
    var payments by remember(debtId) { mutableStateOf(cachedPayments) }
    var isInitialLoading by remember(debtId) { mutableStateOf(cachedDebt == null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingMorePayments by remember(debtId) { mutableStateOf(false) }
    var hasMorePayments by remember(debtId) { mutableStateOf(true) }
    var lastLoadTimeMs by remember { mutableStateOf(0L) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val actualIsLender = debtSummary?.let { it.lenderId == currentUserId } ?: isLender

    fun loadDebtDetails(forceRefresh: Boolean = false) {
        if (isRefreshing) return
        val now = System.currentTimeMillis()
        if (!forceRefresh && now - lastLoadTimeMs < 3_000L) return
        lastLoadTimeMs = now
        scope.launch {
            isRefreshing = true
            try {
                val (dto, mappedCounterparty, mappedPayments, fetchedNotes) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    val (debtResult, paymentsResult, notesResult) = coroutineScope {
                        val debtDeferred = async { debtRepository.getDebtById(debtId) }
                        val paymentsDeferred = async { paymentRepository.getPaymentsForDebt(debtId) }
                        val notesDeferred = async { debtRepository.getDebtNotes(debtId) }
                        Triple(debtDeferred.await(), paymentsDeferred.await(), notesDeferred.await())
                    }

                    val d = debtResult.getOrNull()
                    val paymentList = paymentsResult.getOrNull().orEmpty()
                    val notesVal = if (notesResult.isSuccess) notesResult.getOrNull() else debtNotes

                    val allUserIds = buildList {
                        if (d != null) {
                            add(d.lenderId)
                            d.borrowerId?.let(::add)
                        }
                        paymentList.forEach { add(it.submittedBy) }
                    }.distinct()

                    val profiles = if (allUserIds.isEmpty()) {
                        emptyMap()
                    } else {
                        profileRepository.getProfilesByIds(allUserIds, forceRefresh = forceRefresh).getOrNull().orEmpty()
                    }

                    val cName = if (d != null) {
                        val otherId = if (d.lenderId == currentUserId) d.borrowerId else d.lenderId
                        if (otherId == null) {
                            "Awaiting borrower"
                        } else {
                            profiles[otherId]?.fullName?.takeIf { it.isNotBlank() }
                                ?: if (d.lenderId == currentUserId) "Borrower" else "Lender"
                        }
                    } else {
                        counterpartyName
                    }

                    val pRecords = if (paymentsResult.isSuccess) {
                        paymentList.map { paymentDto ->
                            val submitterName = profiles[paymentDto.submittedBy]?.fullName
                                ?.takeIf { it.isNotBlank() }
                                ?: if (paymentDto.submittedBy == d?.borrowerId) cName else "Borrower"
                            PaymentRecordUiModel(
                                id = paymentDto.id,
                                amount = paymentDto.amount,
                                method = paymentDto.method.replaceFirstChar { it.uppercase() },
                                referenceNo = paymentDto.referenceNo,
                                dateText = DebtScheduleHelper.formatDisplayDate(paymentDto.paymentDate),
                                status = when (paymentDto.status) {
                                    "confirmed" -> PaymentStatus.CONFIRMED
                                    "rejected" -> PaymentStatus.REJECTED
                                    else -> PaymentStatus.PENDING
                                },
                                rejectReason = paymentDto.rejectReason,
                                submittedByName = submitterName,
                                note = paymentDto.note
                            )
                        }
                    } else null

                    LoadedDebtDetails(d, cName, pRecords, notesVal)
                }

                debtNotes = fetchedNotes
                if (dto != null) {
                    debtSummary = dto
                    counterpartyName = mappedCounterparty
                }
                if (mappedPayments != null) {
                    payments = mappedPayments
                    hasMorePayments = mappedPayments.size >= 20
                }
            } finally {
                isRefreshing = false
                isInitialLoading = false
            }
        }
    }

    val shouldLoadMorePayments by remember {
        androidx.compose.runtime.derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMorePayments, hasMorePayments, isLoadingMorePayments) {
        if (shouldLoadMorePayments && hasMorePayments && !isLoadingMorePayments && !isInitialLoading) {
            isLoadingMorePayments = true
            try {
                val nextBatch = paymentRepository.getPaymentsForDebt(
                    debtId = debtId,
                    offset = payments.size,
                    limit = 20
                ).getOrNull().orEmpty()
                hasMorePayments = nextBatch.size >= 20
                if (nextBatch.isNotEmpty()) {
                    val userIds = nextBatch.map { it.submittedBy }.distinct()
                    val profiles = profileRepository.getProfilesByIds(userIds).getOrNull().orEmpty()
                    val existingIds = payments.mapTo(HashSet()) { it.id }
                    val newModels = nextBatch.filter { it.id !in existingIds }.map { paymentDto ->
                        val submitterName = profiles[paymentDto.submittedBy]?.fullName
                            ?.takeIf { it.isNotBlank() }
                            ?: if (paymentDto.submittedBy == debtSummary?.borrowerId) counterpartyName else "Borrower"
                        PaymentRecordUiModel(
                            id = paymentDto.id,
                            amount = paymentDto.amount,
                            method = paymentDto.method.replaceFirstChar { it.uppercase() },
                            referenceNo = paymentDto.referenceNo,
                            dateText = DebtScheduleHelper.formatDisplayDate(paymentDto.paymentDate),
                            status = when (paymentDto.status) {
                                "confirmed" -> PaymentStatus.CONFIRMED
                                "rejected" -> PaymentStatus.REJECTED
                                else -> PaymentStatus.PENDING
                            },
                            rejectReason = paymentDto.rejectReason,
                            submittedByName = submitterName,
                            note = paymentDto.note
                        )
                    }
                    payments = payments + newModels
                }
            } finally {
                isLoadingMorePayments = false
            }
        }
    }

    LaunchedEffect(debtId) {
        loadDebtDetails()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, debtId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isInitialLoading) {
                loadDebtDetails()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Dark12
                    )
                }

                Text(
                    text = debtSummary?.title ?: "Debt Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (debtSummary?.status == "awaiting_borrower") {
                    IconButton(
                        onClick = {
                            val existing = debtSummary?.inviteCode
                            if (!existing.isNullOrBlank()) {
                                onShareInviteCode(existing)
                            } else {
                                scope.launch {
                                    val code = debtRepository.generateInviteCode(debtId).getOrNull() ?: "DC-000000"
                                    onShareInviteCode(code)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(White)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Dark12
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        },
        bottomBar = {
            val pendingPayments = payments.filter { it.status == PaymentStatus.PENDING }

            if (actualIsLender) {
                if (pendingPayments.isNotEmpty()) {
                    Surface(
                        color = LightBg,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            PrimaryPillButton(
                                text = "Review Pending Payment (${pendingPayments.size})",
                                onClick = { selectedReviewPayment = pendingPayments.firstOrNull() }
                            )
                        }
                    }
                } else if (debtSummary?.status == "awaiting_borrower") {
                    Surface(
                        color = LightBg,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            PrimaryPillButton(
                                text = "Share Invite Code",
                                onClick = {
                                    val existing = debtSummary?.inviteCode
                                    if (!existing.isNullOrBlank()) {
                                        onShareInviteCode(existing)
                                    } else {
                                        scope.launch {
                                            val code = debtRepository.generateInviteCode(debtId).getOrNull() ?: "DC-000000"
                                            onShareInviteCode(code)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Borrower view: Primary action to Submit Payment
                Surface(
                    color = LightBg,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        PrimaryPillButton(
                            text = "Submit Payment",
                            onClick = onNavigateToSubmitPayment
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isInitialLoading) {
            DebtDetailsSkeletonLoader(
                modifier = Modifier.padding(innerPadding)
            )
        } else {
        PullToRefreshBox(
            isRefreshing = isRefreshing && !isInitialLoading,
            onRefresh = { loadDebtDetails(forceRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp
            )
        ) {
            // Header Balance Card
            item {
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
                            .padding(22.dp)
                    ) {
                        // Borrower + Status Chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GraySurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = Dark12,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (actualIsLender) "Borrower" else "Lender",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = GrayText,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                    Text(
                                        text = counterpartyName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Dark12,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            val mappedStatus = when {
                                debtSummary?.isOverdue == true || debtSummary?.status == "overdue" -> DebtStatus.OVERDUE
                                debtSummary?.status == "awaiting_borrower" -> DebtStatus.AWAITING
                                debtSummary?.status == "paid" -> DebtStatus.PAID
                                else -> DebtStatus.ACTIVE
                            }
                            StatusChip(status = mappedStatus)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val remaining = debtSummary?.remainingBalance ?: 0.0
                        val total = debtSummary?.totalAmount ?: 0.0
                        val paid = debtSummary?.totalPaid ?: 0.0
                        val progress = if (total > 0.0) (paid / total).toFloat().coerceIn(0f, 1f) else 0f
                        val pct = (progress * 100).toInt()

                        Text(
                            text = "Balance",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GrayText,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = formatDebtCurrency(remaining),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Dark12,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = LimeAccent,
                            trackColor = GraySurface,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paid ${formatDebtCurrency(paid)} of ${formatDebtCurrency(total)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Dark12,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = GrayText,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date given & Next due date info
                        val schedule = remember(debtSummary, debtNotes) {
                            debtSummary?.let { DebtScheduleHelper.computeSchedule(it, debtNotes) }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = GraySurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                val dueTitle = when {
                                    debtSummary?.status == "paid" || remaining <= 0.0 -> "Status"
                                    schedule?.nextDueDate != null && schedule.durationMonths != null -> "Next due"
                                    debtSummary?.dueDate != null -> "Due date"
                                    else -> "Due date"
                                }
                                val dueValue = when {
                                    debtSummary?.status == "paid" || remaining <= 0.0 -> "Paid off"
                                    schedule?.nextDueDate != null && schedule.durationMonths != null ->
                                        DebtScheduleHelper.formatDisplayDate(schedule.nextDueDate)
                                    debtSummary?.dueDate != null -> DebtScheduleHelper.formatDisplayDate(debtSummary?.dueDate)
                                    else -> "No due date"
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Given",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = GrayText,
                                                fontSize = 12.sp
                                            )
                                        )
                                        Text(
                                            text = DebtScheduleHelper.formatDisplayDate(debtSummary?.dateGiven),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Dark12,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dueTitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = GrayText,
                                                fontSize = 12.sp
                                            )
                                        )
                                        Text(
                                            text = dueValue,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Dark12,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (schedule?.durationMonths != null && schedule.monthlyAmount != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${schedule.paidInstallments} of ${schedule.durationMonths} paid",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = GrayText,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${formatDebtCurrency(schedule.monthlyAmount)}/mo",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Dark12,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payments",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark12,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "${payments.size} records",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GrayText,
                            fontSize = 13.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Timeline Items
            items(payments, key = { it.id }) { payment ->
                PaymentItemCard(
                    payment = payment,
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .then(
                            if (actualIsLender && payment.status == PaymentStatus.PENDING) {
                                Modifier.clickable { selectedReviewPayment = payment }
                            } else {
                                Modifier
                            }
                        )
                )
            }
            if (isLoadingMorePayments) {
                item(key = "loading_more_payments") {
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

    // Review Payment Bottom Sheet
    val reviewPayment = selectedReviewPayment
    if (reviewPayment != null) {
        ModalBottomSheet(
            onDismissRequest = { if (!isReviewing) selectedReviewPayment = null },
            sheetState = sheetState,
            containerColor = White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Review Payment",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GraySurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(text = "Amount", style = MaterialTheme.typography.bodySmall.copy(color = GrayText))
                        Text(
                            text = formatDebtCurrency(reviewPayment.amount),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Method: ${reviewPayment.method}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Dark12)
                        )
                        if (!reviewPayment.referenceNo.isNullOrBlank()) {
                            Text(
                                text = "Ref No: ${reviewPayment.referenceNo}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Dark12)
                            )
                        }
                        Text(
                            text = "Date: ${reviewPayment.dateText}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Dark12)
                        )
                        Text(
                            text = "Submitted by: ${reviewPayment.submittedByName}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Dark12)
                        )
                        if (!reviewPayment.note.isNullOrBlank()) {
                            Text(
                                text = "Note: ${reviewPayment.note}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        enabled = !isReviewing,
                        onClick = {
                            val previousPayments = payments
                            val previousSummary = debtSummary
                            val rejectReason = "Rejected by lender"

                            // Optimistic UI update: immediately update payment list & pending count
                            payments = payments.map {
                                if (it.id == reviewPayment.id) {
                                    it.copy(status = PaymentStatus.REJECTED, rejectReason = rejectReason)
                                } else it
                            }
                            debtSummary = debtSummary?.let { summary ->
                                summary.copy(
                                    pendingPaymentsCount = (summary.pendingPaymentsCount - 1).coerceAtLeast(0)
                                ).also { DebtRepository.putCachedDebt(it) }
                            }
                            selectedReviewPayment = null
                            AppFeedback.showSuccess("Payment rejected.")

                            scope.launch {
                                paymentRepository.reviewPayment(
                                    paymentId = reviewPayment.id,
                                    approve = false,
                                    rejectReason = rejectReason
                                ).onSuccess {
                                    loadDebtDetails(forceRefresh = true)
                                }.onFailure { err ->
                                    // Rollback on failure
                                    payments = previousPayments
                                    debtSummary = previousSummary
                                    previousSummary?.let { DebtRepository.putCachedDebt(it) }
                                    AppFeedback.showError(err.localizedMessage ?: "Failed to reject payment")
                                }
                            }
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusOverdueBg,
                            contentColor = StatusOverdueText
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(text = "Reject", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        enabled = !isReviewing,
                        onClick = {
                            val previousPayments = payments
                            val previousSummary = debtSummary

                            // Optimistic UI update: immediately mark confirmed & adjust balance
                            payments = payments.map {
                                if (it.id == reviewPayment.id) {
                                    it.copy(status = PaymentStatus.CONFIRMED)
                                } else it
                            }
                            debtSummary = debtSummary?.let { summary ->
                                val newPaid = summary.totalPaid + reviewPayment.amount
                                val newRemaining = (summary.remainingBalance - reviewPayment.amount).coerceAtLeast(0.0)
                                val newStatus = if (newRemaining <= 0.0) "paid" else summary.status
                                summary.copy(
                                    totalPaid = newPaid,
                                    remainingBalance = newRemaining,
                                    status = newStatus,
                                    pendingPaymentsCount = (summary.pendingPaymentsCount - 1).coerceAtLeast(0)
                                ).also { DebtRepository.putCachedDebt(it) }
                            }
                            selectedReviewPayment = null
                            AppFeedback.showSuccess("Payment confirmed!")

                            scope.launch {
                                paymentRepository.reviewPayment(
                                    paymentId = reviewPayment.id,
                                    approve = true
                                ).onSuccess {
                                    loadDebtDetails(forceRefresh = true)
                                }.onFailure { err ->
                                    // Rollback on failure
                                    payments = previousPayments
                                    debtSummary = previousSummary
                                    previousSummary?.let { DebtRepository.putCachedDebt(it) }
                                    AppFeedback.showError(err.localizedMessage ?: "Failed to confirm payment")
                                }
                            }
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = White
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isReviewing) "Processing..." else "Confirm Payment",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentItemCard(
    payment: PaymentRecordUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDebtCurrency(payment.amount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (payment.referenceNo != null) "${payment.method} • Ref ${payment.referenceNo}" else payment.method,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrayText,
                        fontSize = 13.sp
                    )
                )
                if (payment.rejectReason != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = payment.rejectReason,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StatusOverdueText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = payment.dateText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.5.sp
                    )
                )
            }

            // Status chip for payment
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = when (payment.status) {
                    PaymentStatus.PENDING -> StatusPendingBg
                    PaymentStatus.CONFIRMED -> StatusActiveBg
                    PaymentStatus.REJECTED -> StatusOverdueBg
                }
            ) {
                Text(
                    text = when (payment.status) {
                        PaymentStatus.PENDING -> "Pending"
                        PaymentStatus.CONFIRMED -> "Confirmed"
                        PaymentStatus.REJECTED -> "Rejected"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = when (payment.status) {
                        PaymentStatus.PENDING -> StatusPendingText
                        PaymentStatus.CONFIRMED -> StatusActiveText
                        PaymentStatus.REJECTED -> StatusOverdueText
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private data class LoadedDebtDetails(
    val dto: DebtSummaryDto?,
    val counterpartyName: String,
    val payments: List<PaymentRecordUiModel>?,
    val notes: String?
)

private val DebtCurrencyFormatter = object : ThreadLocal<NumberFormat>() {
    override fun initialValue(): NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

private fun formatDebtCurrency(amount: Double): String {
    return "₱${DebtCurrencyFormatter.get()!!.format(amount)}"
}

@Preview(showBackground = true)
@Composable
fun DebtDetailsScreenPreview() {
    DebtCollectTheme {
        DebtDetailsScreen()
    }
}
