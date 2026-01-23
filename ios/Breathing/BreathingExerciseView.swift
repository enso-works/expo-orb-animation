import SwiftUI

public struct BreathingExerciseView: View {
    private let config: BreathingConfiguration

    public init(configuration: BreathingConfiguration = BreathingConfiguration()) {
        self.config = configuration
    }

    public var body: some View {
        TimelineView(.animation) { timeline in
            GeometryReader { geometry in
                let size = min(geometry.size.width, geometry.size.height)
                let elapsedTime = timeline.date.timeIntervalSinceReferenceDate

                // Update all animation state
                let animState = updateAllAnimations(at: timeline.date, pointCount: config.pointCount)

                ZStack {
                    // Progress ring (behind blob)
                    if config.showProgressRing && animState.isActive {
                        ProgressRingView(
                            progress: animState.phaseProgress,
                            color: config.progressRingColor,
                            lineWidth: 4,
                            size: size * 0.95
                        )
                    }

                    // Main morphing blob with gradient
                    MorphingBlobView(
                        baseRadius: animState.scale * 0.7,
                        pointCount: config.pointCount,
                        offsets: animState.wobbleOffsets,
                        colors: config.blobColors,
                        innerColor: config.innerBlobColor,
                        showInnerBlob: config.showInnerBlob,
                        elapsedTime: elapsedTime
                    )
                    .frame(width: size, height: size)

                    // Text cue
                    if config.showTextCue && !animState.label.isEmpty {
                        BreathingTextCue(
                            text: animState.label,
                            color: config.textColor
                        )
                    }
                }
                .frame(width: size, height: size)
                .modifier(
                    BreathingShadowModifier(
                        colors: config.showShadow ? config.blobColors : [.clear],
                        radius: size * 0.06,
                        scale: animState.scale
                    )
                )
            }
            .aspectRatio(1, contentMode: .fit)
        }
    }

    private struct AnimState {
        let scale: Double
        let wobbleOffsets: [Double]
        let phaseProgress: Double
        let label: String
        let isActive: Bool
    }

    private func updateAllAnimations(at date: Date, pointCount: Int) -> AnimState {
        let state = BreathingSharedState.shared

        // Always update wobble animation (organic movement even when idle)
        updateWobble(at: date, pointCount: pointCount)

        // Handle breathing state
        switch state.state {
        case .stopped, .complete:
            return AnimState(
                scale: 1.0,
                wobbleOffsets: state.wobbleOffsets,
                phaseProgress: 0,
                label: "",
                isActive: false
            )

        case .paused:
            return AnimState(
                scale: state.currentScale,
                wobbleOffsets: state.wobbleOffsets,
                phaseProgress: state.phaseProgress,
                label: state.currentLabel,
                isActive: true
            )

        case .running:
            // Update phase timing
            updatePhaseState(at: date)

            // Calculate scale and progress based on phase
            let currentPhaseConfig = state.phases.isEmpty ? nil : state.phases[state.currentPhaseIndex]
            let scale: Double
            let ringProgress: Double

            if let phase = currentPhaseConfig?.phase {
                switch phase {
                case .inhale:
                    // Ease-out cubic for smooth deceleration toward target
                    let easedProgress = easeOutCubic(state.phaseProgress)
                    scale = state.startScale + (state.targetScale - state.startScale) * easedProgress
                    ringProgress = easedProgress  // 0 -> 1
                case .exhale:
                    let easedProgress = easeOutCubic(state.phaseProgress)
                    scale = state.startScale + (state.targetScale - state.startScale) * easedProgress
                    ringProgress = 1.0 - easedProgress  // 1 -> 0
                case .holdIn:
                    // Hold at expanded scale, ring stays full
                    scale = state.targetScale
                    ringProgress = 1.0
                case .holdOut:
                    // Hold at contracted scale, ring stays empty
                    scale = state.targetScale
                    ringProgress = 0.0
                case .idle:
                    scale = state.targetScale
                    ringProgress = 0.0
                }
            } else {
                scale = 1.0
                ringProgress = 0.0
            }

            state.currentScale = scale

            return AnimState(
                scale: scale,
                wobbleOffsets: state.wobbleOffsets,
                phaseProgress: ringProgress,
                label: state.currentLabel,
                isActive: true
            )
        }
    }

    private func updatePhaseState(at date: Date) {
        let state = BreathingSharedState.shared
        guard !state.phases.isEmpty else { return }

        let currentPhaseConfig = state.phases[state.currentPhaseIndex]
        let elapsed = date.timeIntervalSince(state.phaseStartTime)
        let duration = currentPhaseConfig.duration

        // Calculate progress through current phase
        state.phaseProgress = min(1.0, elapsed / duration)

        // Check if phase is complete
        if elapsed >= duration {
            // Move to next phase
            state.currentPhaseIndex = (state.currentPhaseIndex + 1) % state.phases.count

            // Check if we completed a cycle
            if state.currentPhaseIndex == 0 {
                state.currentCycle += 1

                // Check if exercise is complete
                if let totalCycles = state.totalCycles, state.currentCycle >= totalCycles {
                    state.state = .complete
                    state.totalDuration = date.timeIntervalSince(state.exerciseStartTime)
                    state.onExerciseComplete?(state.currentCycle, state.totalDuration)
                    return
                }
            }

            // Start new phase
            state.phaseStartTime = date
            let newPhaseConfig = state.phases[state.currentPhaseIndex]
            state.currentPhase = newPhaseConfig.phase
            state.currentLabel = newPhaseConfig.label
            state.startScale = state.currentScale  // Remember where we're starting from
            state.targetScale = newPhaseConfig.targetScale
            state.phaseProgress = 0

            // Update wobble intensity based on phase
            switch newPhaseConfig.phase {
            case .inhale, .exhale:
                state.wobbleIntensity = 1.0
            case .holdIn, .holdOut:
                state.wobbleIntensity = 0.3
            case .idle:
                state.wobbleIntensity = 0.5
            }

            // Fire phase change callback
            state.onPhaseChange?(
                newPhaseConfig.phase,
                newPhaseConfig.label,
                state.currentPhaseIndex,
                state.currentCycle
            )
        }
    }

    private func updateWobble(at date: Date, pointCount: Int) {
        let state = BreathingSharedState.shared

        // Initialize arrays if needed
        if state.wobbleOffsets.count != pointCount {
            state.wobbleOffsets = (0..<pointCount).map { _ in 0.0 }
            state.wobbleTargets = (0..<pointCount).map { _ in 0.0 }
            state.lastWobbleUpdate = date
            state.lastWobbleTargetUpdate = date
        }

        let now = date
        let dt = now.timeIntervalSince(state.lastWobbleUpdate)
        state.lastWobbleUpdate = now

        // Generate new random targets periodically (slower, more organic)
        let timeSinceTargetUpdate = now.timeIntervalSince(state.lastWobbleTargetUpdate)
        if timeSinceTargetUpdate > 1.8 {
            state.lastWobbleTargetUpdate = now

            let intensity = state.wobbleIntensity * config.wobbleIntensity
            let maxOffset = 0.18 * intensity

            state.wobbleTargets = (0..<pointCount).map { i in
                let phase = Double(i) / Double(pointCount) * 2.0 * .pi
                let wave1 = sin(phase * 2 + Double.random(in: 0...1)) * 0.6
                let wave2 = cos(phase * 3 + Double.random(in: 0...1)) * 0.4
                let baseOffset = Double.random(in: -maxOffset...maxOffset)
                return baseOffset * (1.0 + wave1 + wave2) * 0.5
            }
        }

        // Smoothly interpolate current offsets toward targets
        let interpolationSpeed = 1.8
        let factor = min(1.0, dt * interpolationSpeed)

        for i in 0..<pointCount {
            let current = state.wobbleOffsets[i]
            let target = state.wobbleTargets[i]
            state.wobbleOffsets[i] = current + (target - current) * factor
        }
    }
}

struct BreathingShadowModifier: ViewModifier {
    let colors: [Color]
    let radius: CGFloat
    let scale: Double

    func body(content: Content) -> some View {
        content
            .shadow(color: colors.first?.opacity(0.4) ?? .clear, radius: radius * scale, x: 0, y: radius * 0.3 * scale)
            .shadow(color: colors.first?.opacity(0.2) ?? .clear, radius: radius * 2 * scale, x: 0, y: radius * 0.5 * scale)
    }
}

// Easing functions
private func easeOutCubic(_ t: Double) -> Double {
    let t1 = t - 1
    return t1 * t1 * t1 + 1
}

private func easeInOutCubic(_ t: Double) -> Double {
    if t < 0.5 {
        return 4 * t * t * t
    } else {
        let t1 = -2 * t + 2
        return 1 - (t1 * t1 * t1) / 2
    }
}
