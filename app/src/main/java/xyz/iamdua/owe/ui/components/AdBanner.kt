package xyz.iamdua.owe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import xyz.iamdua.owe.data.local.ProPreferences
import xyz.iamdua.owe.data.repository.AppConfigStore
import xyz.iamdua.owe.ui.theme.ForestGreen
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GraySurface
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.White

// Google's official test Banner Ad Unit ID (replace with production Ad Unit ID before release)
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun FreeTierAdBanner(
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val isProFlow by ProPreferences.isProFlow.collectAsState()
    val runtimeConfig by AppConfigStore.config.collectAsState()
    val isPro = remember(isProFlow) { !isInPreview && ProPreferences.isPro(context) }

    if (isPro || (!isInPreview && !runtimeConfig.adsEnabled)) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GraySurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GrayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(StatusActiveBg)
                        .clickable { onUpgradeClick() }
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Remove ads with Pro",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            if (isInPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GraySurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AdMob Banner (Free Tier)",
                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                    )
                }
            } else {
                val adView = remember {
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = TEST_BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
                androidx.compose.runtime.DisposableEffect(adView) {
                    adView.resume()
                    onDispose {
                        adView.pause()
                    }
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp),
                    factory = {
                        (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                        adView
                    }
                )
            }
        }
    }
}
