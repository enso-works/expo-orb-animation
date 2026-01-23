package expo.modules.breathing

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

@Composable
fun ProgressRingView(
    progress: Double,
    color: Color,
    lineWidth: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val size = min(this.size.width, this.size.height)
        val radius = (size - lineWidth) / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)

        // Background ring
        drawArc(
            color = color.copy(alpha = 0.2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = lineWidth)
        )

        // Progress arc
        val clampedProgress = progress.coerceIn(0.0, 1.0).toFloat()
        drawArc(
            color = color,
            startAngle = -90f,  // Start from top
            sweepAngle = 360f * clampedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = lineWidth, cap = StrokeCap.Round)
        )
    }
}
