package expo.modules.breathing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.random.Random

@Composable
fun BreathingExerciseView(
    config: BreathingConfiguration,
    modifier: Modifier = Modifier
) {
    // Frame-based animation state
    var frameTime by remember { mutableLongStateOf(System.nanoTime()) }

    // Continuous animation loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { currentTime ->
                frameTime = currentTime
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

    Box(
        modifier = modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Progress ring (behind blob)
        if (config.showProgressRing) {
            ProgressRingView(
                progress = animationState.phaseProgress,
                color = config.progressRingColor,
                lineWidth = 8f,
                modifier = Modifier
                    .fillMaxSize(0.95f)
            )
        }

        // Main morphing blob with shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (config.showShadow) {
                        Modifier.drawBehind {
                            // Simple shadow approximation
                            drawCircle(
                                color = config.blobColors.firstOrNull()?.copy(alpha = 0.3f) ?: Color.Transparent,
                                radius = size.minDimension / 2 * animationState.scale.toFloat() * 0.7f,
                                center = center.copy(y = center.y + 8.dp.toPx())
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            MorphingBlobView(
                baseRadius = animationState.scale.toFloat() * 0.7f,
                pointCount = config.pointCount,
                offsets = animationState.wobbleOffsets,
                colors = config.blobColors,
                innerColor = config.innerBlobColor,
                showInnerBlob = config.showInnerBlob,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Text cue
        if (config.showTextCue) {
            BreathingTextCue(
                text = animationState.label,
                color = config.textColor
            )
        }
    }
}

private data class AnimationState(
    val scale: Double,
    val wobbleOffsets: List<Double>,
    val phaseProgress: Double,
    val label: String
)

private fun updateAnimationState(
    frameTime: Long,
    config: BreathingConfiguration
): AnimationState {
    val state = BreathingSharedState

    // Handle different states
    return when (state.state) {
        BreathingExerciseState.STOPPED, BreathingExerciseState.COMPLETE -> {
            AnimationState(
                scale = 1.0,
                wobbleOffsets = List(config.pointCount) { 0.0 },
                phaseProgress = 0.0,
                label = ""
            )
        }

        BreathingExerciseState.PAUSED -> {
            AnimationState(
                scale = state.currentScale,
                wobbleOffsets = state.wobbleOffsets.toList(),
                phaseProgress = state.phaseProgress,
                label = state.currentLabel
            )
        }

        BreathingExerciseState.RUNNING -> {
            // Update phase timing
            updatePhaseState(frameTime)

            // Interpolate scale toward target
            val dt = (frameTime - state.lastWobbleUpdate) / 1_000_000_000.0
            state.lastWobbleUpdate = frameTime

            // Smooth scale interpolation
            val scaleFactor = min(1.0, dt * 3.0)
            state.currentScale = state.currentScale + (state.targetScale - state.currentScale) * scaleFactor

            // Update wobble animation
            updateWobble(frameTime, config)

            AnimationState(
                scale = state.currentScale,
                wobbleOffsets = state.wobbleOffsets.toList(),
                phaseProgress = state.phaseProgress,
                label = state.currentLabel
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
    val wobbleUpdateInterval = 0.8  // New targets every 0.8 seconds

    // Ensure arrays are sized correctly
    if (state.wobbleOffsets.size != config.pointCount) {
        state.wobbleOffsets = MutableList(config.pointCount) { 0.0 }
        state.wobbleTargets = MutableList(config.pointCount) { 0.0 }
    }

    // Check if we need new random targets
    val timeSinceLastTarget = (frameTime - state.lastWobbleTargetUpdate) / 1_000_000_000.0
    if (timeSinceLastTarget > wobbleUpdateInterval || state.wobbleTargets.all { it == 0.0 }) {
        state.lastWobbleTargetUpdate = frameTime
        val intensity = state.wobbleIntensity * config.wobbleIntensity
        val maxOffset = 0.08 * intensity  // Max 8% radius variation at full intensity

        for (i in 0 until config.pointCount) {
            state.wobbleTargets[i] = Random.nextDouble(-maxOffset, maxOffset)
        }
    }

    // Interpolate offsets toward targets
    val interpolationSpeed = 4.0
    val dt = (frameTime - state.lastWobbleUpdate) / 1_000_000_000.0
    val factor = min(1.0, dt * interpolationSpeed)

    for (i in 0 until min(state.wobbleOffsets.size, state.wobbleTargets.size)) {
        state.wobbleOffsets[i] = state.wobbleOffsets[i] + (state.wobbleTargets[i] - state.wobbleOffsets[i]) * factor
    }
}
