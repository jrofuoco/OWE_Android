package xyz.iamdua.owe.data.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class DebtScheduleInfo(
    val durationMonths: Int? = null,
    val monthlyAmount: Double? = null,
    val payDay: Int? = null,
    val paidInstallments: Int = 0,
    val nextDueDate: LocalDate? = null,
    val nextDueAmount: Double = 0.0
)

object DebtScheduleHelper {
    private val termRegex = Regex("""Term:\s*(\d+)\s*mo""")
    private val monthlyRegex = Regex("""₱([\d,]+(?:\.\d+)?)/mo""")
    private val payDayRegex = Regex("""due every\s*(\d+)(?:st|nd|rd|th)""")

    fun parseMonthlyInstallment(notes: String?): Double? {
        if (notes.isNullOrBlank()) return null
        val match = monthlyRegex.find(notes) ?: return null
        return match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
    }

    fun computeSchedule(
        debt: DebtSummaryDto,
        notes: String?
    ): DebtScheduleInfo {
        val parsedDueDate = debt.dueDate?.let { parseIsoDate(it) }
        val remaining = debt.remainingBalance.coerceAtLeast(0.0)

        if (debt.status == "paid" || remaining <= 0.0) {
            return DebtScheduleInfo(
                durationMonths = notes?.let { termRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() },
                monthlyAmount = parseMonthlyInstallment(notes),
                payDay = notes?.let { payDayRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 31) },
                paidInstallments = notes?.let { termRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() } ?: 0,
                nextDueDate = null,
                nextDueAmount = 0.0
            )
        }

        val durationMonths = notes?.let { termRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }
        val monthlyAmount = parseMonthlyInstallment(notes)
        val payDay = notes?.let { payDayRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 31) }

        if (durationMonths != null && durationMonths > 0 && monthlyAmount != null && monthlyAmount > 0.0 && payDay != null) {
            val givenDate = parseIsoDate(debt.dateGiven) ?: LocalDate.now()
            val paidCount = ((debt.totalPaid + 0.01) / monthlyAmount).toInt().coerceIn(0, durationMonths)

            val firstInstallmentMonth = YearMonth.from(givenDate).plusMonths(1)
            val targetMonth = firstInstallmentMonth.plusMonths(paidCount.toLong())
            val clampedDay = minOf(payDay, targetMonth.lengthOfMonth())
            val computedDue = targetMonth.atDay(clampedDay)
            val effectiveNextDue = if (parsedDueDate != null && computedDue.isAfter(parsedDueDate)) {
                parsedDueDate
            } else {
                computedDue
            }

            return DebtScheduleInfo(
                durationMonths = durationMonths,
                monthlyAmount = monthlyAmount,
                payDay = payDay,
                paidInstallments = paidCount,
                nextDueDate = effectiveNextDue,
                nextDueAmount = minOf(monthlyAmount, remaining)
            )
        }

        return DebtScheduleInfo(
            nextDueDate = parsedDueDate,
            nextDueAmount = remaining
        )
    }

    private val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.US)

    fun formatIsoDate(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun formatDisplayDate(date: LocalDate): String = date.format(displayFormatter)

    fun formatDisplayDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        val datePart = raw.trim().take(10)
        return parseIsoDate(datePart)?.let { it.format(displayFormatter) } ?: raw
    }

    private fun parseIsoDate(raw: String): LocalDate? = runCatching {
        LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull()
}
