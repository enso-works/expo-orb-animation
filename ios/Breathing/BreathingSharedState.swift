import Foundation

public enum BreathPhase: String {
    case inhale
    case holdIn
    case exhale
    case holdOut
    case idle
}

public enum BreathingExerciseState {
    case stopped
    case running
    case paused
    case complete
}

public final class BreathingSharedState {
    public static let shared = BreathingSharedState()

    // Pattern configuration
    var phases: [BreathPhaseConfig] = []
    var totalCycles: Int? = nil  // nil = infinite

    // Current state
    var state: BreathingExerciseState = .stopped
    var currentPhaseIndex: Int = 0
    var currentCycle: Int = 0
    var phaseStartTime: Date = Date()
    var pauseTime: Date? = nil

    // Animation values (read by TimelineView each frame)
    var currentScale: Double = 1.0
    var targetScale: Double = 1.0
    var startScale: Double = 1.0  // Scale at start of current phase
    var lastScaleUpdate: Date = Date()
    var currentPhase: BreathPhase = .idle
    var currentLabel: String = ""
    var phaseProgress: Double = 0.0  // 0-1 progress through current phase

    // Wobble animation
    var wobbleIntensity: Double = 1.0
    var wobbleOffsets: [Double] = []
    var wobbleTargets: [Double] = []
    var lastWobbleUpdate: Date = Date()
    var lastWobbleTargetUpdate: Date = Date()

    // Exercise tracking
    var exerciseStartTime: Date = Date()
    var totalDuration: Double = 0.0

    // Callbacks
    var onPhaseChange: ((BreathPhase, String, Int, Int) -> Void)? = nil
    var onExerciseComplete: ((Int, Double) -> Void)? = nil

    private init() {}

    func reset() {
        state = .stopped
        currentPhaseIndex = 0
        currentCycle = 0
        currentScale = 1.0
        targetScale = 1.0
        currentPhase = .idle
        currentLabel = ""
        phaseProgress = 0.0
        wobbleIntensity = 1.0
        pauseTime = nil
    }
}

public struct BreathPhaseConfig {
    let phase: BreathPhase
    let duration: Double  // seconds
    let targetScale: Double
    let label: String

    init(phase: BreathPhase, duration: Double, targetScale: Double, label: String) {
        self.phase = phase
        self.duration = duration
        self.targetScale = targetScale
        self.label = label
    }
}
