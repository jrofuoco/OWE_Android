package xyz.iamdua.owe.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object VerifyOtp : Screen("verify_otp/{email}") {
        fun createRoute(email: String) = "verify_otp/${android.net.Uri.encode(email)}"
    }
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Debts : Screen("debts")
    object Activity : Screen("activity")
    object Profile : Screen("profile")
    object CreateDebt : Screen("create_debt")
    object InviteCode : Screen("invite_code/{code}") {
        fun createRoute(code: String = "DC-7K4P9Q") = "invite_code/$code"
    }
    object JoinDebt : Screen("join_debt")
    object DebtDetails : Screen("debt_details/{debtId}") {
        fun createRoute(debtId: String = "1") = "debt_details/$debtId"
    }
    object SubmitPayment : Screen("submit_payment/{debtId}") {
        fun createRoute(debtId: String = "1") = "submit_payment/$debtId"
    }
    object ProUpgrade : Screen("pro_upgrade")
    object Notifications : Screen("notifications")
}
