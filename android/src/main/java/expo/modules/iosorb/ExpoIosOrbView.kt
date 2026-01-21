package expo.modules.iosorb

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

class ExpoIosOrbView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {

    // Mutable state for configuration
    private var config by mutableStateOf(OrbConfiguration())

    private val composeView = ComposeView(context).apply {
        setContent {
            OrbViewContent()
        }
    }

    init {
        // Allow glow/shadow effects to render outside view bounds
        clipChildren = false
        clipToPadding = false
        composeView.clipChildren = false
        composeView.clipToPadding = false
        addView(composeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    @Composable
    private fun OrbViewContent() {
        // Add padding so glow/shadow can overflow outside the orb circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),  // Space for glow overflow
            contentAlignment = Alignment.Center
        ) {
            OrbView(
                config = config,
                useSharedActivityState = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    fun setBackgroundColors(colors: List<Int>) {
        config = config.copy(
            backgroundColors = colors.map { Color(it) }
        )
    }

    fun setGlowColor(color: Int) {
        config = config.copy(glowColor = Color(color))
    }

    fun setParticleColor(color: Int) {
        config = config.copy(particleColor = Color(color))
    }

    fun setCoreGlowIntensity(value: Double) {
        config = config.copy(coreGlowIntensity = value)
    }

    fun setBreathingIntensity(value: Double) {
        config = config.copy(breathingIntensity = value)
    }

    fun setBreathingSpeed(value: Double) {
        config = config.copy(breathingSpeed = value)
    }

    fun setShowBackground(value: Boolean) {
        config = config.copy(showBackground = value)
    }

    fun setShowWavyBlobs(value: Boolean) {
        config = config.copy(showWavyBlobs = value)
    }

    fun setShowParticles(value: Boolean) {
        config = config.copy(showParticles = value)
    }

    fun setShowGlowEffects(value: Boolean) {
        config = config.copy(showGlowEffects = value)
    }

    fun setShowShadow(value: Boolean) {
        config = config.copy(showShadow = value)
    }

    fun setSpeed(value: Double) {
        config = config.copy(speed = value)
    }
}
