package expo.modules.breathing

import android.graphics.Color as AndroidColor
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoBreathingExerciseModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("ExpoBreathingExercise")

        Events("onPhaseChange", "onExerciseComplete")

        Function("startBreathingExercise") { pattern: Map<String, Any?> ->
            startExercise(pattern)
        }

        Function("stopBreathingExercise") {
            BreathingSharedState.reset()
        }

        Function("pauseBreathingExercise") {
            val state = BreathingSharedState
            if (state.state == BreathingExerciseState.RUNNING) {
                state.state = BreathingExerciseState.PAUSED
                state.pauseTime = System.nanoTime()
            }
        }

        Function("resumeBreathingExercise") {
            val state = BreathingSharedState
            val pauseTime = state.pauseTime
            if (state.state == BreathingExerciseState.PAUSED && pauseTime != null) {
                val pauseDuration = System.nanoTime() - pauseTime
                state.phaseStartTime += pauseDuration
                state.exerciseStartTime += pauseDuration
                state.state = BreathingExerciseState.RUNNING
                state.pauseTime = null
            }
        }

        View(ExpoBreathingExerciseView::class) {
            Events("onPhaseChange", "onExerciseComplete")

            Prop("blobColors") { view: ExpoBreathingExerciseView, colors: List<Any> ->
                val parsedColors = colors.map { parseColor(it) }
                view.setBlobColors(parsedColors)
            }

            Prop("innerBlobColor") { view: ExpoBreathingExerciseView, color: Any ->
                view.setInnerBlobColor(parseColor(color))
            }

            Prop("progressRingColor") { view: ExpoBreathingExerciseView, color: Any ->
                view.setProgressRingColor(parseColor(color))
            }

            Prop("textColor") { view: ExpoBreathingExerciseView, color: Any ->
                view.setTextColor(parseColor(color))
            }

            Prop("showProgressRing") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowProgressRing(value)
            }

            Prop("showTextCue") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowTextCue(value)
            }

            Prop("showInnerBlob") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowInnerBlob(value)
            }

            Prop("showShadow") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowShadow(value)
            }

            Prop("showParticles") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowParticles(value)
            }

            Prop("showWavyBlobs") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowWavyBlobs(value)
            }

            Prop("showGlowEffects") { view: ExpoBreathingExerciseView, value: Boolean ->
                view.setShowGlowEffects(value)
            }

            Prop("glowColor") { view: ExpoBreathingExerciseView, color: Any ->
                view.setGlowColor(parseColor(color))
            }

            Prop("particleColor") { view: ExpoBreathingExerciseView, color: Any ->
                view.setParticleColor(parseColor(color))
            }

            Prop("pointCount") { view: ExpoBreathingExerciseView, value: Int ->
                view.setPointCount(value)
            }

            Prop("wobbleIntensity") { view: ExpoBreathingExerciseView, value: Double ->
                view.setWobbleIntensity(value)
            }
        }
    }

    private fun startExercise(pattern: Map<String, Any?>) {
        val state = BreathingSharedState
        state.reset()

        // Parse phases
        @Suppress("UNCHECKED_CAST")
        val phasesArray = pattern["phases"] as? List<Map<String, Any?>> ?: return

        val phases = phasesArray.mapNotNull { phaseDict ->
            val phaseString = phaseDict["phase"] as? String ?: return@mapNotNull null
            val duration = (phaseDict["duration"] as? Number)?.toDouble() ?: return@mapNotNull null
            val targetScale = (phaseDict["targetScale"] as? Number)?.toDouble() ?: return@mapNotNull null
            val label = phaseDict["label"] as? String ?: return@mapNotNull null

            val phase = when (phaseString) {
                "inhale" -> BreathPhase.INHALE
                "holdIn" -> BreathPhase.HOLD_IN
                "exhale" -> BreathPhase.EXHALE
                "holdOut" -> BreathPhase.HOLD_OUT
                else -> return@mapNotNull null
            }

            BreathPhaseConfig(
                phase = phase,
                duration = duration / 1000.0,  // Convert ms to seconds
                targetScale = targetScale,
                label = label
            )
        }

        if (phases.isEmpty()) return

        // Parse cycles
        state.totalCycles = (pattern["cycles"] as? Number)?.toInt()

        // Setup state
        state.phases = phases
        state.currentPhaseIndex = 0
        state.currentCycle = 0
        state.phaseStartTime = System.nanoTime()
        state.exerciseStartTime = System.nanoTime()

        // Set initial phase values
        val firstPhase = phases[0]
        state.currentPhase = firstPhase.phase
        state.currentLabel = firstPhase.label
        state.startScale = 1.0
        state.targetScale = firstPhase.targetScale
        state.currentScale = 1.0
        state.phaseProgress = 0.0

        // Set initial wobble intensity
        state.wobbleIntensity = when (firstPhase.phase) {
            BreathPhase.INHALE, BreathPhase.EXHALE -> 1.0
            BreathPhase.HOLD_IN, BreathPhase.HOLD_OUT -> 0.3
            BreathPhase.IDLE -> 0.5
        }

        state.state = BreathingExerciseState.RUNNING
    }

    private fun parseColor(value: Any): Int {
        return when (value) {
            is String -> AndroidColor.parseColor(value)
            is Int -> value
            is Double -> value.toInt()
            is Long -> value.toInt()
            else -> AndroidColor.WHITE
        }
    }
}
