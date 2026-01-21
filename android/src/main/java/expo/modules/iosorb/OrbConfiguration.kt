package expo.modules.iosorb

import androidx.compose.ui.graphics.Color

data class OrbConfiguration(
    val backgroundColors: List<Color> = listOf(Color.Green, Color.Blue, Color.Magenta),
    val glowColor: Color = Color.White,
    val particleColor: Color = Color.White,
    val coreGlowIntensity: Double = 1.0,
    val showBackground: Boolean = true,
    val showWavyBlobs: Boolean = true,
    val showParticles: Boolean = true,
    val showGlowEffects: Boolean = true,
    val showShadow: Boolean = true,
    val speed: Double = 60.0,
    val breathingIntensity: Double = 0.0,
    val breathingSpeed: Double = 0.25
)

/**
 * Effective configuration computed from activity level.
 * All speeds must stay CONSTANT to avoid animation jumps.
 */
data class EffectiveConfig(
    val speed: Double,
    val breathingIntensity: Double,
    val breathingSpeed: Double,
    val coreGlowIntensity: Double,
    val glowColor: Color
)
