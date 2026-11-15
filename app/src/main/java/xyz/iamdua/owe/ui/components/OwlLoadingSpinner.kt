package xyz.iamdua.owe.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val DEFAULT_OWL_OUTLINE_PATH =
    "M 155,45 L 140,8 L 175,32 C 185,42 195,55 205,65 C 235,55 265,55 295,65 C 305,55 315,42 325,32 L 360,8 L 345,45 C 390,70 455,130 472,255 C 485,355 475,480 435,570 C 395,660 330,682 295,682 C 285,682 278,675 272,668 C 260,682 235,685 220,680 C 205,675 195,662 190,650 C 140,620 85,570 55,510 L 25,525 L 35,465 L 12,430 L 42,400 L 18,360 L 50,335 C 45,280 50,210 75,150 C 100,90 130,60 155,45 Z"

private val PATH_D_REGEX = Regex("""\bd="([^"]+)"""")

private object OwlPathCache {
    @Volatile
    private var cachedPath: Path? = null

    fun get(context: android.content.Context): Path {
        cachedPath?.let { return it }
        return synchronized(this) {
            cachedPath ?: run {
                val pathData = runCatching {
                    context.applicationContext.assets.open("loading_outline.svg").bufferedReader().use { reader ->
                        PATH_D_REGEX.find(reader.readText())?.groupValues?.get(1)
                    }
                }.getOrNull() ?: DEFAULT_OWL_OUTLINE_PATH
                PathParser().parsePathString(pathData).toPath()
            }.also { cachedPath = it }
        }
    }
}

/**
 * Animated owl contour loading indicator backed by `assets/loading_outline.svg`.
 */
@Composable
fun OwlLoadingSpinner(
    modifier: Modifier = Modifier.size(36.dp),
    color: Color = Color(0xFF137333),
    strokeWidth: Dp = 2.5.dp,
    durationMillis: Int = 1200
) {
    val context = LocalContext.current
    val basePath = remember { OwlPathCache.get(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "owl_outline_trace")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "owl_trace_progress"
    )

    val scaledPath = remember { Path() }
    val segmentPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }
    val transformMatrix = remember { Matrix() }
    var lastSizeW = remember { -1f }
    var lastSizeH = remember { -1f }

    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        if (size.width != lastSizeW || size.height != lastSizeH) {
            lastSizeW = size.width
            lastSizeH = size.height
            val pad = strokePx
            val availW = (size.width - pad * 2f).coerceAtLeast(1f)
            val availH = (size.height - pad * 2f).coerceAtLeast(1f)
            val scale = minOf(availW / 500f, availH / 700f)
            val offsetX = (size.width - 500f * scale) / 2f
            val offsetY = (size.height - 700f * scale) / 2f

            scaledPath.reset()
            scaledPath.addPath(basePath)
            transformMatrix.reset()
            transformMatrix.translate(x = offsetX, y = offsetY)
            transformMatrix.scale(x = scale, y = scale)
            scaledPath.transform(transformMatrix)
            pathMeasure.setPath(scaledPath, forceClosed = true)
        }

        val totalLength = pathMeasure.length
        val segmentLength = totalLength * 0.42f
        val head = totalLength * progress
        val tail = head - segmentLength

        segmentPath.reset()
        if (tail >= 0f) {
            pathMeasure.getSegment(
                startDistance = tail,
                stopDistance = head,
                destination = segmentPath,
                startWithMoveTo = true
            )
        } else {
            pathMeasure.getSegment(
                startDistance = totalLength + tail,
                stopDistance = totalLength,
                destination = segmentPath,
                startWithMoveTo = true
            )
            if (head > 0f) {
                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = head,
                    destination = segmentPath,
                    startWithMoveTo = false
                )
            }
        }

        // Subtle outer glow
        drawPath(
            path = segmentPath,
            color = color.copy(alpha = 0.25f),
            style = Stroke(
                width = strokePx * 2.2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        // Main crisp contour trace
        drawPath(
            path = segmentPath,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
