package xyz.iamdua.owe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Dark12,
    onPrimary = White,
    secondary = LimeAccent,
    onSecondary = LimeText,
    background = LightBg,
    onBackground = Dark12,
    surface = White,
    onSurface = Dark12,
    surfaceVariant = GraySurface,
    onSurfaceVariant = GrayText,
    outline = GrayBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Dark12,
    secondary = LimeAccent,
    onSecondary = LimeText,
    background = Dark12,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = GrayText,
    outline = DarkSurface
)

@Composable
fun DebtCollectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
