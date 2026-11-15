package xyz.iamdua.owe.ui.auth

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal fun friendlyAuthErrorMessage(
    error: Throwable,
    fallback: String = "Something went wrong. Please try again."
): String {
    if (error is UnknownHostException || error is ConnectException || error is SocketTimeoutException) {
        return "Unable to connect. Please check your internet connection."
    }

    val raw = error.message.orEmpty()
    val lower = raw.lowercase()

    return when {
        "invalid_credentials" in lower || "invalid login credentials" in lower ->
            "Incorrect email or password. Please try again."
        "email_not_confirmed" in lower || "email not confirmed" in lower ->
            "Please verify your email address before logging in."
        "user_already_exists" in lower || "already registered" in lower ->
            "An account with this email already exists."
        "over_request_rate_limit" in lower || "rate limit" in lower || "too many requests" in lower ->
            "Too many attempts. Please wait a moment and try again."
        "otp_expired" in lower || "token has expired" in lower || "invalid otp" in lower ->
            "Invalid or expired verification code. Please try again."
        "weak_password" in lower ->
            "Password is too weak. Please choose a stronger password."
        "unable to resolve host" in lower || "failed to connect" in lower || "timeout" in lower ->
            "Unable to connect. Please check your internet connection."
        else -> {
            // Strip verbose supabase-kt HTTP metadata if present
            val cleaned = raw
                .substringBefore("\nURL:")
                .substringBefore(" URL:")
                .trim()
            if (cleaned.isNotBlank() && cleaned.length <= 120) cleaned else fallback
        }
    }
}
