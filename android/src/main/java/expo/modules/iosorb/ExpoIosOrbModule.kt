package expo.modules.iosorb

import android.graphics.Color as AndroidColor
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoIosOrbModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("ExpoIosOrb")

        // Activity function - bypasses React's prop reconciliation
        Function("setActivity") { activity: Double ->
            val clampedActivity = activity.coerceIn(0.0, 1.0)
            OrbSharedState.targetActivity = clampedActivity
        }

        View(ExpoIosOrbView::class) {
            // Colors come as strings (hex) or integers from React Native
            Prop("backgroundColors") { view: ExpoIosOrbView, colors: List<Any> ->
                val parsedColors = colors.map { parseColor(it) }
                view.setBackgroundColors(parsedColors)
            }

            Prop("glowColor") { view: ExpoIosOrbView, color: Any ->
                view.setGlowColor(parseColor(color))
            }

            Prop("particleColor") { view: ExpoIosOrbView, color: Any ->
                view.setParticleColor(parseColor(color))
            }

            Prop("coreGlowIntensity") { view: ExpoIosOrbView, value: Double ->
                view.setCoreGlowIntensity(value)
            }

            Prop("breathingIntensity") { view: ExpoIosOrbView, value: Double ->
                view.setBreathingIntensity(value)
            }

            Prop("breathingSpeed") { view: ExpoIosOrbView, value: Double ->
                view.setBreathingSpeed(value)
            }

            Prop("showBackground") { view: ExpoIosOrbView, value: Boolean ->
                view.setShowBackground(value)
            }

            Prop("showWavyBlobs") { view: ExpoIosOrbView, value: Boolean ->
                view.setShowWavyBlobs(value)
            }

            Prop("showParticles") { view: ExpoIosOrbView, value: Boolean ->
                view.setShowParticles(value)
            }

            Prop("showGlowEffects") { view: ExpoIosOrbView, value: Boolean ->
                view.setShowGlowEffects(value)
            }

            Prop("showShadow") { view: ExpoIosOrbView, value: Boolean ->
                view.setShowShadow(value)
            }

            Prop("speed") { view: ExpoIosOrbView, value: Double ->
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
