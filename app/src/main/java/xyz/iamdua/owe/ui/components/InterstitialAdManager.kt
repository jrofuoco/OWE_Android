package xyz.iamdua.owe.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import xyz.iamdua.owe.data.local.ProPreferences
import xyz.iamdua.owe.data.repository.AppConfigStore

/**
 * Manages AdMob Interstitial ads for Free-tier users with a strict cooldown
 * so ads only appear at natural completion points without overdoing it.
 */
object InterstitialAdManager {
    // Google's official test Interstitial Ad Unit ID (replace with production ID before release)
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastShownAtMs = 0L
    private val mainHandler = Handler(Looper.getMainLooper())

    fun preload(context: Context) {
        val appContext = context.applicationContext
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { preloadInternal(appContext) }
        } else {
            preloadInternal(appContext)
        }
    }

    private fun preloadInternal(appContext: Context) {
        val config = AppConfigStore.config.value
        if (ProPreferences.isPro(appContext) || !config.adsEnabled) return
        if (interstitialAd != null || isLoading) return

        isLoading = true
        InterstitialAd.load(
            appContext,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Shows an interstitial if the user is on the Free tier, the cooldown has elapsed,
     * and an ad is already preloaded. Always invokes [onComplete] so navigation is never blocked.
     */
    fun showIfReady(context: Context, onComplete: () -> Unit) {
        val config = AppConfigStore.config.value
        if (ProPreferences.isPro(context) || !config.adsEnabled) {
            onComplete()
            return
        }

        val cooldownMs = config.interstitialCooldownMinutes * 60_000L
        val now = System.currentTimeMillis()
        if (now - lastShownAtMs < cooldownMs) {
            preload(context)
            onComplete()
            return
        }

        val activity = context.findActivity()
        val ad = interstitialAd
        if (activity == null || ad == null) {
            preload(context)
            onComplete()
            return
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                lastShownAtMs = System.currentTimeMillis()
                preload(context)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                preload(context)
                onComplete()
            }
        }
        ad.show(activity)
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
