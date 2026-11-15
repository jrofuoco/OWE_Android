package xyz.iamdua.owe.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.iamdua.owe.data.repository.AuthRepository
import xyz.iamdua.owe.ui.auth.ForgotPasswordScreen
import xyz.iamdua.owe.ui.auth.LoginScreen
import xyz.iamdua.owe.ui.auth.RegisterScreen
import xyz.iamdua.owe.ui.auth.VerifyOtpScreen
import xyz.iamdua.owe.ui.components.InterstitialAdManager
import xyz.iamdua.owe.ui.debt.CreateDebtScreen
import xyz.iamdua.owe.ui.debt.DebtDetailsScreen
import xyz.iamdua.owe.ui.debt.InviteCodeScreen
import xyz.iamdua.owe.ui.debt.JoinDebtScreen
import xyz.iamdua.owe.ui.home.HomeScreen
import xyz.iamdua.owe.ui.notifications.NotificationsScreen
import xyz.iamdua.owe.ui.onboarding.OnboardingPrefs
import xyz.iamdua.owe.ui.onboarding.OnboardingScreen
import xyz.iamdua.owe.ui.payment.SubmitPaymentScreen
import xyz.iamdua.owe.ui.pro.ProUpgradeScreen
import xyz.iamdua.owe.ui.splash.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val authRepository = remember { AuthRepository() }
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable(
            route = Screen.Splash.route,
            exitTransition = {
                fadeOut(animationSpec = tween(260))
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    val destination = when {
                        authRepository.hasActiveSession() -> Screen.Home.route
                        !OnboardingPrefs.isCompleted(context) -> Screen.Onboarding.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Onboarding.route
        ) {
            OnboardingScreen(
                onFinished = {
                    OnboardingPrefs.setCompleted(context)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Login.route
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Register.route
        ) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.VerifyOtp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = android.net.Uri.decode(
                backStackEntry.arguments?.getString("email").orEmpty()
            )
            VerifyOtpScreen(
                email = email,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVerified = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route
        ) {
            HomeScreen(
                onNavigateToCreateDebt = {
                    navController.navigate(Screen.CreateDebt.route)
                },
                onNavigateToJoinDebt = {
                    navController.navigate(Screen.JoinDebt.route)
                },
                onNavigateToDebtDetails = { debtId ->
                    navController.navigate(Screen.DebtDetails.createRoute(debtId))
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToProUpgrade = {
                    navController.navigate(Screen.ProUpgrade.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateDebt.route) {
            CreateDebtScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDebtCreated = { code ->
                    InterstitialAdManager.showIfReady(context) {
                        navController.navigate(Screen.InviteCode.createRoute(code)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onNavigateToProUpgrade = {
                    navController.navigate(Screen.ProUpgrade.route)
                }
            )
        }

        composable(Screen.ProUpgrade.route) {
            ProUpgradeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProActivated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDebt = { debtId ->
                    navController.navigate(Screen.DebtDetails.createRoute(debtId))
                }
            )
        }

        composable(
            route = Screen.InviteCode.route,
            arguments = listOf(
                navArgument("code") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code").orEmpty()
            InviteCodeScreen(
                initialCode = code,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDoneClick = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(Screen.JoinDebt.route) {
            JoinDebtScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDebtAccepted = { debtId ->
                    navController.navigate(Screen.DebtDetails.createRoute(debtId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.DebtDetails.route,
            arguments = listOf(
                navArgument("debtId") {
                    type = NavType.StringType
                    defaultValue = "1"
                }
            )
        ) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getString("debtId") ?: "1"
            DebtDetailsScreen(
                debtId = debtId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSubmitPayment = {
                    navController.navigate(Screen.SubmitPayment.createRoute(debtId))
                },
                onShareInviteCode = { code ->
                    navController.navigate(Screen.InviteCode.createRoute(code))
                }
            )
        }

        composable(
            route = Screen.SubmitPayment.route,
            arguments = listOf(
                navArgument("debtId") {
                    type = NavType.StringType
                    defaultValue = "1"
                }
            )
        ) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getString("debtId").orEmpty()
            SubmitPaymentScreen(
                debtId = debtId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentSubmitted = {
                    InterstitialAdManager.showIfReady(context) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
