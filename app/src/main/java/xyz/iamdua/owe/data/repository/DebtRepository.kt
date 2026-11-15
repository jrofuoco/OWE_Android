package xyz.iamdua.owe.data.repository

import xyz.iamdua.owe.data.local.LocalDiskCache
import xyz.iamdua.owe.data.model.CreateDebtRequest
import xyz.iamdua.owe.data.model.DebtSummaryDto
import xyz.iamdua.owe.data.model.PreviewDebtDto
import xyz.iamdua.owe.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class CreatedDebtIdDto(val id: String)

@Serializable
private data class DebtNotesDto(val notes: String? = null)

@Serializable
private data class DebtIdNotesDto(val id: String, val notes: String? = null)

class DebtRepository {
    private val client get() = SupabaseClientProvider.client
    private val auth get() = client.auth

    companion object {
        private val debtCache = ConcurrentHashMap<String, DebtSummaryDto>()
        private val notesCache = ConcurrentHashMap<String, String>()
        @Volatile
        private var hydratedFromDisk = false

        private val SUMMARY_COLUMNS = Columns.list(
            "id",
            "lender_id",
            "borrower_id",
            "title",
            "principal",
            "interest_amount",
            "total_amount",
            "total_paid",
            "remaining_balance",
            "pending_payments_count",
            "date_given",
            "due_date",
            "status",
            "invite_code",
            "invite_expires_at",
            "is_overdue",
            "created_at"
        )

        fun ensureHydrated() {
            if (hydratedFromDisk) return
            synchronized(this) {
                if (!hydratedFromDisk) {
                    LocalDiskCache.loadDebts().forEach { debtCache[it.id] = it }
                    notesCache.putAll(LocalDiskCache.loadNotes())
                    hydratedFromDisk = true
                }
            }
        }

        fun getCachedDebts(): List<DebtSummaryDto> {
            ensureHydrated()
            return debtCache.values.sortedByDescending { it.createdAt ?: it.dateGiven }
        }

        fun getCachedDebt(debtId: String): DebtSummaryDto? {
            ensureHydrated()
            return debtCache[debtId]
        }

        fun putCachedDebt(debt: DebtSummaryDto) {
            ensureHydrated()
            debtCache[debt.id] = debt
            LocalDiskCache.saveDebts(getCachedDebts())
        }

        fun getCachedNotes(debtId: String): String? {
            ensureHydrated()
            return notesCache[debtId]
        }

        fun getAllCachedNotes(): Map<String, String> {
            ensureHydrated()
            return notesCache.toMap()
        }

        fun clearCache() {
            debtCache.clear()
            notesCache.clear()
        }
    }

    fun currentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun getMyDebts(
        offset: Int = 0,
        limit: Int = 25
    ): Result<List<DebtSummaryDto>> = runCatching {
        ensureHydrated()
        val list = client.from("debt_summary")
            .select(columns = SUMMARY_COLUMNS) {
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<DebtSummaryDto>()
        if (offset == 0) {
            debtCache.clear()
        }
        list.forEach { debtCache[it.id] = it }
        LocalDiskCache.saveDebts(getCachedDebts())
        list
    }

    suspend fun getDebtById(debtId: String): Result<DebtSummaryDto> = runCatching {
        ensureHydrated()
        val debt = client.from("debt_summary")
            .select(columns = SUMMARY_COLUMNS) {
                filter { eq("id", debtId) }
            }
            .decodeSingle<DebtSummaryDto>()
        debtCache[debt.id] = debt
        LocalDiskCache.saveDebts(getCachedDebts())
        debt
    }

    suspend fun createDebt(
        title: String,
        principal: Double,
        interestAmount: Double,
        dateGiven: String,
        dueDate: String?,
        notes: String?
    ): Result<DebtSummaryDto> = runCatching {
        val uid = currentUserId() ?: error("Not authenticated")
        val created = client.from("debts")
            .insert(
                CreateDebtRequest(
                    lenderId = uid,
                    title = title.trim(),
                    principal = principal,
                    interestAmount = interestAmount,
                    dateGiven = dateGiven,
                    dueDate = dueDate,
                    notes = notes?.trim()?.takeIf { it.isNotEmpty() }
                )
            ) {
                select(Columns.list("id"))
            }
            .decodeSingle<CreatedDebtIdDto>()

        // Auto-generate invite code immediately
        generateInviteCode(created.id).getOrThrow()
        getDebtById(created.id).getOrThrow()
    }

    suspend fun getDebtNotes(debtId: String): Result<String?> = runCatching {
        ensureHydrated()
        val notes = client.from("debts")
            .select(Columns.list("notes")) {
                filter { eq("id", debtId) }
            }
            .decodeSingleOrNull<DebtNotesDto>()
            ?.notes
        notesCache[debtId] = notes.orEmpty()
        LocalDiskCache.saveNotes(notesCache)
        notes
    }

    suspend fun getNotesForDebts(
        debtIds: List<String>,
        forceRefresh: Boolean = false
    ): Result<Map<String, String>> = runCatching {
        ensureHydrated()
        val distinctIds = debtIds.distinct()
        if (distinctIds.isEmpty()) return@runCatching emptyMap()

        val missingIds = if (forceRefresh) {
            distinctIds
        } else {
            distinctIds.filter { !notesCache.containsKey(it) }
        }

        if (missingIds.isNotEmpty()) {
            val fetched = client.from("debts")
                .select(Columns.list("id", "notes")) {
                    filter { isIn("id", missingIds) }
                }
                .decodeList<DebtIdNotesDto>()
            val fetchedMap = fetched.associate { it.id to it.notes.orEmpty() }
            missingIds.forEach { id -> notesCache[id] = fetchedMap[id].orEmpty() }
            LocalDiskCache.saveNotes(notesCache)
        }

        distinctIds.associateWith { notesCache[it].orEmpty() }
    }

    suspend fun generateInviteCode(debtId: String): Result<String> = runCatching {
        client.postgrest.rpc(
            "generate_invite_code",
            buildJsonObject { put("p_debt_id", debtId) }
        ).decodeAs<String>()
    }

    suspend fun previewDebtByCode(code: String): Result<PreviewDebtDto?> = runCatching {
        client.postgrest.rpc(
            "preview_debt_by_code",
            buildJsonObject { put("p_code", code.trim().uppercase()) }
        ).decodeList<PreviewDebtDto>().firstOrNull()
    }

    suspend fun joinDebt(code: String): Result<String> = runCatching {
        client.postgrest.rpc(
            "join_debt",
            buildJsonObject { put("p_code", code.trim().uppercase()) }
        ).decodeAs<String>()
    }
}
