package expo.modules.breathing

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

@Composable
fun MorphingBlobView(
    baseRadius: Float,
    pointCount: Int,
    offsets: List<Double>,
    colors: List<Color>,
    innerColor: Color,
    showInnerBlob: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val size = min(this.size.width, this.size.height)
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val effectiveRadius = baseRadius * size / 2f

        // Draw main blob
        val mainPath = createBlobPath(center, effectiveRadius, pointCount, offsets)
        drawPath(
            path = mainPath,
            brush = Brush.radialGradient(
                colors = colors,
                center = center,
                radius = effectiveRadius
            )
        )

        // Draw inner blob for depth
        if (showInnerBlob) {
            val innerPath = createBlobPath(
                center = center,
                baseRadius = effectiveRadius * 0.6f,
                pointCount = pointCount,
                offsets = offsets.map { it * 0.7 }
            )
            drawPath(
                path = innerPath,
                color = innerColor
            )
        }
    }
}

private fun DrawScope.createBlobPath(
    center: Offset,
    baseRadius: Float,
    pointCount: Int,
    offsets: List<Double>
): Path {
    val path = Path()
    if (pointCount < 3) return path

    // Generate points around the circle with offsets
    val points = mutableListOf<Offset>()
    for (i in 0 until pointCount) {
        val angle = (i.toDouble() / pointCount) * 2 * PI - PI / 2
        val offset = if (i < offsets.size) offsets[i] else 0.0
        val radius = baseRadius * (1.0 + offset).toFloat()

        points.add(
            Offset(
                x = center.x + (cos(angle) * radius).toFloat(),
                y = center.y + (sin(angle) * radius).toFloat()
            )
        )
    }

    // Tangent coefficient for smooth cubic bezier curves
    val tangentCoeff = ((4.0 / 3.0) * tan(PI / (2.0 * pointCount))).toFloat()

    // Start at first point
    path.moveTo(points[0].x, points[0].y)

    // Draw cubic bezier curves between each pair of points
    for (i in 0 until pointCount) {
        val current = points[i]
        val next = points[(i + 1) % pointCount]

        // Calculate angles for tangent directions
        val currentAngle = (i.toDouble() / pointCount) * 2 * PI - PI / 2
        val nextAngle = (((i + 1) % pointCount).toDouble() / pointCount) * 2 * PI - PI / 2

        // Current point's radius and tangent
        val currentOffset = if (i < offsets.size) offsets[i] else 0.0
        val currentRadius = baseRadius * (1.0 + currentOffset).toFloat()
        val currentTangentLength = currentRadius * tangentCoeff

        // Next point's radius and tangent
        val nextOffset = if ((i + 1) % pointCount < offsets.size) offsets[(i + 1) % pointCount] else 0.0
        val nextRadius = baseRadius * (1.0 + nextOffset).toFloat()
        val nextTangentLength = nextRadius * tangentCoeff

        // Control points are perpendicular to radial direction
        val control1 = Offset(
            x = current.x + (cos(currentAngle + PI / 2) * currentTangentLength).toFloat(),
            y = current.y + (sin(currentAngle + PI / 2) * currentTangentLength).toFloat()
        )

        val control2 = Offset(
            x = next.x + (cos(nextAngle - PI / 2) * nextTangentLength).toFloat(),
            y = next.y + (sin(nextAngle - PI / 2) * nextTangentLength).toFloat()
        )

        path.cubicTo(
            control1.x, control1.y,
            control2.x, control2.y,
            next.x, next.y
        )
    }

    path.close()
    return path
}
