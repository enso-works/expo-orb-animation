package expo.modules.orb

/**
 * Shared state for activity animation - mirrors iOS OrbSharedState.
 *
 * This singleton allows smooth animations when bridging React Native ↔ Compose.
 * JS sends target values, native interpolates during animation loop.
 */
object OrbSharedState {
    @Volatile
    var targetActivity: Double = 0.0  // Written by native bridge, read by animation loop

    @Volatile
    var currentActivity: Double = 0.0  // Interpolated value, updated each frame

    @Volatile
    var lastUpdateTime: Long = System.nanoTime()  // For frame-time-based interpolation

    // Cumulative phase for breathing - allows speed changes without jumps
    @Volatile
    var breathingPhase: Double = 0.0

    @Volatile
    var lastBreathingUpdate: Long = System.nanoTime()
}
