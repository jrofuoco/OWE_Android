package xyz.iamdua.owe.data.local

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.iamdua.owe.data.model.DebtSummaryDto
import xyz.iamdua.owe.data.model.NotificationDto
import xyz.iamdua.owe.data.model.PaymentDto
import xyz.iamdua.owe.data.model.ProfileDto
import java.io.File

/**
 * Lightweight local JSON disk cache (backed by internal filesDir) so that
 * profiles, debts, payments, notes, and notifications are available on cold start
 * in 0ms before checking the network (Stale-While-Revalidate).
 */
object LocalDiskCache {
    @Volatile
    private var cacheDir: File? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun init(context: Context) {
        if (cacheDir != null) return
        synchronized(this) {
            if (cacheDir == null) {
                cacheDir = File(context.applicationContext.filesDir, "owe_data_cache").apply {
                    if (!exists()) mkdirs()
                }
            }
        }
    }

    private fun file(name: String): File? {
        val dir = cacheDir ?: return null
        return File(dir, name)
    }

    fun saveProfiles(profiles: Collection<ProfileDto>) {
        val f = file("profiles.json") ?: return
        runCatching { f.writeText(json.encodeToString(profiles.toList())) }
    }

    fun loadProfiles(): List<ProfileDto> {
        val f = file("profiles.json") ?: return emptyList()
        if (!f.exists()) return emptyList()
        return runCatching { json.decodeFromString<List<ProfileDto>>(f.readText()) }.getOrDefault(emptyList())
    }

    fun saveDebts(debts: List<DebtSummaryDto>) {
        val f = file("debts.json") ?: return
        runCatching { f.writeText(json.encodeToString(debts)) }
    }

    fun loadDebts(): List<DebtSummaryDto> {
        val f = file("debts.json") ?: return emptyList()
        if (!f.exists()) return emptyList()
        return runCatching { json.decodeFromString<List<DebtSummaryDto>>(f.readText()) }.getOrDefault(emptyList())
    }

    fun saveNotes(notes: Map<String, String>) {
        val f = file("debt_notes.json") ?: return
        runCatching { f.writeText(json.encodeToString(notes)) }
    }

    fun loadNotes(): Map<String, String> {
        val f = file("debt_notes.json") ?: return emptyMap()
        if (!f.exists()) return emptyMap()
        return runCatching { json.decodeFromString<Map<String, String>>(f.readText()) }.getOrDefault(emptyMap())
    }

    fun savePayments(payments: List<PaymentDto>) {
        val f = file("payments.json") ?: return
        runCatching { f.writeText(json.encodeToString(payments)) }
    }

    fun loadPayments(): List<PaymentDto> {
        val f = file("payments.json") ?: return emptyList()
        if (!f.exists()) return emptyList()
        return runCatching { json.decodeFromString<List<PaymentDto>>(f.readText()) }.getOrDefault(emptyList())
    }

    fun saveNotifications(notifications: List<NotificationDto>) {
        val f = file("notifications.json") ?: return
        runCatching { f.writeText(json.encodeToString(notifications)) }
    }

    fun loadNotifications(): List<NotificationDto> {
        val f = file("notifications.json") ?: return emptyList()
        if (!f.exists()) return emptyList()
        return runCatching { json.decodeFromString<List<NotificationDto>>(f.readText()) }.getOrDefault(emptyList())
    }

    fun clearAll() {
        val dir = cacheDir ?: return
        runCatching {
            dir.listFiles()?.forEach { it.delete() }
        }
    }
}
