package xyz.iamdua.owe.data.repository

import xyz.iamdua.owe.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val auth get() = SupabaseClientProvider.client.auth

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(
        firstName: String,
        middleName: String?,
        lastName: String,
        age: Int,
        province: String,
        municipality: String,
        barangay: String,
        streetAddress: String?,
        email: String,
        phone: String?,
        password: String
    ): Result<Unit> = runCatching {
        val fullName = listOfNotNull(
            firstName.trim(),
            middleName?.trim()?.takeIf { it.isNotEmpty() },
            lastName.trim()
        ).joinToString(" ")

        auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject {
                put("full_name", fullName)
                put("first_name", firstName.trim())
                if (!middleName.isNullOrBlank()) put("middle_name", middleName.trim())
                put("last_name", lastName.trim())
                put("age", age)
                put("province", province.trim())
                put("municipality", municipality.trim())
                put("barangay", barangay.trim())
                if (!streetAddress.isNullOrBlank()) put("street_address", streetAddress.trim())
                if (!phone.isNullOrBlank()) put("phone", phone.trim())
            }
        }
    }

    suspend fun verifySignupOtp(email: String, token: String): Result<Unit> = runCatching {
        auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = email.trim(),
            token = token.trim()
        )
    }

    suspend fun resendSignupOtp(email: String): Result<Unit> = runCatching {
        auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email.trim()
        )
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email.trim())
    }

    suspend fun verifyRecoveryOtp(email: String, token: String): Result<Unit> = runCatching {
        auth.verifyEmailOtp(
            type = OtpType.Email.RECOVERY,
            email = email.trim(),
            token = token.trim()
        )
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.updateUser {
            password = newPassword
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        val currentEmail = auth.currentUserOrNull()?.email?.takeIf { it.isNotBlank() }
            ?: error("Please sign in again.")
        runCatching {
            auth.signInWith(Email) {
                this.email = currentEmail
                this.password = oldPassword
            }
        }.onFailure {
            error("Current password is incorrect.")
        }
        auth.updateUser {
            password = newPassword
        }
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        ProfileRepository.clearCache()
        DebtRepository.clearCache()
        PaymentRepository.clearCache()
        xyz.iamdua.owe.data.local.LocalDiskCache.clearAll()
        auth.signOut()
    }

    fun hasActiveSession(): Boolean = auth.currentSessionOrNull() != null
}
