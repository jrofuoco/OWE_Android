package xyz.iamdua.owe.ui.payment

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import xyz.iamdua.owe.data.model.DebtScheduleHelper
import xyz.iamdua.owe.data.repository.DebtRepository
import xyz.iamdua.owe.data.repository.PaymentRepository
import xyz.iamdua.owe.data.repository.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.AppFeedback
import xyz.iamdua.owe.ui.components.DebtTextField
import xyz.iamdua.owe.ui.components.OwlLoadingSpinner
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
import xyz.iamdua.owe.ui.theme.White

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    GCASH("GCash"),
    MAYA("Maya"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}

@Composable
fun SubmitPaymentScreen(
    debtId: String = "",
    lenderName: String = "Lender",
    debtTitle: String = "",
    currentBalance: Double = 0.0,
    onNavigateBack: () -> Unit = {},
    onPaymentSubmitted: () -> Unit = {}
) {
    val cachedDebt = remember(debtId) {
        if (debtId.isBlank()) null else DebtRepository.getCachedDebt(debtId)
    }
    val cachedLenderName = remember(debtId, cachedDebt) {
        cachedDebt?.let { ProfileRepository.getCachedProfile(it.lenderId)?.fullName?.takeIf { n -> n.isNotBlank() } }
            ?: lenderName
    }
    val cachedPrefillAmount = remember(debtId, cachedDebt) {
        if (cachedDebt == null) {
            ""
        } else {
            val cachedNotes = DebtRepository.getCachedNotes(debtId)
            val monthlyAmount = DebtScheduleHelper.parseMonthlyInstallment(cachedNotes)
            val prefill = when {
                monthlyAmount != null && monthlyAmount > 0.0 -> {
                    if (cachedDebt.remainingBalance > 0.0) minOf(monthlyAmount, cachedDebt.remainingBalance)
                    else monthlyAmount
                }
                cachedDebt.remainingBalance > 0.0 -> cachedDebt.remainingBalance
                else -> null
            }
            if (prefill != null && prefill > 0.0) formatAmountInput(prefill) else ""
        }
    }

    var resolvedLenderName by remember(debtId, lenderName) { mutableStateOf(cachedLenderName) }
    var resolvedDebtTitle by remember(debtId, debtTitle) { mutableStateOf(cachedDebt?.title ?: debtTitle) }
    var resolvedBalance by remember(debtId, currentBalance) { mutableStateOf(cachedDebt?.remainingBalance ?: currentBalance) }
    val cachedSchedule = remember(debtId, cachedDebt) {
        cachedDebt?.let { DebtScheduleHelper.computeSchedule(it, DebtRepository.getCachedNotes(debtId)) }
    }
    var resolvedDueDate by remember(debtId) {
        mutableStateOf(
            cachedSchedule?.nextDueDate?.let { DebtScheduleHelper.formatDisplayDate(it) }
                ?: cachedDebt?.dueDate?.let { DebtScheduleHelper.formatDisplayDate(it) }
        )
    }
    var resolvedInstallmentLabel by remember(debtId) {
        mutableStateOf(
            if (cachedSchedule?.durationMonths != null) {
                "Installment ${cachedSchedule.paidInstallments + 1} of ${cachedSchedule.durationMonths}"
            } else {
                null
            }
        )
    }
    var amountText by remember(debtId) { mutableStateOf(cachedPrefillAmount) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.GCASH) }
    var referenceNo by remember { mutableStateOf("") }
    val paymentDate = remember {
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isInitialLoading by remember(debtId) {
        mutableStateOf(debtId.isNotBlank() && cachedDebt == null)
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val debtRepository = remember { DebtRepository() }
    val profileRepository = remember { ProfileRepository() }
    val paymentRepository = remember { PaymentRepository() }

    LaunchedEffect(debtId) {
        if (debtId.isBlank()) {
            isInitialLoading = false
            return@LaunchedEffect
        }
        try {
            val (debtResult, notes) = coroutineScope {
                val debtDeferred = async { debtRepository.getDebtById(debtId) }
                val notesDeferred = async { debtRepository.getDebtNotes(debtId).getOrNull() }
                debtDeferred.await() to notesDeferred.await()
            }

            debtResult.onSuccess { debt ->
                resolvedDebtTitle = debt.title
                resolvedBalance = debt.remainingBalance

                val schedule = DebtScheduleHelper.computeSchedule(debt, notes)
                resolvedDueDate = schedule.nextDueDate?.let { DebtScheduleHelper.formatDisplayDate(it) }
                    ?: debt.dueDate?.let { DebtScheduleHelper.formatDisplayDate(it) }
                resolvedInstallmentLabel = if (schedule.durationMonths != null) {
                    "Installment ${schedule.paidInstallments + 1} of ${schedule.durationMonths}"
                } else {
                    null
                }

                profileRepository.getProfilesByIds(listOf(debt.lenderId)).onSuccess { profiles ->
                    profiles[debt.lenderId]?.fullName?.takeIf { it.isNotBlank() }?.let {
                        resolvedLenderName = it
                    }
                }

                val monthlyAmount = DebtScheduleHelper.parseMonthlyInstallment(notes)
                val prefillAmount = when {
                    monthlyAmount != null && monthlyAmount > 0.0 -> {
                        if (debt.remainingBalance > 0.0) minOf(monthlyAmount, debt.remainingBalance)
                        else monthlyAmount
                    }
                    debt.remainingBalance > 0.0 -> debt.remainingBalance
                    else -> null
                }
                if (amountText.isBlank() && prefillAmount != null && prefillAmount > 0.0) {
                    amountText = formatAmountInput(prefillAmount)
                }
            }
        } finally {
            isInitialLoading = false
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
                verticalAlignment = Alignment.CenterVertically
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

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Submit Payment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark12
                    )
                )
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
                        text = "Submit Payment",
                        isLoading = isLoading,
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 &&
                                (selectedMethod == PaymentMethod.CASH || referenceNo.isNotBlank()),
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val methodStr = when (selectedMethod) {
                                PaymentMethod.CASH -> "cash"
                                PaymentMethod.GCASH -> "gcash"
                                PaymentMethod.MAYA -> "maya"
                                PaymentMethod.BANK_TRANSFER -> "bank_transfer"
                                PaymentMethod.OTHER -> "other"
                            }
                            scope.launch {
                                isLoading = true
                                paymentRepository.submitPayment(
                                    debtId = debtId,
                                    amount = amt,
                                    paymentDate = normalizeIsoDate(paymentDate),
                                    method = methodStr,
                                    referenceNo = if (selectedMethod == PaymentMethod.CASH) null else referenceNo,
                                    note = note.trim(),
                                    isLenderDirectRecord = false
                                ).onSuccess { createdPayment ->
                                    val existing = PaymentRepository.getCachedPayments(debtId).orEmpty()
                                    PaymentRepository.putCachedPayments(debtId, listOf(createdPayment) + existing)
                                    isLoading = false
                                    AppFeedback.showSuccess("Payment submitted! Waiting for confirmation.")
                                    onPaymentSubmitted()
                                }.onFailure { err ->
                                    isLoading = false
                                    AppFeedback.showError(err.localizedMessage ?: "Failed to submit payment")
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isInitialLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                OwlLoadingSpinner()
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Debt Context Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paying to $resolvedLenderName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12,
                                fontSize = 15.sp
                            )
                        )
                        if (resolvedDebtTitle.isNotBlank()) {
                            Text(
                                text = resolvedDebtTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = GrayText,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Balance",
                            style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                        )
                        Text(
                            text = formatPaymentCurrency(resolvedBalance),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Input Card
            Text(
                text = "Amount to Pay",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Dark12
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "₱",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark12,
                                fontSize = 30.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                            placeholder = { Text("0.00", fontSize = 28.sp, color = GrayText) },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Dark12
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable {
                                if (resolvedBalance > 0.0) {
                                    amountText = formatAmountInput(resolvedBalance)
                                }
                            },
                        color = LimeAccent
                    ) {
                        Text(
                            text = "Pay full balance",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = LimeText,
                                fontSize = 11.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Method Selector
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Dark12
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    val isSelected = method == selectedMethod
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { selectedMethod = method },
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) Dark12 else White,
                        border = if (isSelected) null else BorderStroke(1.dp, GrayBorder)
                    ) {
                        Text(
                            text = method.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.5.sp
                            ),
                            color = if (isSelected) White else Dark12,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reference Number (if not Cash)
            AnimatedVisibility(visible = selectedMethod != PaymentMethod.CASH) {
                Column {
                    DebtTextField(
                        label = "Reference Number",
                        value = referenceNo,
                        onValueChange = { referenceNo = it },
                        placeholder = "e.g. 1029384756",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Payment Date & Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payment Date",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Dark12
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = DebtScheduleHelper.formatDisplayDate(paymentDate),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            color = Dark12
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = GraySurface,
                            disabledBorderColor = GrayBorder,
                            disabledTextColor = Dark12,
                            disabledTrailingIconColor = GrayText
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Dark12
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = resolvedDueDate ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            color = Dark12
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = GraySurface,
                            disabledBorderColor = GrayBorder,
                            disabledTextColor = Dark12,
                            disabledTrailingIconColor = GrayText
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (resolvedInstallmentLabel != null || resolvedDueDate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val helperText = buildString {
                    resolvedInstallmentLabel?.let { append("$it • ") }
                    append("Registered on ${DebtScheduleHelper.formatDisplayDate(paymentDate)}")
                    resolvedDueDate?.let { append(" for due date $it") }
                }
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayText,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note (Optional)
            DebtTextField(
                label = "Note (Optional)",
                value = note,
                onValueChange = { note = it },
                placeholder = "Add a message or payment breakdown...",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notice Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GraySurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Lender will confirm this payment before the balance is deducted.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayText,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}

private val monthlyAmountRegex = Regex("""₱([\d,]+(?:\.\d+)?)/mo""")

private fun parseMonthlyInstallment(notes: String?): Double? {
    if (notes.isNullOrBlank()) return null
    val match = monthlyAmountRegex.find(notes) ?: return null
    return match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
}

private fun formatAmountInput(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", amount)
    }
}

private val PaymentCurrencyFormatter = object : ThreadLocal<NumberFormat>() {
    override fun initialValue(): NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

private fun formatPaymentCurrency(amount: Double): String {
    return "₱${PaymentCurrencyFormatter.get()!!.format(amount)}"
}

private fun normalizeIsoDate(rawDate: String): String {
    val trimmed = rawDate.trim()
    if (trimmed.isEmpty()) return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    return runCatching {
        LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.recoverCatching {
        val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        LocalDate.parse(trimmed, displayFormatter)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrElse {
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitPaymentScreenPreview() {
    DebtCollectTheme {
        SubmitPaymentScreen()
    }
}
