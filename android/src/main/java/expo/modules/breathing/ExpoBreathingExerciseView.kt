package expo.modules.breathing

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView

class ExpoBreathingExerciseView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {

    // Mutable state for configuration
    private var config by mutableStateOf(BreathingConfiguration())

    private val onPhaseChange by EventDispatcher()
    private val onExerciseComplete by EventDispatcher()

    private val composeView = ComposeView(context).apply {
        setContent {
            BreathingExerciseViewContent()
        }
    }

    init {
        // Allow effects to render outside view bounds
        clipChildren = false
        clipToPadding = false
        composeView.clipChildren = false
        composeView.clipToPadding = false
        addView(composeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // Setup callbacks
        setupCallbacks()
    }

    private fun setupCallbacks() {
        BreathingSharedState.onPhaseChange = { phase, label, phaseIndex, cycle ->
            onPhaseChange(
                mapOf(
                    "phase" to phase.value,
                    "label" to label,
                    "phaseIndex" to phaseIndex,
                    "cycle" to cycle
                )
            )
        }

        BreathingSharedState.onExerciseComplete = { totalCycles, totalDuration ->
            onExerciseComplete(
                mapOf(
                    "totalCycles" to totalCycles,
                    "totalDuration" to totalDuration
                )
            )
        }
    }

    @Composable
    private fun BreathingExerciseViewContent() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            BreathingExerciseView(
                config = config,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    fun setBlobColors(colors: List<Int>) {
        config = config.copy(
            blobColors = colors.map { Color(it) }
        )
    }

    fun setInnerBlobColor(color: Int) {
        config = config.copy(innerBlobColor = Color(color))
    }

    fun setProgressRingColor(color: Int) {
        config = config.copy(progressRingColor = Color(color))
    }

    fun setTextColor(color: Int) {
        config = config.copy(textColor = Color(color))
    }

    fun setShowProgressRing(value: Boolean) {
        config = config.copy(showProgressRing = value)
    }

    fun setShowTextCue(value: Boolean) {
        config = config.copy(showTextCue = value)
    }

    fun setShowInnerBlob(value: Boolean) {
        config = config.copy(showInnerBlob = value)
    }

    fun setShowShadow(value: Boolean) {
        config = config.copy(showShadow = value)
    }

    fun setPointCount(value: Int) {
        config = config.copy(pointCount = maxOf(6, value))
    }

    fun setWobbleIntensity(value: Double) {
        config = config.copy(wobbleIntensity = value.coerceIn(0.0, 1.0))
    }
}
