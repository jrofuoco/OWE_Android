package xyz.iamdua.owe.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("middle_name") val middleName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val age: Int? = null,
    val province: String? = null,
    val municipality: String? = null,
    val barangay: String? = null,
    @SerialName("street_address") val streetAddress: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class DebtSummaryDto(
    val id: String,
    @SerialName("lender_id") val lenderId: String,
    @SerialName("borrower_id") val borrowerId: String? = null,
    val title: String,
    val principal: Double,
    @SerialName("interest_amount") val interestAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("total_paid") val totalPaid: Double = 0.0,
    @SerialName("remaining_balance") val remainingBalance: Double,
    @SerialName("pending_payments_count") val pendingPaymentsCount: Int = 0,
    @SerialName("date_given") val dateGiven: String,
    @SerialName("due_date") val dueDate: String? = null,
    val status: String,
    @SerialName("invite_code") val inviteCode: String? = null,
    @SerialName("invite_expires_at") val inviteExpiresAt: String? = null,
    @SerialName("is_overdue") val isOverdue: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateDebtRequest(
    @SerialName("lender_id") val lenderId: String,
    val title: String,
    val principal: Double,
    @SerialName("interest_amount") val interestAmount: Double = 0.0,
    @SerialName("date_given") val dateGiven: String,
    @SerialName("due_date") val dueDate: String? = null,
    val notes: String? = null
)

@Serializable
data class PreviewDebtDto(
    @SerialName("debt_id") val debtId: String,
    @SerialName("lender_name") val lenderName: String,
    val title: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("due_date") val dueDate: String? = null,
    val notes: String? = null
)

@Serializable
data class PaymentDto(
    val id: String,
    @SerialName("debt_id") val debtId: String,
    @SerialName("submitted_by") val submittedBy: String,
    val amount: Double,
    @SerialName("payment_date") val paymentDate: String,
    val method: String,
    @SerialName("reference_no") val referenceNo: String? = null,
    val note: String? = null,
    val status: String,
    @SerialName("reject_reason") val rejectReason: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreatePaymentRequest(
    @SerialName("debt_id") val debtId: String,
    @SerialName("submitted_by") val submittedBy: String,
    val amount: Double,
    @SerialName("payment_date") val paymentDate: String,
    val method: String,
    @SerialName("reference_no") val referenceNo: String? = null,
    val note: String? = null,
    val status: String = "pending"
)

@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("debt_id") val debtId: String? = null,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String
)
