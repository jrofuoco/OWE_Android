package xyz.iamdua.owe.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

private object LogoBitmapCache {
    @Volatile
    private var cached: ImageBitmap? = null

    fun get(context: Context): ImageBitmap {
        cached?.let { return it }
        return synchronized(this) {
            cached ?: context.applicationContext.assets.open("logo.png").use { stream ->
                BitmapFactory.decodeStream(stream).asImageBitmap()
            }.also { cached = it }
        }
    }
}

/**
 * Renders the app's final owl logo from `assets/logo.png`.
 */
@Composable
fun OweLogo(
    modifier: Modifier = Modifier,
    contentDescription: String = "OWE Logo"
) {
    val context = LocalContext.current
    val logoBitmap = remember { LogoBitmapCache.get(context) }

    Image(
        bitmap = logoBitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
