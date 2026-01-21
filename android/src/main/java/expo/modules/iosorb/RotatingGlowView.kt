package expo.modules.iosorb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.min

enum class RotationDirection(val multiplier: Double) {
    CLOCKWISE(1.0),
    COUNTER_CLOCKWISE(-1.0)
}

@Composable
fun RotatingGlowView(
    color: Color,
    rotationSpeed: Double = 30.0,
    direction: RotationDirection,
    elapsedTime: Double,
    modifier: Modifier = Modifier,
    blurRadius: Float = 0f
) {
    val safeSpeed = maxOf(0.01, rotationSpeed)
    val rotation = (elapsedTime * safeSpeed * direction.multiplier).toFloat()

    Canvas(modifier = modifier.fillMaxSize()) {
        val sizePx = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(rotation, pivot = center) {
            // Main glow circle
            drawCircle(
                color = color,
                radius = sizePx / 2f,
                center = center,
                alpha = 0.8f
            )

            // Cutout circle offset downward (creates crescent effect)
            drawCircle(
                color = Color.Transparent,
                radius = sizePx * 0.655f,  // 1.31 / 2
                center = Offset(center.x, center.y + sizePx * 0.31f),
                blendMode = BlendMode.Clear
            )
        }
    }
}

/**
 * Simplified glow without complex masking - just a soft gradient circle
 */
@Composable
fun SimpleRotatingGlow(
    color: Color,
    rotationSpeed: Double = 30.0,
    direction: RotationDirection,
    elapsedTime: Double,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    val safeSpeed = maxOf(0.01, rotationSpeed)
    val rotation = (elapsedTime * safeSpeed * direction.multiplier).toFloat()
    // Clamp alpha to valid range 0-1
    val safeAlpha = alpha.coerceIn(0f, 1f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val sizePx = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(rotation, pivot = center) {
            // Draw offset glow to create rotating effect
            drawCircle(
                color = color.copy(alpha = (safeAlpha * 0.9f).coerceIn(0f, 1f)),
                radius = sizePx * 0.4f,
                center = Offset(center.x, center.y - sizePx * 0.15f)
            )
            drawCircle(
                color = color.copy(alpha = (safeAlpha * 0.6f).coerceIn(0f, 1f)),
                radius = sizePx * 0.3f,
                center = Offset(center.x + sizePx * 0.1f, center.y + sizePx * 0.1f)
            )
        }
    }
}
