package xyz.iamdua.owe.ui.debt

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import xyz.iamdua.owe.data.local.ProPreferences
import xyz.iamdua.owe.data.repository.AppConfigStore
import xyz.iamdua.owe.data.repository.DebtRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
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
import xyz.iamdua.owe.ui.components.OweConfirmDialog
import xyz.iamdua.owe.ui.components.PrimaryPillButton
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
import xyz.iamdua.owe.ui.theme.White
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private enum class InterestRateType(val label: String) {
    FLAT("Flat Rate (₱)"),
    PERCENTAGE("Percentage (%)")
}

@Composable
fun CreateDebtScreen(
    onNavigateBack: () -> Unit = {},
    onDebtCreated: (String) -> Unit = {},
    onNavigateToProUpgrade: () -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    var title by remember { mutableStateOf("") }
    var principalText by remember { mutableStateOf("") }
    var interestType by remember { mutableStateOf(InterestRateType.PERCENTAGE) }
    var interestText by remember { mutableStateOf("") }
    var durationMonthsText by remember { mutableStateOf("6") }
    var paymentDayText by remember { mutableStateOf(today.dayOfMonth.toString()) }
    var dateGiven by remember { mutableStateOf(today.toString()) }
    var dueDate by remember { mutableStateOf(computeFinalDueDate(today.toString(), 6, today.dayOfMonth)) }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProLimitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val runtimeConfig by AppConfigStore.config.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val debtRepository = remember { DebtRepository() }

    val principalAmount by remember {
        derivedStateOf { principalText.toDoubleOrNull() ?: 0.0 }
    }
    val interestInput by remember {
        derivedStateOf { interestText.toDoubleOrNull() ?: 0.0 }
    }
    val interestAmount by remember {
        derivedStateOf {
            when (interestType) {
                InterestRateType.FLAT -> interestInput
                InterestRateType.PERCENTAGE -> principalAmount * (interestInput / 100.0)
            }
        }
    }
    val totalToCollect by remember {
        derivedStateOf { principalAmount + interestAmount }
    }
    val durationMonths by remember {
        derivedStateOf { (durationMonthsText.toIntOrNull() ?: 1).coerceIn(1, 120) }
    }
    val paymentDay by remember {
        derivedStateOf { (paymentDayText.toIntOrNull() ?: 1).coerceIn(1, 31) }
    }
    val monthlyPayment by remember {
        derivedStateOf {
            if (durationMonths > 0) totalToCollect / durationMonths else totalToCollect
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
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Dark12
                    )
                }

                Text(
                    text = "New Debt",
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
                        text = "Create & Generate Code",
                        isLoading = isLoading,
                        enabled = title.isNotBlank() && principalAmount > 0 && !isLoading,
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null

                                if (!ProPreferences.isPro(context)) {
                                    val uid = debtRepository.currentUserId()
                                    val activeCount = debtRepository.getMyDebts().getOrNull()
                                        ?.count { it.lenderId == uid && it.status != "paid" && it.status != "cancelled" }
                                        ?: 0
                                    if (activeCount >= runtimeConfig.freeActiveDebtLimit) {
                                        isLoading = false
                                        showProLimitDialog = true
                                        return@launch
                                    }
                                }

                                val scheduleNote = "Term: $durationMonths mo(s) • ${formatCurrency(monthlyPayment)}/mo due every ${ordinalDay(paymentDay)}"
                                val combinedNotes = if (notes.isBlank()) scheduleNote else "$scheduleNote\n${notes.trim()}"
                                debtRepository.createDebt(
                                    title = title,
                                    principal = principalAmount,
                                    interestAmount = interestAmount,
                                    dateGiven = dateGiven,
                                    dueDate = dueDate.takeIf { it.isNotBlank() },
                                    notes = combinedNotes
                                ).onSuccess { created ->
                                    isLoading = false
                                    AppFeedback.showSuccess("Debt created! Share your invite code.")
                                    onDebtCreated(created.inviteCode ?: "DC-000000")
                                }.onFailure { err ->
                                    isLoading = false
                                    val msg = err.localizedMessage ?: "Failed to create debt"
                                    errorMessage = msg
                                    AppFeedback.showError(msg)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Debt Title
            DebtTextField(
                label = "Debt Title",
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g. Motorcycle loan",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Principal Amount
            DebtTextField(
                label = "Principal Amount",
                value = principalText,
                onValueChange = { principalText = it.filter { char -> char.isDigit() || char == '.' } },
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interest Type Toggle (Flat Rate vs Percentage)
            Text(
                text = "Interest Type",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Dark12
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GraySurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InterestRateType.entries.forEach { type ->
                    val selected = interestType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (selected) ForestGreen else Color.Transparent)
                            .clickable { interestType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) White else GrayText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interest Value
            DebtTextField(
                label = if (interestType == InterestRateType.FLAT) "Flat Interest Amount (Optional)" else "Interest Percentage % (Optional)",
                value = interestText,
                onValueChange = { interestText = it.filter { char -> char.isDigit() || char == '.' } },
                placeholder = if (interestType == InterestRateType.FLAT) "0.00" else "e.g. 10",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Duration (Months) & Payment Day of Month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebtTextField(
                    label = "Duration (Months)",
                    value = durationMonthsText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(3)
                        durationMonthsText = digits
                        val m = (digits.toIntOrNull() ?: 1).coerceIn(1, 120)
                        dueDate = computeFinalDueDate(dateGiven, m, paymentDay)
                    },
                    placeholder = "6",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.weight(1f)
                )

                DebtTextField(
                    label = "Pay Day (1–31)",
                    value = paymentDayText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(2)
                        val clamped = digits.toIntOrNull()?.coerceIn(1, 31)?.toString() ?: digits
                        paymentDayText = clamped
                        val d = (clamped.toIntOrNull() ?: 1).coerceIn(1, 31)
                        dueDate = computeFinalDueDate(dateGiven, durationMonths, d)
                    },
                    placeholder = "15",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Duration Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 6, 12, 24).forEach { months ->
                    val selected = durationMonthsText == months.toString()
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) ForestGreen else White)
                            .border(1.dp, if (selected) ForestGreen else GrayBorder, CircleShape)
                            .clickable {
                                durationMonthsText = months.toString()
                                dueDate = computeFinalDueDate(dateGiven, months, paymentDay)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${months}m",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) White else GrayText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Preview: Total to Collect & Monthly Breakdown Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ForestGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total to collect",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = formatCurrency(totalToCollect),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ForestGreenDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Calculate,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ForestGreenDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Monthly Payment",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatCurrency(monthlyPayment)} / mo",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$durationMonths ${if (durationMonths == 1) "month" else "months"}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Every ${ordinalDay(paymentDay)} of month",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 11.5.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dates Row (Date Given & Due Date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date Given",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Dark12
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = dateGiven,
                        onValueChange = { dateGiven = it },
                        readOnly = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
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
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = Dark12,
                            unfocusedBorderColor = GrayBorder
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
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        readOnly = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
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

            Spacer(modifier = Modifier.height(20.dp))

            // Notes (Optional)
            Text(
                text = "Notes (Optional)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Dark12
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Downpayment for Yamaha NMAX, payable in installments...", color = GrayText) },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = Dark12,
                    unfocusedBorderColor = GrayBorder
                ),
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showProLimitDialog) {
        OweConfirmDialog(
            icon = Icons.Rounded.WorkspacePremium,
            title = "Free Plan Limit Reached",
            message = "Free accounts can track up to ${runtimeConfig.freeActiveDebtLimit} active debts. Upgrade to OWE Pro for unlimited active debts, PDF statement exports, and more.",
            confirmText = "Upgrade to Pro",
            dismissText = "Not now",
            isDestructive = false,
            onConfirm = {
                showProLimitDialog = false
                onNavigateToProUpgrade()
            },
            onDismiss = { showProLimitDialog = false }
        )
    }
}

private fun computeFinalDueDate(startDateStr: String, months: Int, dayOfMonth: Int): String {
    return runCatching {
        val start = LocalDate.parse(startDateStr.trim())
        val target = start.plusMonths(months.toLong())
        val clampedDay = dayOfMonth.coerceIn(1, target.lengthOfMonth())
        target.withDayOfMonth(clampedDay).toString()
    }.getOrElse { startDateStr }
}

private fun ordinalDay(day: Int): String {
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$day$suffix"
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "₱${formatter.format(amount)}"
}

@Preview(showBackground = true)
@Composable
fun CreateDebtScreenPreview() {
    DebtCollectTheme {
        CreateDebtScreen()
    }
}
