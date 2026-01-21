package expo.modules.iosorb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val phase: Float  // Random phase offset for variety
)

@Composable
fun ParticlesView(
    color: Color,
    particleCount: Int = 10,
    speedRange: ClosedFloatingPointRange<Float> = 10f..20f,
    sizeRange: ClosedFloatingPointRange<Float> = 0.5f..1f,
    opacityRange: ClosedFloatingPointRange<Float> = 0f..0.3f,
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    // Generate stable particles on first composition
    val particles = remember {
        (0 until particleCount).map { id ->
            Particle(
                id = id,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = Random.nextFloat() * (speedRange.endInclusive - speedRange.start) + speedRange.start,
                size = Random.nextFloat() * (sizeRange.endInclusive - sizeRange.start) + sizeRange.start,
                alpha = Random.nextFloat() * (opacityRange.endInclusive - opacityRange.start) + opacityRange.start,
                phase = Random.nextFloat() * 6.28f  // Random phase 0-2π
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            // Calculate particle position based on time
            val cycleTime = 3.0 + particle.phase  // 3-6 second cycle
            val progress = ((elapsedTime + particle.phase) % cycleTime) / cycleTime

            // Drift upward with slight horizontal wobble
            val x = particle.startX * width + sin(elapsedTime * 0.5 + particle.phase) * 20f
            val y = height * (1f - progress.toFloat()) * particle.startY + height * (1f - progress.toFloat())

            // Fade in/out based on lifecycle
            val lifecycleAlpha = when {
                progress < 0.2 -> (progress / 0.2).toFloat()  // Fade in
                progress > 0.8 -> ((1.0 - progress) / 0.2).toFloat()  // Fade out
                else -> 1f
            } * particle.alpha

            drawCircle(
                color = color.copy(alpha = lifecycleAlpha),
                radius = particle.size * 4f,
                center = Offset(x.toFloat(), y.toFloat()),
                blendMode = BlendMode.Plus
            )
        }
    }
}
