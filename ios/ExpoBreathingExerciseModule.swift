import ExpoModulesCore
import UIKit

public class ExpoBreathingExerciseModule: Module {
    public func definition() -> ModuleDefinition {
        Name("ExpoBreathingExercise")

        Events("onPhaseChange", "onExerciseComplete")

        Function("startBreathingExercise") { (pattern: [String: Any]) in
            DispatchQueue.main.async {
                self.startExercise(pattern: pattern)
            }
        }

        Function("stopBreathingExercise") {
            DispatchQueue.main.async {
                BreathingSharedState.shared.reset()
            }
        }

        Function("pauseBreathingExercise") {
            DispatchQueue.main.async {
                let state = BreathingSharedState.shared
                if state.state == .running {
                    state.state = .paused
                    state.pauseTime = Date()
                }
            }
        }

        Function("resumeBreathingExercise") {
            DispatchQueue.main.async {
                let state = BreathingSharedState.shared
                if state.state == .paused, let pauseTime = state.pauseTime {
                    let pauseDuration = Date().timeIntervalSince(pauseTime)
                    state.phaseStartTime = state.phaseStartTime.addingTimeInterval(pauseDuration)
                    state.exerciseStartTime = state.exerciseStartTime.addingTimeInterval(pauseDuration)
                    state.state = .running
                    state.pauseTime = nil
                }
            }
        }

        View(ExpoBreathingExerciseView.self) {
            Events("onPhaseChange", "onExerciseComplete")

            Prop("blobColors") { (view: ExpoBreathingExerciseView, colors: [UIColor]) in
                view.updateProps { $0.blobColors = colors }
            }

            Prop("innerBlobColor") { (view: ExpoBreathingExerciseView, color: UIColor) in
                view.updateProps { $0.innerBlobColor = color }
            }

            Prop("glowColor") { (view: ExpoBreathingExerciseView, color: UIColor) in
                view.updateProps { $0.glowColor = color }
            }

            Prop("particleColor") { (view: ExpoBreathingExerciseView, color: UIColor) in
                view.updateProps { $0.particleColor = color }
            }

            Prop("progressRingColor") { (view: ExpoBreathingExerciseView, color: UIColor) in
                view.updateProps { $0.progressRingColor = color }
            }

            Prop("textColor") { (view: ExpoBreathingExerciseView, color: UIColor) in
                view.updateProps { $0.textColor = color }
            }

            Prop("showProgressRing") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showProgressRing = value }
            }

            Prop("showTextCue") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showTextCue = value }
            }

            Prop("showInnerBlob") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showInnerBlob = value }
            }

            Prop("showShadow") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showShadow = value }
            }

            Prop("showParticles") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showParticles = value }
            }

            Prop("showWavyBlobs") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showWavyBlobs = value }
            }

            Prop("showGlowEffects") { (view: ExpoBreathingExerciseView, value: Bool) in
                view.updateProps { $0.showGlowEffects = value }
            }

            Prop("pointCount") { (view: ExpoBreathingExerciseView, value: Int) in
                view.updateProps { $0.pointCount = value }
            }

            Prop("wobbleIntensity") { (view: ExpoBreathingExerciseView, value: Double) in
                view.updateProps { $0.wobbleIntensity = value }
            }
        }
    }

    private func startExercise(pattern: [String: Any]) {
        let state = BreathingSharedState.shared
        state.reset()

        // Parse phases
        guard let phasesArray = pattern["phases"] as? [[String: Any]] else {
            return
        }

        var phases: [BreathPhaseConfig] = []
        for phaseDict in phasesArray {
            // NSNumber casts: bridged JS numbers can arrive as Int or Double
            // depending on their value; a direct `as? Double`/`as? Int` on the
            // wrong underlying type silently fails.
            guard let phaseString = phaseDict["phase"] as? String,
                  let duration = (phaseDict["duration"] as? NSNumber)?.doubleValue,
                  let targetScale = (phaseDict["targetScale"] as? NSNumber)?.doubleValue,
                  let label = phaseDict["label"] as? String else {
                continue
            }

            let phase: BreathPhase
            switch phaseString {
            case "inhale": phase = .inhale
            case "holdIn": phase = .holdIn
            case "exhale": phase = .exhale
            case "holdOut": phase = .holdOut
            default: continue
            }

            phases.append(BreathPhaseConfig(
                phase: phase,
                duration: duration / 1000.0,  // Convert ms to seconds
                targetScale: targetScale,
                label: label
            ))
        }

        guard !phases.isEmpty else { return }

        // Parse cycles. NSNumber cast, NOT `as? Int`: the bridge delivers JS
        // numbers as Double, and `Double as? Int` fails in Swift — which made
        // totalCycles nil (= infinite) and exercises never complete.
        state.totalCycles = (pattern["cycles"] as? NSNumber)?.intValue

        // Setup state
        state.phases = phases
        state.currentPhaseIndex = 0
        state.currentCycle = 0
        // Optional absolute anchor (epoch ms) so the caller can drive other
        // consumers - voice cues, a Live Activity - from the very same
        // schedule; without it the exercise starts now.
        let startTime: Date
        if let ms = (pattern["startTime"] as? NSNumber)?.doubleValue, ms > 0 {
            startTime = Date(timeIntervalSince1970: ms / 1000.0)
        } else {
            startTime = Date()
        }
        state.phaseStartTime = startTime
        state.exerciseStartTime = startTime

        // Set initial phase values
        let firstPhase = phases[0]
        state.currentPhase = firstPhase.phase
        state.currentLabel = firstPhase.label
        state.startScale = 1.0    // Start scale for first phase
        state.targetScale = firstPhase.targetScale
        state.currentScale = 1.0  // Start at base scale
        state.phaseProgress = 0

        // Set initial wobble intensity
        switch firstPhase.phase {
        case .inhale, .exhale:
            state.wobbleIntensity = 1.0
        case .holdIn, .holdOut:
            state.wobbleIntensity = 0.3
        case .idle:
            state.wobbleIntensity = 0.5
        }

        state.state = .running
    }
}
