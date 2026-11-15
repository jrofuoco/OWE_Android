package xyz.iamdua.owe.data.repository

import android.util.Log
import io.github.jan.supabase.auth.auth
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
import xyz.iamdua.owe.data.local.LocalDiskCache
import xyz.iamdua.owe.data.model.NotificationDto
import xyz.iamdua.owe.data.remote.SupabaseClientProvider

object NotificationStore {
    private val client get() = SupabaseClientProvider.client
    private val auth get() = client.auth

    private val NOTIFICATION_COLUMNS = Columns.list(
        "id",
        "user_id",
        "debt_id",
        "type",
        "title",
        "body",
        "is_read",
        "created_at"
    )

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private const val PAGE_SIZE = 25
    private var realtimeStarted = false

    fun startRealtime(scope: CoroutineScope) {
        if (realtimeStarted) return
        realtimeStarted = true

        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val cached = LocalDiskCache.loadNotifications()
            if (cached.isNotEmpty() && _notifications.value.isEmpty()) {
                _notifications.value = cached
                _unreadCount.value = cached.count { !it.isRead }
            }
            refresh()

            runCatching {
                client.realtime.connect()
                val channel = client.channel("user-notifications")
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "notifications"
                }.onEach {
                    refresh()
                }.launchIn(this)
                channel.subscribe(blockUntilSubscribed = true)
            }.onFailure { err ->
                Log.w("NotificationStore", "Realtime subscription failed: ${err.message}")
            }
        }
    }

    suspend fun refresh() {
        val uid = auth.currentUserOrNull()?.id ?: run {
            _notifications.value = emptyList()
            _unreadCount.value = 0
            return
        }

        runCatching {
            val list = client.from("notifications")
                .select(columns = NOTIFICATION_COLUMNS) {
                    filter { eq("user_id", uid) }
                    order("created_at", Order.DESCENDING)
                    range(0L, (PAGE_SIZE - 1).toLong())
                }
                .decodeList<NotificationDto>()
            _notifications.value = list
            _unreadCount.value = list.count { !it.isRead }
            _hasMore.value = list.size >= PAGE_SIZE
            LocalDiskCache.saveNotifications(list)
        }.onFailure { err ->
            Log.e("NotificationStore", "refresh failed: ${err.message}", err)
        }
    }

    suspend fun loadMore() {
        val uid = auth.currentUserOrNull()?.id ?: return
        if (_isLoadingMore.value || !_hasMore.value) return
        _isLoadingMore.value = true
        try {
            val offset = _notifications.value.size.toLong()
            val nextBatch = client.from("notifications")
                .select(columns = NOTIFICATION_COLUMNS) {
                    filter { eq("user_id", uid) }
                    order("created_at", Order.DESCENDING)
                    range(offset, offset + PAGE_SIZE - 1)
                }
                .decodeList<NotificationDto>()
            val existingIds = _notifications.value.mapTo(HashSet()) { it.id }
            val merged = _notifications.value + nextBatch.filter { it.id !in existingIds }
            _notifications.value = merged
            _unreadCount.value = merged.count { !it.isRead }
            _hasMore.value = nextBatch.size >= PAGE_SIZE
        } catch (err: Exception) {
            Log.e("NotificationStore", "loadMore failed: ${err.message}", err)
        } finally {
            _isLoadingMore.value = false
        }
    }

    suspend fun markAsRead(notificationId: String) {
        val current = _notifications.value
        val updated = current.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        _notifications.value = updated
        _unreadCount.value = updated.count { !it.isRead }
        LocalDiskCache.saveNotifications(updated)

        runCatching {
            client.from("notifications")
                .update({ set("is_read", true) }) {
                    filter { eq("id", notificationId) }
                }
        }.onFailure { err ->
            Log.w("NotificationStore", "markAsRead failed: ${err.message}")
        }
    }

    suspend fun markAllAsRead() {
        val uid = auth.currentUserOrNull()?.id ?: return
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        _unreadCount.value = 0
        LocalDiskCache.saveNotifications(updated)

        runCatching {
            client.from("notifications")
                .update({ set("is_read", true) }) {
                    filter {
                        eq("user_id", uid)
                        eq("is_read", false)
                    }
                }
        }.onFailure { err ->
            Log.w("NotificationStore", "markAllAsRead failed: ${err.message}")
        }
    }
}
