package xyz.iamdua.owe.data.repository

import xyz.iamdua.owe.data.local.LocalDiskCache
import xyz.iamdua.owe.data.model.CreatePaymentRequest
import xyz.iamdua.owe.data.model.PaymentDto
import xyz.iamdua.owe.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PaymentRepository {
    private val client get() = SupabaseClientProvider.client
    private val auth get() = client.auth

    companion object {
        private val paymentsCache = ConcurrentHashMap<String, List<PaymentDto>>()
        @Volatile
        private var hydratedFromDisk = false

        private val PAYMENT_COLUMNS = Columns.list(
            "id",
            "debt_id",
            "submitted_by",
            "amount",
            "payment_date",
            "method",
            "reference_no",
            "note",
            "status",
            "reject_reason",
            "reviewed_at",
            "created_at"
        )

        fun ensureHydrated() {
            if (hydratedFromDisk) return
            synchronized(this) {
                if (!hydratedFromDisk) {
                    val grouped = LocalDiskCache.loadPayments().groupBy { it.debtId }
                    paymentsCache.putAll(grouped)
                    hydratedFromDisk = true
                }
            }
        }

        fun getCachedPayments(debtId: String): List<PaymentDto>? {
            ensureHydrated()
            return paymentsCache[debtId]
        }

        fun getAllCachedPayments(): List<PaymentDto> {
            ensureHydrated()
            return paymentsCache.values.flatten().sortedByDescending { it.createdAt ?: it.paymentDate }
        }

        fun putCachedPayments(debtId: String, list: List<PaymentDto>) {
            ensureHydrated()
            paymentsCache[debtId] = list
            LocalDiskCache.savePayments(paymentsCache.values.flatten())
        }

        fun clearCache() {
            paymentsCache.clear()
        }
    }

    suspend fun getPaymentsForDebt(
        debtId: String,
        offset: Int = 0,
        limit: Int = 20
    ): Result<List<PaymentDto>> = runCatching {
        ensureHydrated()
        val list = client.from("payments")
            .select(columns = PAYMENT_COLUMNS) {
                filter { eq("debt_id", debtId) }
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<PaymentDto>()
        if (offset == 0) {
            paymentsCache[debtId] = list
        } else {
            val existing = paymentsCache[debtId].orEmpty()
            val existingIds = existing.mapTo(HashSet()) { it.id }
            paymentsCache[debtId] = existing + list.filter { it.id !in existingIds }
        }
        LocalDiskCache.savePayments(paymentsCache.values.flatten())
        list
    }

    suspend fun getPaymentsForDebts(debtIds: List<String>): Result<List<PaymentDto>> = runCatching {
        ensureHydrated()
        val distinctIds = debtIds.distinct()
        if (distinctIds.isEmpty()) return@runCatching emptyList()
        val list = client.from("payments")
            .select(columns = PAYMENT_COLUMNS) {
                filter { isIn("debt_id", distinctIds) }
                order("created_at", Order.DESCENDING)
                limit(200)
            }
            .decodeList<PaymentDto>()
        val grouped = list.groupBy { it.debtId }
        distinctIds.forEach { id -> paymentsCache[id] = grouped[id].orEmpty() }
        LocalDiskCache.savePayments(paymentsCache.values.flatten())
        list
    }

    suspend fun submitPayment(
        debtId: String,
        amount: Double,
        paymentDate: String,
        method: String,
        referenceNo: String?,
        note: String?,
        isLenderDirectRecord: Boolean
    ): Result<PaymentDto> = runCatching {
        val uid = auth.currentUserOrNull()?.id ?: error("Not authenticated")
        client.from("payments")
            .insert(
                CreatePaymentRequest(
                    debtId = debtId,
                    submittedBy = uid,
                    amount = amount,
                    paymentDate = paymentDate,
                    method = method,
                    referenceNo = referenceNo?.trim()?.takeIf { it.isNotEmpty() },
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    status = if (isLenderDirectRecord) "confirmed" else "pending"
                )
            ) {
                select()
            }
            .decodeSingle<PaymentDto>()
    }

    suspend fun reviewPayment(
        paymentId: String,
        approve: Boolean,
        rejectReason: String? = null
    ): Result<String> = runCatching {
        client.postgrest.rpc(
            "review_payment",
            buildJsonObject {
                put("p_payment_id", paymentId)
                put("p_approve", approve)
                if (rejectReason != null) {
                    put("p_reject_reason", rejectReason)
                }
            }
        ).decodeAs<String>()
    }
}
