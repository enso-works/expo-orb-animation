package expo.modules.breathing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import expo.modules.orb.ParticlesView
import expo.modules.orb.RotationDirection
import expo.modules.orb.SimpleRotatingGlow
import expo.modules.orb.WavyBlobView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan
import kotlin.random.Random

@Composable
fun BreathingExerciseView(
    config: BreathingConfiguration,
    modifier: Modifier = Modifier
) {
    // Frame-based animation state
    var frameTime by remember { mutableLongStateOf(System.nanoTime()) }
    var elapsedTime by remember { mutableStateOf(0.0) }

    // Continuous animation loop
    LaunchedEffect(Unit) {
        val startTime = System.nanoTime()
        while (true) {
            withFrameNanos { currentTime ->
                frameTime = currentTime
                elapsedTime = (currentTime - startTime) / 1_000_000_000.0
            }
        }
    }

    // Initialize wobble offsets if needed
    val state = BreathingSharedState
    if (state.wobbleOffsets.size != config.pointCount) {
        state.wobbleOffsets = MutableList(config.pointCount) { 0.0 }
        state.wobbleTargets = MutableList(config.pointCount) { 0.0 }
    }

    // Update animation state
    val animationState = updateAnimationState(frameTime, config)

    // Create blob shape for clipping
    val blobShape = remember(animationState.scale, animationState.wobbleOffsets, config.pointCount) {
        MorphingBlobShape(
            baseRadius = animationState.scale.toFloat() * 0.7f,
            pointCount = config.pointCount,
            offsets = animationState.wobbleOffsets
        )
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Progress ring (behind blob)
        if (config.showProgressRing && animationState.isActive) {
            ProgressRingView(
                progress = animationState.phaseProgress,
                color = config.progressRingColor,
                lineWidth = 4f,
                modifier = Modifier.fillMaxSize(0.95f)
            )
        }

        // Main blob with all effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false }
        ) {
            // Shadow
            if (config.showShadow) {
                BreathingShadow(
                    colors = config.blobColors,
                    scale = animationState.scale.toFloat(),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { clip = false }
                )
            }

            // Main blob content clipped to morphing shape
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(blobShape)
            ) {
                // Background gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = config.blobColors.reversed()
                            )
                        )
                )

                // Base depth glows
                BaseDepthGlows(
                    glowColor = config.glowColor,
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
                        glowColor = config.glowColor,
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
                        particleCount = 15,
                        speedRange = 10f..20f,
                        sizeRange = 1f..3f,
                        opacityRange = 0.1f..0.4f,
                        elapsedTime = elapsedTime,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(1.dp)
                    )
                    ParticlesView(
                        color = config.particleColor,
                        particleCount = 10,
                        speedRange = 20f..35f,
                        sizeRange = 0.5f..2f,
                        opacityRange = 0.3f..0.7f,
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

        // Text cue (on top)
        if (config.showTextCue && animationState.label.isNotEmpty()) {
            BreathingTextCue(
                text = animationState.label,
                color = config.textColor
            )
        }
    }
}

// Custom shape for clipping
private class MorphingBlobShape(
    private val baseRadius: Float,
    private val pointCount: Int,
    private val offsets: List<Double>
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        if (pointCount < 3) return Outline.Generic(path)

        val minDim = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)
        val effectiveRadius = baseRadius * minDim / 2f

        // Generate points around the circle with offsets
        val points = mutableListOf<Offset>()
        for (i in 0 until pointCount) {
            val angle = (i.toDouble() / pointCount) * 2 * PI - PI / 2
            val offset = if (i < offsets.size) offsets[i] else 0.0
            val radius = effectiveRadius * (1.0 + offset).toFloat()

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

            val currentAngle = (i.toDouble() / pointCount) * 2 * PI - PI / 2
            val nextAngle = (((i + 1) % pointCount).toDouble() / pointCount) * 2 * PI - PI / 2

            val currentOffset = if (i < offsets.size) offsets[i] else 0.0
            val currentRadius = effectiveRadius * (1.0 + currentOffset).toFloat()
            val currentTangentLength = currentRadius * tangentCoeff

            val nextOffset = if ((i + 1) % pointCount < offsets.size) offsets[(i + 1) % pointCount] else 0.0
            val nextRadius = effectiveRadius * (1.0 + nextOffset).toFloat()
            val nextTangentLength = nextRadius * tangentCoeff

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
        return Outline.Generic(path)
    }
}

private data class AnimationState(
    val scale: Double,
    val wobbleOffsets: List<Double>,
    val phaseProgress: Double,
    val label: String,
    val isActive: Boolean
)

@Composable
private fun BaseDepthGlows(
    glowColor: Color,
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    val speed = 18.0
    Box(modifier = modifier) {
        SimpleRotatingGlow(
            color = glowColor,
            rotationSpeed = speed * 0.75,
            direction = RotationDirection.COUNTER_CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = 0.5f,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .blur(8.dp)
        )
        SimpleRotatingGlow(
            color = glowColor.copy(alpha = 0.5f),
            rotationSpeed = speed * 0.25,
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
    glowColor: Color,
    elapsedTime: Double,
    modifier: Modifier = Modifier
) {
    val speed = 18.0
    val glowIntensity = 0.8
    Box(modifier = modifier) {
        SimpleRotatingGlow(
            color = glowColor,
            rotationSpeed = speed * 1.2,
            direction = RotationDirection.CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = (glowIntensity * 0.8).toFloat(),
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp)
        )
        SimpleRotatingGlow(
            color = glowColor,
            rotationSpeed = speed * 0.9,
            direction = RotationDirection.CLOCKWISE,
            elapsedTime = elapsedTime,
            alpha = (glowIntensity * 0.6).toFloat(),
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
private fun BreathingShadow(
    colors: List<Color>,
    scale: Float,
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
                radius = size.minDimension / 2f * scale * 0.7f
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
                radius = size.minDimension / 2f * scale * 0.7f
            )
        }
    }
}

private fun easeOutCubic(t: Double): Double {
    val t1 = t - 1
    return t1 * t1 * t1 + 1
}

private fun updateAnimationState(
    frameTime: Long,
    config: BreathingConfiguration
): AnimationState {
    val state = BreathingSharedState

    // Always update wobble animation
    updateWobble(frameTime, config)

    return when (state.state) {
        BreathingExerciseState.STOPPED, BreathingExerciseState.COMPLETE -> {
            AnimationState(
                scale = 1.0,
                wobbleOffsets = state.wobbleOffsets.toList(),
                phaseProgress = 0.0,
                label = "",
                isActive = false
            )
        }

        BreathingExerciseState.PAUSED -> {
            AnimationState(
                scale = state.currentScale,
                wobbleOffsets = state.wobbleOffsets.toList(),
                phaseProgress = state.phaseProgress,
                label = state.currentLabel,
                isActive = true
            )
        }

        BreathingExerciseState.RUNNING -> {
            // Update phase timing
            updatePhaseState(frameTime)

            // Calculate scale and progress based on phase
            val currentPhaseConfig = if (state.phases.isEmpty()) null else state.phases[state.currentPhaseIndex]
            val scale: Double
            val ringProgress: Double

            if (currentPhaseConfig != null) {
                when (currentPhaseConfig.phase) {
                    BreathPhase.INHALE -> {
                        val easedProgress = easeOutCubic(state.phaseProgress)
                        scale = state.startScale + (state.targetScale - state.startScale) * easedProgress
                        ringProgress = easedProgress
                    }
                    BreathPhase.EXHALE -> {
                        val easedProgress = easeOutCubic(state.phaseProgress)
                        scale = state.startScale + (state.targetScale - state.startScale) * easedProgress
                        ringProgress = 1.0 - easedProgress
                    }
                    BreathPhase.HOLD_IN -> {
                        scale = state.targetScale
                        ringProgress = 1.0
                    }
                    BreathPhase.HOLD_OUT -> {
                        scale = state.targetScale
                        ringProgress = 0.0
                    }
                    BreathPhase.IDLE -> {
                        scale = state.targetScale
                        ringProgress = 0.0
                    }
                }
            } else {
                scale = 1.0
                ringProgress = 0.0
            }

            state.currentScale = scale

            AnimationState(
                scale = scale,
                wobbleOffsets = state.wobbleOffsets.toList(),
                phaseProgress = ringProgress,
                label = state.currentLabel,
                isActive = true
            )
        }
    }
}

private fun updatePhaseState(frameTime: Long) {
    val state = BreathingSharedState
    if (state.phases.isEmpty()) return

    val currentPhaseConfig = state.phases[state.currentPhaseIndex]
    val elapsed = (frameTime - state.phaseStartTime) / 1_000_000_000.0
    val duration = currentPhaseConfig.duration

    // Calculate progress through current phase
    state.phaseProgress = min(1.0, elapsed / duration)

    // Check if phase is complete
    if (elapsed >= duration) {
        // Move to next phase
        state.currentPhaseIndex = (state.currentPhaseIndex + 1) % state.phases.size

        // Check if we completed a cycle
        if (state.currentPhaseIndex == 0) {
            state.currentCycle += 1

            // Check if exercise is complete
            val totalCycles = state.totalCycles
            if (totalCycles != null && state.currentCycle >= totalCycles) {
                state.state = BreathingExerciseState.COMPLETE
                state.totalDuration = (frameTime - state.exerciseStartTime) / 1_000_000_000.0
                state.onExerciseComplete?.invoke(state.currentCycle, state.totalDuration)
                return
            }
        }

        // Start new phase
        state.phaseStartTime = frameTime
        val newPhaseConfig = state.phases[state.currentPhaseIndex]
        state.currentPhase = newPhaseConfig.phase
        state.currentLabel = newPhaseConfig.label
        state.startScale = state.currentScale
        state.targetScale = newPhaseConfig.targetScale
        state.phaseProgress = 0.0

        // Update wobble intensity based on phase
        state.wobbleIntensity = when (newPhaseConfig.phase) {
            BreathPhase.INHALE, BreathPhase.EXHALE -> 1.0
            BreathPhase.HOLD_IN, BreathPhase.HOLD_OUT -> 0.3
            BreathPhase.IDLE -> 0.5
        }

        // Fire phase change callback
        state.onPhaseChange?.invoke(
            newPhaseConfig.phase,
            newPhaseConfig.label,
            state.currentPhaseIndex,
            state.currentCycle
        )
    }
}

private fun updateWobble(frameTime: Long, config: BreathingConfiguration) {
    val state = BreathingSharedState

    // Ensure arrays are sized correctly
    if (state.wobbleOffsets.size != config.pointCount) {
        state.wobbleOffsets = MutableList(config.pointCount) { 0.0 }
        state.wobbleTargets = MutableList(config.pointCount) { 0.0 }
    }

    // Check if we need new random targets (slower, more organic)
    val timeSinceLastTarget = (frameTime - state.lastWobbleTargetUpdate) / 1_000_000_000.0
    if (timeSinceLastTarget > 1.8 || state.wobbleTargets.all { it == 0.0 }) {
        state.lastWobbleTargetUpdate = frameTime
        val intensity = state.wobbleIntensity * config.wobbleIntensity
        val maxOffset = 0.18 * intensity

        for (i in 0 until config.pointCount) {
            val phase = i.toDouble() / config.pointCount * 2.0 * PI
            val wave1 = sin(phase * 2 + Random.nextDouble()) * 0.6
            val wave2 = cos(phase * 3 + Random.nextDouble()) * 0.4
            val baseOffset = Random.nextDouble(-maxOffset, maxOffset)
            state.wobbleTargets[i] = baseOffset * (1.0 + wave1 + wave2) * 0.5
        }
    }

    // Smoothly interpolate offsets toward targets
    val dt = (frameTime - state.lastWobbleUpdate) / 1_000_000_000.0
    state.lastWobbleUpdate = frameTime
    val interpolationSpeed = 1.8
    val factor = min(1.0, dt * interpolationSpeed)

    for (i in 0 until min(state.wobbleOffsets.size, state.wobbleTargets.size)) {
        state.wobbleOffsets[i] = state.wobbleOffsets[i] + (state.wobbleTargets[i] - state.wobbleOffsets[i]) * factor
    }
}
