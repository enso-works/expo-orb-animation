package expo.modules.orb

import android.graphics.Color as AndroidColor
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoOrbModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("ExpoOrb")

        // Activity function - bypasses React's prop reconciliation
        Function("setActivity") { activity: Double ->
            val clampedActivity = activity.coerceIn(0.0, 1.0)
            OrbSharedState.targetActivity = clampedActivity
        }

        View(ExpoOrbView::class) {
            // Colors come as strings (hex) or integers from React Native
            Prop("backgroundColors") { view: ExpoOrbView, colors: List<Any> ->
                val parsedColors = colors.map { parseColor(it) }
                view.setBackgroundColors(parsedColors)
            }

            Prop("glowColor") { view: ExpoOrbView, color: Any ->
                view.setGlowColor(parseColor(color))
            }

            Prop("particleColor") { view: ExpoOrbView, color: Any ->
                view.setParticleColor(parseColor(color))
            }

            Prop("coreGlowIntensity") { view: ExpoOrbView, value: Double ->
                view.setCoreGlowIntensity(value)
            }

            Prop("breathingIntensity") { view: ExpoOrbView, value: Double ->
                view.setBreathingIntensity(value)
            }

            Prop("breathingSpeed") { view: ExpoOrbView, value: Double ->
                view.setBreathingSpeed(value)
            }

            Prop("showBackground") { view: ExpoOrbView, value: Boolean ->
                view.setShowBackground(value)
            }

            Prop("showWavyBlobs") { view: ExpoOrbView, value: Boolean ->
                view.setShowWavyBlobs(value)
            }

            Prop("showParticles") { view: ExpoOrbView, value: Boolean ->
                view.setShowParticles(value)
            }

            Prop("showGlowEffects") { view: ExpoOrbView, value: Boolean ->
                view.setShowGlowEffects(value)
            }

            Prop("showShadow") { view: ExpoOrbView, value: Boolean ->
                view.setShowShadow(value)
            }

            Prop("speed") { view: ExpoOrbView, value: Double ->
                view.setSpeed(value)
            }
        }
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
