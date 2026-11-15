package xyz.iamdua.owe.data.repository

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import xyz.iamdua.owe.data.remote.SupabaseClientProvider

@Serializable
data class AppConfigDto(
    val key: String,
    val value: JsonElement,
    val description: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val message: String,
    val type: String = "info",
    @SerialName("action_label") val actionLabel: String? = null,
    @SerialName("action_route") val actionRoute: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

data class AppRuntimeConfig(
    val proMonthlyPrice: Int = 0,
    val proLifetimePrice: Int = 0,
    val freeActiveDebtLimit: Int = 5,
    val interstitialCooldownMinutes: Int = 5,
    val adsEnabled: Boolean = false,
    val maintenanceMode: Boolean = false,
    val isLoaded: Boolean = false
)

object AppConfigStore {
    private val client get() = SupabaseClientProvider.client

    private val _config = MutableStateFlow(AppRuntimeConfig())
    val config: StateFlow<AppRuntimeConfig> = _config.asStateFlow()

    private val _announcements = MutableStateFlow<List<AnnouncementDto>>(emptyList())
    val announcements: StateFlow<List<AnnouncementDto>> = _announcements.asStateFlow()

    private var realtimeStarted = false

    fun startRealtime(scope: CoroutineScope) {
        if (realtimeStarted) return
        realtimeStarted = true

        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            refresh()

            runCatching {
                client.realtime.connect()
                val channel = client.channel("app-config-and-announcements")
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "app_config"
                }.onEach {
                    refreshConfig()
                }.launchIn(this)

                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "announcements"
                }.onEach {
                    refreshAnnouncements()
                }.launchIn(this)

                channel.subscribe(blockUntilSubscribed = true)
            }.onFailure { err ->
                Log.w("AppConfigStore", "Realtime subscription failed: ${err.message}")
            }
        }
    }

    suspend fun refresh() {
        refreshConfig()
        refreshAnnouncements()
    }

    private val CONFIG_COLUMNS = Columns.list("key", "value")
    private val ANNOUNCEMENT_COLUMNS = Columns.list("id", "title", "message", "type", "action_label", "action_route", "created_at")

    private suspend fun refreshConfig() {
        runCatching {
            val rows = client.from("app_config")
                .select(columns = CONFIG_COLUMNS)
                .decodeList<AppConfigDto>()
            if (rows.isNotEmpty()) {
                val map = rows.associate { it.key to it.value }
                _config.value = AppRuntimeConfig(
                    proMonthlyPrice = map.intValue("pro_monthly_price", 0),
                    proLifetimePrice = map.intValue("pro_lifetime_price", 0),
                    freeActiveDebtLimit = map.intValue("free_active_debt_limit", 5).coerceAtLeast(1),
                    interstitialCooldownMinutes = map.intValue("interstitial_cooldown_minutes", 5).coerceAtLeast(1),
                    adsEnabled = map.boolValue("ads_enabled", false),
                    maintenanceMode = map.boolValue("maintenance_mode", false),
                    isLoaded = true
                )
            }
        }.onFailure { err ->
            Log.e("AppConfigStore", "refreshConfig failed: ${err.message}", err)
        }
    }

    private suspend fun refreshAnnouncements() {
        runCatching {
            val items = client.from("announcements")
                .select(columns = ANNOUNCEMENT_COLUMNS) {
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }
                .decodeList<AnnouncementDto>()
            _announcements.value = items
        }.onFailure { err ->
            Log.e("AppConfigStore", "refreshAnnouncements failed: ${err.message}", err)
        }
    }

    private fun Map<String, JsonElement>.intValue(key: String, default: Int): Int {
        val primitive = this[key] as? JsonPrimitive ?: return default
        return primitive.intOrNull ?: primitive.content.toIntOrNull() ?: default
    }

    private fun Map<String, JsonElement>.boolValue(key: String, default: Boolean): Boolean {
        val primitive = this[key] as? JsonPrimitive ?: return default
        return primitive.booleanOrNull ?: primitive.content.toBooleanStrictOrNull() ?: default
    }
}
