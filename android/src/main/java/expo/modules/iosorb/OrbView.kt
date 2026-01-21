package expo.modules.iosorb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun OrbView(
    config: OrbConfiguration,
    useSharedActivityState: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Frame-based animation state
    var elapsedTime by remember { mutableStateOf(0.0) }
    var frameTime by remember { mutableStateOf(System.nanoTime()) }

    // Continuous animation loop
    LaunchedEffect(Unit) {
        val startTime = System.nanoTime()
        while (true) {
            withFrameNanos { currentTime ->
                elapsedTime = (currentTime - startTime) / 1_000_000_000.0
                frameTime = currentTime
            }
        }
    }

    // Interpolate activity
    val activity = if (useSharedActivityState) {
        interpolatedActivity(frameTime)
    } else {
        0.0
    }

    // Compute effective config from activity
    val effectiveConfig = activityDerivedConfig(activity, config)

    // Calculate breathing scale
    val scale = breathingScale(frameTime, effectiveConfig)

    // Outer container with padding to allow glow/shadow overflow
    Box(
        modifier = modifier
            .graphicsLayer {
                // Disable clipping to allow glow/shadow overflow
                clip = false
            }
            .aspectRatio(1f)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Shadow layers (rendered outside the circle)
        if (config.showShadow) {
            RealisticShadow(
                colors = config.backgroundColors,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { clip = false }
            )
        }

        // Main orb content clipped to circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            // Background gradient
            if (config.showBackground) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = config.backgroundColors.reversed()
                            )
                        )
                )
            }

            // Base depth glows
            BaseDepthGlows(
                effectiveConfig = effectiveConfig,
                elapsedTime = elapsedTime,
                modifier = Modifier.fillMaxSize()
            )

            // Wavy blobs
            if (config.showWavyBlobs) {
                WavyBlobLayer(
                    elapsedTime = elapsedTime,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Core glow effects
            if (config.showGlowEffects) {
                CoreGlowEffects(
                    effectiveConfig = effectiveConfig,
                    elapsedTime = elapsedTime,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            // Particles
            if (config.showParticles) {
                ParticlesView(
                    color = config.particleColor,
                    particleCount = 10,
                    speedRange = 10f..20f,
                    sizeRange = 0.5f..1f,
                    opacityRange = 0f..0.3f,
                    elapsedTime = elapsedTime,
                    modifier = Modifier.fillMaxSize()
                )
                ParticlesView(
                    color = config.particleColor,
                    particleCount = 10,
                    speedRange = 20f..30f,
                    sizeRange = 0.2f..1f,
                    opacityRange = 0.3f..0.8f,
                    elapsedTime = elapsedTime,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Inner glow overlay
            InnerGlowOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun interpolatedActivity(currentTime: Long): Double {
    val state = OrbSharedState
    val target = state.targetActivity
    val dt = (currentTime - state.lastUpdateTime) / 1_000_000_000.0  // Convert to seconds
    state.lastUpdateTime = currentTime

    // Smooth interpolation (same as iOS)
    val factor = min(1.0, dt * 6.0)
    val next = state.currentActivity + (target - state.currentActivity) * factor
    state.currentActivity = next

    return next
}

private fun activityDerivedConfig(activity: Double, config: OrbConfiguration): EffectiveConfig {
    // IMPORTANT: Rotation speed must stay CONSTANT
    val speed = 18.0
    // Breathing speed CAN vary because we use cumulative phase
    val breathingSpeed = 0.03 + activity * 0.25
    // Idle: no breathing, Speaking: full breathing
    val breathingIntensity = max(0.0, (activity - 0.2)) * 1.25
    // Idle: barely visible glow, Speaking: bright
    val coreGlowIntensity = 0.08 + activity * 1.8

    return EffectiveConfig(
        speed = speed,
        breathingIntensity = breathingIntensity,
        breathingSpeed = breathingSpeed,
        coreGlowIntensity = coreGlowIntensity,
        glowColor = config.glowColor
    )
}

private fun breathingScale(currentTime: Long, effectiveConfig: EffectiveConfig): Float {
    val intensity = max(0.0, min(1.0, effectiveConfig.breathingIntensity))
    if (intensity == 0.0) {
        return 1f
    }

    val state = OrbSharedState
    val dt = (currentTime - state.lastBreathingUpdate) / 1_000_000_000.0
    state.lastBreathingUpdate = currentTime

    val speed = max(0.01, effectiveConfig.breathingSpeed)
    state.breathingPhase += dt * speed * 2 * PI

    // Punchy wave shape
    val rawWave = sin(state.breathingPhase)
    val wave = if (rawWave >= 0) {
        rawWave.pow(0.6)
    } else {
        -abs(rawWave).pow(1.4)
    }

    val amplitude = intensity * 0.17
    return (1.0 + amplitude * wave).toFloat()
}

@Composable
private fun BaseDepthGlows(
    effectiveConfig: EffectiveConfig,
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        SimpleRotatingGlow(
            color = effectiveConfig.glowColor,
            rotationSpeed = effectiveConfig.speed * 0.75,
            direction = RotationDirection.COUNTER_CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = 0.5f,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .blur(8.dp)
        )
        SimpleRotatingGlow(
            color = effectiveConfig.glowColor.copy(alpha = 0.5f),
            rotationSpeed = effectiveConfig.speed * 0.25,
            direction = RotationDirection.CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = 0.3f,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .blur(4.dp)
        )
    }
}

@Composable
private fun CoreGlowEffects(
    effectiveConfig: EffectiveConfig,
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        SimpleRotatingGlow(
            color = effectiveConfig.glowColor,
            rotationSpeed = effectiveConfig.speed * 1.2,
            direction = RotationDirection.CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = (effectiveConfig.coreGlowIntensity * 0.8).toFloat(),
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp)
        )
        SimpleRotatingGlow(
            color = effectiveConfig.glowColor,
            rotationSpeed = effectiveConfig.speed * 0.9,
            direction = RotationDirection.CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = (effectiveConfig.coreGlowIntensity * 0.6).toFloat(),
            modifier = Modifier
                .fillMaxSize()
                .blur(8.dp)
        )
    }
}

@Composable
private fun WavyBlobLayer(
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    val blobSpeed = 20.0

    Box(modifier = modifier) {
        WavyBlobView(
            color = Color.White.copy(alpha = 0.4f),
            loopDuration = 60.0 / blobSpeed * 1.75,
            elapsedTime = elapsedTime,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.5f)
                .offset(y = 40.dp)
                .blur(2.dp)
        )
        WavyBlobView(
            color = Color.White.copy(alpha = 0.25f),
            loopDuration = 60.0 / blobSpeed * 2.25,
            elapsedTime = elapsedTime,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.2f)
                .offset(y = (-40).dp)
                .graphicsLayer { rotationZ = 90f }
                .blur(2.dp)
        )
    }
}

@Composable
private fun InnerGlowOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f

        // Inner glow rings
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.15f)),
            ),
            radius = radius,
            center = center
        )
    }
}

@Composable
private fun RealisticShadow(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Soft outer shadow
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 8.dp)
                .blur(24.dp)
                .graphicsLayer { alpha = 0.3f }
        ) {
            drawCircle(
                brush = Brush.verticalGradient(colors.reversed()),
                radius = size.minDimension / 2f
            )
        }
        // Closer shadow
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp)
                .blur(12.dp)
                .graphicsLayer { alpha = 0.5f }
        ) {
            drawCircle(
                brush = Brush.verticalGradient(colors.reversed()),
                radius = size.minDimension / 2f
            )
        }
    }
}
