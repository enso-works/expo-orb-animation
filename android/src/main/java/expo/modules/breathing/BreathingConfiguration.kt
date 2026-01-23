package expo.modules.breathing

import androidx.compose.ui.graphics.Color

data class BreathingConfiguration(
    val blobColors: List<Color> = listOf(
        Color(0xFF66B3E6),  // Light blue
        Color(0xFF4D80CC),  // Medium blue
        Color(0xFF804DB3)   // Purple
    ),
    val innerBlobColor: Color = Color.White.copy(alpha = 0.3f),
    val glowColor: Color = Color.White,
    val particleColor: Color = Color.White,
    val progressRingColor: Color = Color.White.copy(alpha = 0.5f),
    val textColor: Color = Color.White,
    val showProgressRing: Boolean = true,
    val showTextCue: Boolean = true,
    val showInnerBlob: Boolean = true,
    val showShadow: Boolean = true,
    val showParticles: Boolean = true,
    val showWavyBlobs: Boolean = true,
    val showGlowEffects: Boolean = true,
    val pointCount: Int = 8,
    val wobbleIntensity: Double = 1.0
)
