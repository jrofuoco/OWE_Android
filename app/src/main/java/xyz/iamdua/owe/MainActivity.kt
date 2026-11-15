package xyz.iamdua.owe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.iamdua.owe.data.local.LocalDiskCache
import xyz.iamdua.owe.data.network.NetworkMonitor
import xyz.iamdua.owe.data.repository.AppConfigStore
import xyz.iamdua.owe.data.repository.AuthRepository
import xyz.iamdua.owe.data.repository.DebtRepository
import xyz.iamdua.owe.data.repository.NotificationStore
import xyz.iamdua.owe.data.repository.PaymentRepository
import xyz.iamdua.owe.data.repository.ProfileRepository
import xyz.iamdua.owe.data.repository.PsgcRepository
import xyz.iamdua.owe.ui.components.AppFeedback
import xyz.iamdua.owe.ui.components.AppFeedbackHost
import xyz.iamdua.owe.ui.components.InterstitialAdManager
import xyz.iamdua.owe.ui.components.NoInternetDialog
import xyz.iamdua.owe.ui.navigation.AppNavHost
import xyz.iamdua.owe.ui.theme.DebtCollectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LocalDiskCache.init(applicationContext)
        NetworkMonitor.start(this)
        lifecycleScope.launch(Dispatchers.IO) {
            // Hydrate disk caches & prefetch upcoming screen data while Splash plays
            ProfileRepository.ensureHydrated()
            DebtRepository.ensureHydrated()
            PaymentRepository.ensureHydrated()
            val authRepo = AuthRepository()
            if (authRepo.hasActiveSession()) {
                launch { ProfileRepository().getMyProfile() }
                launch {
                    val debts = DebtRepository().getMyDebts().getOrNull().orEmpty()
                    val debtIds = debts.map { it.id }
                    if (debtIds.isNotEmpty()) {
                        launch { PaymentRepository().getPaymentsForDebts(debtIds) }
                        launch { DebtRepository().getNotesForDebts(debtIds) }
                    }
                }
            } else {
                launch { PsgcRepository(applicationContext).getProvinces() }
            }
            MobileAds.initialize(applicationContext) {
                InterstitialAdManager.preload(applicationContext)
            }
        }
        AppConfigStore.startRealtime(lifecycleScope)
        NotificationStore.startRealtime(lifecycleScope)
        setContent {
            DebtCollectTheme {
                val isConnected by NetworkMonitor.isConnected.collectAsState()
                var dismissedOffline by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(isConnected) {
                    if (isConnected) {
                        dismissedOffline = false
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost()
                        AppFeedbackHost(modifier = Modifier.align(Alignment.BottomCenter))

                        if (!isConnected && !dismissedOffline) {
                            NoInternetDialog(
                                onRetry = {
                                    if (NetworkMonitor.checkNow(this@MainActivity)) {
                                        dismissedOffline = false
                                        scope.launch {
                                            AppConfigStore.refresh()
                                            NotificationStore.refresh()
                                        }
                                        AppFeedback.showSuccess("Back online!")
                                    } else {
                                        AppFeedback.showError("Still offline. Check your connection.")
                                    }
                                },
                                onDismiss = { dismissedOffline = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
