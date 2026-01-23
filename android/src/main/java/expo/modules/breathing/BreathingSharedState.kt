package expo.modules.breathing

enum class BreathPhase(val value: String) {
    INHALE("inhale"),
    HOLD_IN("holdIn"),
    EXHALE("exhale"),
    HOLD_OUT("holdOut"),
    IDLE("idle")
}

enum class BreathingExerciseState {
    STOPPED,
    RUNNING,
    PAUSED,
    COMPLETE
}

data class BreathPhaseConfig(
    val phase: BreathPhase,
    val duration: Double,  // seconds
    val targetScale: Double,
    val label: String
)

object BreathingSharedState {
    // Pattern configuration
    @Volatile
    var phases: List<BreathPhaseConfig> = emptyList()

    @Volatile
    var totalCycles: Int? = null  // null = infinite

    // Current state
    @Volatile
    var state: BreathingExerciseState = BreathingExerciseState.STOPPED

    @Volatile
    var currentPhaseIndex: Int = 0

    @Volatile
    var currentCycle: Int = 0

    @Volatile
    var phaseStartTime: Long = System.nanoTime()

    @Volatile
    var pauseTime: Long? = null

    // Animation values
    @Volatile
    var currentScale: Double = 1.0

    @Volatile
    var targetScale: Double = 1.0

    @Volatile
    var currentPhase: BreathPhase = BreathPhase.IDLE

    @Volatile
    var currentLabel: String = ""

    @Volatile
    var phaseProgress: Double = 0.0

    // Wobble animation
    @Volatile
    var wobbleIntensity: Double = 1.0

    @Volatile
    var wobbleOffsets: MutableList<Double> = mutableListOf()

    @Volatile
    var wobbleTargets: MutableList<Double> = mutableListOf()

    @Volatile
    var lastWobbleUpdate: Long = System.nanoTime()

    @Volatile
    var lastWobbleTargetUpdate: Long = System.nanoTime()

    // Exercise tracking
    @Volatile
    var exerciseStartTime: Long = System.nanoTime()

    @Volatile
    var totalDuration: Double = 0.0

    // Callbacks
    var onPhaseChange: ((BreathPhase, String, Int, Int) -> Unit)? = null
    var onExerciseComplete: ((Int, Double) -> Unit)? = null

    fun reset() {
        state = BreathingExerciseState.STOPPED
        currentPhaseIndex = 0
        currentCycle = 0
        currentScale = 1.0
        targetScale = 1.0
        currentPhase = BreathPhase.IDLE
        currentLabel = ""
        phaseProgress = 0.0
        wobbleIntensity = 1.0
        pauseTime = null
    }
}
