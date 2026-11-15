package xyz.iamdua.owe.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.White

private val SkeletonFill = Color(0xFFE4E7EC)

@Composable
private fun rememberSkeletonPulseAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    return alpha
}

/**
 * Low-contrast skeleton placeholder for HomeScreen when uncached initial data is loading.
 */
@Composable
fun HomeSkeletonLoader(
    modifier: Modifier = Modifier
) {
    val pulseAlpha = rememberSkeletonPulseAlpha()

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = pulseAlpha }
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Tab Switcher Pill Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(SkeletonFill)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Hero Card Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(SkeletonFill)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Filter Chips Row Placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .width(if (index == 0) 64.dp else 78.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(SkeletonFill)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Debt Card Placeholders
        repeat(3) {
            SkeletonDebtCardItem()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SkeletonDebtCardItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SkeletonFill)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkeletonFill)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(84.dp)
                                .height(11.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkeletonFill)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(SkeletonFill)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(108.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SkeletonFill)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(SkeletonFill)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SkeletonFill)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(SkeletonFill)
            )
        }
    }
}

/**
 * Low-contrast skeleton placeholder for DebtDetailsScreen when uncached debt details are loading.
 */
@Composable
fun DebtDetailsSkeletonLoader(
    modifier: Modifier = Modifier
) {
    val pulseAlpha = rememberSkeletonPulseAlpha()

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = pulseAlpha }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header Balance Card Placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SkeletonFill)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(SkeletonFill)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SkeletonFill)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(SkeletonFill)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(11.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(SkeletonFill)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkeletonFill)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(SkeletonFill)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SkeletonFill)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SkeletonFill)
        )

        Spacer(modifier = Modifier.height(12.dp))

        repeat(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkeletonFill)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkeletonFill)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(SkeletonFill)
                    )
                }
            }
        }
    }
}
