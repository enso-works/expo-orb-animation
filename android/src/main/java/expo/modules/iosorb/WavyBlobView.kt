package expo.modules.iosorb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class BlobPoint(val x: Float, val y: Float)

@Composable
fun WavyBlobView(
    color: Color,
    loopDuration: Double = 1.0,
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    // Initial blob points (6 points in a circle)
    val basePoints = (0 until 6).map { index ->
        val angle = (index.toDouble() / 6.0) * 2.0 * PI
        BlobPoint(
            x = (0.5 + cos(angle) * 0.9).toFloat(),
            y = (0.5 + sin(angle) * 0.9).toFloat()
        )
    }

    val angle = ((elapsedTime % loopDuration) / loopDuration) * 2.0 * PI

    Canvas(modifier = modifier.fillMaxSize()) {
        val sizePx = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = sizePx * 0.45f

        // Adjust points with sine wave movement
        val adjustedPoints = basePoints.mapIndexed { index, point ->
            val phaseOffset = index * PI / 3.0
            val xOffset = sin(angle + phaseOffset) * 0.15
            val yOffset = cos(angle + phaseOffset) * 0.15
            Offset(
                x = ((point.x - 0.5f + xOffset.toFloat()) * radius + center.x).toFloat(),
                y = ((point.y - 0.5f + yOffset.toFloat()) * radius + center.y).toFloat()
            )
        }

        val path = Path().apply {
            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)

            for (i in adjustedPoints.indices) {
                val next = (i + 1) % adjustedPoints.size

                // Calculate angles for control points
                val currentAngle = atan2(
                    adjustedPoints[i].y - center.y,
                    adjustedPoints[i].x - center.x
                )
                val nextAngle = atan2(
                    adjustedPoints[next].y - center.y,
                    adjustedPoints[next].x - center.x
                )

                val handleLength = radius * 0.33f

                val control1 = Offset(
                    x = adjustedPoints[i].x + cos(currentAngle + PI.toFloat() / 2f) * handleLength,
                    y = adjustedPoints[i].y + sin(currentAngle + PI.toFloat() / 2f) * handleLength
                )
                val control2 = Offset(
                    x = adjustedPoints[next].x + cos(nextAngle - PI.toFloat() / 2f) * handleLength,
                    y = adjustedPoints[next].y + sin(nextAngle - PI.toFloat() / 2f) * handleLength
                )

                cubicTo(
                    control1.x, control1.y,
                    control2.x, control2.y,
                    adjustedPoints[next].x, adjustedPoints[next].y
                )
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}
