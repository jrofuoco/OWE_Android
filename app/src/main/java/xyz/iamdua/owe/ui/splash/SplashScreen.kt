package xyz.iamdua.owe.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import xyz.iamdua.owe.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.iamdua.owe.ui.components.OweLogo
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.DebtCollectTheme
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.StatusActiveText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    val scale = remember { Animatable(0.88f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
        delay(800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .padding(horizontal = 20.dp)
        ) {
            // OWE Owl Logo (optical center offset to account for left wing feathers)
            OweLogo(
                modifier = Modifier
                    .size(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App Name with Lime Accent Dot (balanced left spacer so "OWE" is centered)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "OWE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Dark12
                )

                // Lime Accent Dot
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LimeAccent)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Lend. Borrow. Settle.",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 10.dp)
            )
        }

        // Bottom Brand / Version Tag
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = GrayText.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    DebtCollectTheme {
        SplashScreen()
    }
}
