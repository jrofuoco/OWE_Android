package xyz.iamdua.owe.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ProPlan(val id: String, val title: String, val priceLabel: String, val periodLabel: String, val subtitle: String) {
    MONTHLY(
        id = "monthly",
        title = "Monthly",
        priceLabel = "—",
        periodLabel = "/ month",
        subtitle = "Flexible billing, cancel anytime"
    ),
    LIFETIME(
        id = "lifetime",
        title = "One-Time Pro",
        priceLabel = "—",
        periodLabel = "once",
        subtitle = "Pay once, unlock Pro forever"
    )
}

object ProPreferences {
    const val FREE_ACTIVE_DEBT_LIMIT = 5

    private const val PREFS_NAME = "debtcollect_pro_prefs"
    private const val KEY_IS_PRO = "is_pro"
    private const val KEY_PLAN_ID = "plan_id"

    private val _isProFlow = MutableStateFlow(false)
    val isProFlow: StateFlow<Boolean> = _isProFlow.asStateFlow()

    fun isPro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getBoolean(KEY_IS_PRO, false)
        _isProFlow.value = value
        return value
    }

    fun getPlan(context: Context): ProPlan? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_PLAN_ID, null) ?: return null
        return ProPlan.entries.firstOrNull { it.id == id }
    }

    fun activatePro(context: Context, plan: ProPlan) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_PRO, true)
            .putString(KEY_PLAN_ID, plan.id)
            .apply()
        _isProFlow.value = true
    }

    fun clearPro(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_PRO, false)
            .remove(KEY_PLAN_ID)
            .apply()
        _isProFlow.value = false
    }
}
