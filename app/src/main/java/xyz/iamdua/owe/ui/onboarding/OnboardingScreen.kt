package xyz.iamdua.owe.ui.onboarding

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import xyz.iamdua.owe.R
import xyz.iamdua.owe.ui.theme.Dark12
import xyz.iamdua.owe.ui.theme.GrayBorder
import xyz.iamdua.owe.ui.theme.GrayText
import xyz.iamdua.owe.ui.theme.LightBg
import xyz.iamdua.owe.ui.theme.LimeAccent
import xyz.iamdua.owe.ui.theme.StatusActiveBg
import xyz.iamdua.owe.ui.theme.StatusActiveText
import xyz.iamdua.owe.ui.theme.White
import kotlin.math.absoluteValue

object OnboardingPrefs {
    private const val PREFS_NAME = "owe_prefs"
    private const val KEY_COMPLETED = "onboarding_completed"

    fun isCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETED, false)
    }

    fun setCompleted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_COMPLETED, true)
            .apply()
    }
}

private data class OnboardingPage(
    val badge: String,
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        badge = "01 · BORROW & LEND",
        title = "Borrow & Lend\nwith Clarity",
        description = "Create a loan record in seconds or join using an invite code so both sides agree on the exact amount and terms.",
        imageRes = R.drawable.onboarding_slide_1
    ),
    OnboardingPage(
        badge = "02 · TRACK",
        title = "Track Every Peso\n& Due Date",
        description = "Monitor remaining balances, repayment progress, and upcoming due dates in real time—no more guesswork.",
        imageRes = R.drawable.onboarding_slide_2
    ),
    OnboardingPage(
        badge = "03 · SETTLE",
        title = "Settle Up\nwith Proof",
        description = "Submit payments with notes or proof, get lender confirmation, and mark every debt cleanly as settled.",
        imageRes = R.drawable.onboarding_slide_3
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    val entranceAlpha = remember { Animatable(0f) }
    val entranceOffset = remember { Animatable(32f) }

    LaunchedEffect(Unit) {
        launch {
            entranceAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        launch {
            entranceOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
    }

    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .graphicsLayer {
                alpha = entranceAlpha.value
                translationY = entranceOffset.value
            }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Bar: Skip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = GrayText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onFinished() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Swipeable Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                .absoluteValue
                .coerceIn(0f, 1f)

            val imageScale = 1f - (pageOffset * 0.12f)
            val contentAlpha = 1f - (pageOffset * 0.65f)
            val contentTranslationY = pageOffset * 36f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Illustration with Parallax Scale & Fade (no card)
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = page.title.replace("\n", " "),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            alpha = contentAlpha
                        }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Step Badge
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentTranslationY
                        }
                        .clip(CircleShape)
                        .background(StatusActiveBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = page.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = StatusActiveText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp
                    ),
                    color = Dark12,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentTranslationY * 1.2f
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentTranslationY * 1.4f
                        }
                )
            }
        }

        // Bottom Controls: Animated Dot Indicators + Back / Next / Get Started
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "dot_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) StatusActiveText else GrayBorder,
                        animationSpec = tween(durationMillis = 300),
                        label = "dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(visible = pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(White)
                            .border(1.dp, GrayBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Previous slide",
                            tint = Dark12
                        )
                    }
                }

                val buttonBg by animateColorAsState(
                    targetValue = if (isLastPage) StatusActiveText else Dark12,
                    animationSpec = tween(durationMillis = 300),
                    label = "cta_bg"
                )

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinished()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    AnimatedContent(
                        targetState = isLastPage,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInHorizontally { it / 6 }) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally { -it / 6 })
                        },
                        label = "cta_text"
                    ) { last ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (last) "Get Started" else "Next",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = if (last) LimeAccent else White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
