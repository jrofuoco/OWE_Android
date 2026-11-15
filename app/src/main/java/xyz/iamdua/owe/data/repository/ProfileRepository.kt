package xyz.iamdua.owe.data.repository

import xyz.iamdua.owe.data.local.LocalDiskCache
import xyz.iamdua.owe.data.model.ProfileDto
import xyz.iamdua.owe.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepository {
    private val client get() = SupabaseClientProvider.client
    private val auth get() = client.auth

    companion object {
        private val profileCache = ConcurrentHashMap<String, ProfileDto>()
        @Volatile
        private var hydratedFromDisk = false

        private val SUMMARY_COLUMNS = Columns.list(
            "id",
            "full_name",
            "first_name",
            "middle_name",
            "last_name",
            "phone",
            "avatar_url"
        )

        fun ensureHydrated() {
            if (hydratedFromDisk) return
            synchronized(this) {
                if (!hydratedFromDisk) {
                    LocalDiskCache.loadProfiles().forEach { profileCache[it.id] = it }
                    hydratedFromDisk = true
                }
            }
        }

        fun getCachedProfile(id: String): ProfileDto? {
            ensureHydrated()
            return profileCache[id]
        }

        fun clearCache() {
            profileCache.clear()
        }
    }

    suspend fun getMyProfile(forceRefresh: Boolean = false): Result<ProfileDto> = runCatching {
        ensureHydrated()
        val uid = auth.currentUserOrNull()?.id ?: error("Not authenticated")
        if (!forceRefresh) {
            profileCache[uid]?.let { return@runCatching it }
        }
        val profile = client.from("profiles")
            .select(columns = SUMMARY_COLUMNS) {
                filter { eq("id", uid) }
            }
            .decodeSingle<ProfileDto>()
        profileCache[uid] = profile
        LocalDiskCache.saveProfiles(profileCache.values)
        profile
    }

    suspend fun getProfilesByIds(
        ids: List<String>,
        forceRefresh: Boolean = false
    ): Result<Map<String, ProfileDto>> = runCatching {
        ensureHydrated()
        val distinctIds = ids.distinct()
        if (distinctIds.isEmpty()) return@runCatching emptyMap()

        val missingIds = if (forceRefresh) {
            distinctIds
        } else {
            distinctIds.filter { !profileCache.containsKey(it) }
        }

        if (missingIds.isNotEmpty()) {
            val fetched = client.from("profiles")
                .select(columns = SUMMARY_COLUMNS) {
                    filter { isIn("id", missingIds) }
                }
                .decodeList<ProfileDto>()
            fetched.forEach { profileCache[it.id] = it }
            LocalDiskCache.saveProfiles(profileCache.values)
        }

        distinctIds.mapNotNull { id -> profileCache[id]?.let { id to it } }.toMap()
    }

    suspend fun updateProfile(fullName: String, phone: String?): Result<Unit> = runCatching {
        val uid = auth.currentUserOrNull()?.id ?: error("Not authenticated")
        val trimmedName = fullName.trim()
        val trimmedPhone = phone?.trim()?.takeIf { it.isNotEmpty() }
        client.from("profiles")
            .update(
                buildJsonObject {
                    put("full_name", trimmedName)
                    if (trimmedPhone == null) {
                        put("phone", null as String?)
                    } else {
                        put("phone", trimmedPhone)
                    }
                }
            ) {
                filter { eq("id", uid) }
            }
        profileCache[uid]?.let { existing ->
            profileCache[uid] = existing.copy(fullName = trimmedName, phone = trimmedPhone)
        }
    }
}
