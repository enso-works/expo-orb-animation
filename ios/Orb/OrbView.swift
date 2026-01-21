//
//  OrbView.swift
//  Prototype-Orb
//
//  Created by Siddhant Mehta on 2024-11-06.
//
import Foundation
import SwiftUI

// ARCHITECTURE NOTE: Shared state for activity animation
// ========================================================
// This class is the key to smooth animations when bridging React Native ↔ SwiftUI.
//
// THE PROBLEM:
// When JS updates props frequently (e.g., activity level), each update would normally:
// 1. Cross the JS-to-native bridge
// 2. Update an @Published property in SwiftUI
// 3. Trigger SwiftUI view re-renders
// 4. Interfere with ongoing TimelineView animations → causes flickering/shaking
//
// THE SOLUTION:
// 1. JS sends target values to native (can be frequent, e.g., every 100-800ms)
// 2. Native writes to this PLAIN (non-reactive) shared state
// 3. SwiftUI's TimelineView reads from shared state during its animation loop
// 4. Interpolation happens inside TimelineView - no external state triggers re-renders
//
// RULES FOR SMOOTH ANIMATION:
// - NEVER use @Published for values that change during animation
// - NEVER pass animated values as View parameters that change frequently
// - DO use shared state + TimelineView for all animated properties
// - DO interpolate inside TimelineView body, not in response to prop changes
//
public final class OrbSharedState {
    public static let shared = OrbSharedState()
    public var targetActivity: Double = 0  // Written by native bridge, read by animation loop
    var currentActivity: Double = 0        // Interpolated value, updated each frame
    var lastUpdateTime: Date = Date()      // For frame-time-based interpolation

    // Cumulative phase for breathing - allows speed changes without jumps
    var breathingPhase: Double = 0
    var lastBreathingUpdate: Date = Date()

    // DEBUG: Set to true to test native-only activity generation (no JS involvement)
    public var useNativeDemoMode: Bool = false
    var lastDemoUpdate: Date = Date()
}

public struct OrbView: View {
    private let config: OrbConfiguration
    private let useSharedActivityState: Bool

    public init(configuration: OrbConfiguration = OrbConfiguration(), targetActivity: Double? = nil, useSharedActivityState: Bool = false) {
        self.config = configuration
        self.useSharedActivityState = useSharedActivityState || (targetActivity != nil)
        // If targetActivity is passed directly, set it to shared state
        if let activity = targetActivity {
            OrbSharedState.shared.targetActivity = activity
        }
    }

    public var body: some View {
        TimelineView(.animation) { timeline in
            let elapsedTime = timeline.date.timeIntervalSinceReferenceDate

            GeometryReader { geometry in
                let size = min(geometry.size.width, geometry.size.height)

                // Interpolate activity in the same animation loop
                let activity = interpolatedActivity(at: timeline.date)
                let effectiveConfig = activityDerivedConfig(activity: activity)
                let scale = breathingScale(at: timeline.date, config: effectiveConfig)

                ZStack {
                    // Base gradient background layer
                    if config.showBackground {
                        background
                    }

                    // Creates depth with rotating glow effects
                    baseDepthGlows(size: size, config: effectiveConfig, elapsedTime: elapsedTime)

                    // Adds organic movement with flowing blob shapes
                    if config.showWavyBlobs {
                        wavyBlob(elapsedTime: elapsedTime)
                        wavyBlobTwo(elapsedTime: elapsedTime)
                    }

                    // Adds bright, energetic core glow animations
                    if config.showGlowEffects {
                        coreGlowEffects(size: size, config: effectiveConfig, elapsedTime: elapsedTime)
                    }

                    // Overlays floating particle effects for additional dynamism
                    if config.showParticles {
                        particleView
                            .frame(maxWidth: size, maxHeight: size)
                    }
                }
                // Orb outline for depth
                .overlay {
                    realisticInnerGlows
                }
                // Masking out all the effects so it forms a perfect circle
                .mask {
                    Circle()
                }
                .aspectRatio(1, contentMode: .fit)
                // Adding realistic, layered shadows so its brighter near the core, and softer as it grows outwards
                .modifier(
                    RealisticShadowModifier(
                        colors: config.showShadow ? config.backgroundColors : [.clear],
                        radius: size * 0.08
                    )
                )
                .scaleEffect(scale)
            }
        }
    }

    // Interpolate activity toward target within the TimelineView animation loop
    private func interpolatedActivity(at date: Date) -> Double {
        guard useSharedActivityState else {
            return 0
        }

        let state = OrbSharedState.shared

        // DEBUG: Native demo mode - generate activity without JS
        if state.useNativeDemoMode {
            // Update target every ~800ms
            if date.timeIntervalSince(state.lastDemoUpdate) > 0.8 {
                state.lastDemoUpdate = date
                let speaking = Double.random(in: 0...1) > 0.4
                if speaking {
                    state.targetActivity = 0.4 + Double.random(in: 0...0.6)
                } else {
                    state.targetActivity = Double.random(in: 0...0.15)
                }
            }
        }

        let target = state.targetActivity
        let dt = date.timeIntervalSince(state.lastUpdateTime)
        state.lastUpdateTime = date

        // Smooth interpolation factor (adjusted for frame time)
        // Higher factor = faster response to activity changes
        let factor = min(1.0, dt * 6.0) // ~6.0 per second convergence rate
        let next = state.currentActivity + (target - state.currentActivity) * factor
        state.currentActivity = next

        return next
    }

    // Compute derived config values from interpolated activity
    private func activityDerivedConfig(activity: Double) -> EffectiveConfig {
        if useSharedActivityState {
            // Activity mode: derive values from interpolated activity
            // IMPORTANT: Rotation speed must stay CONSTANT - changing it causes massive jumps
            // because phase = elapsedTime * speed, and elapsedTime is ~788 million seconds
            let speed = 18.0  // Fixed rotation speed
            // Breathing speed CAN vary because we use cumulative phase (see breathingScale)
            // Faster when speaking for punchy effect
            let breathingSpeed = 0.03 + activity * 0.25  // 0.03 idle (almost none) → 0.28 speaking (punchy)
            // Idle: no breathing, Speaking: full breathing
            let breathingIntensity = max(0, (activity - 0.2)) * 1.25  // Kicks in above 0.2 activity
            // Idle: barely visible glow, Speaking: bright
            let coreGlowIntensity = 0.08 + activity * 1.8  // 0.08 idle → 1.88 speaking
            return EffectiveConfig(
                speed: speed,
                breathingIntensity: breathingIntensity,
                breathingSpeed: breathingSpeed,
                coreGlowIntensity: coreGlowIntensity,
                glowColor: config.glowColor
            )
        } else {
            // Static mode: use config values directly
            return EffectiveConfig(
                speed: config.speed,
                breathingIntensity: config.breathingIntensity,
                breathingSpeed: config.breathingSpeed,
                coreGlowIntensity: config.coreGlowIntensity,
                glowColor: config.glowColor
            )
        }
    }

    private struct EffectiveConfig {
        let speed: Double
        let breathingIntensity: Double
        let breathingSpeed: Double
        let coreGlowIntensity: Double
        let glowColor: Color
    }

    private var background: some View {
        LinearGradient(colors: config.backgroundColors,
                       startPoint: .bottom,
                       endPoint: .top)
    }

    private var orbOutlineColor: LinearGradient {
        LinearGradient(colors: [.white, .clear],
                       startPoint: .bottom,
                       endPoint: .top)
    }
    
    private var particleView: some View {
        // Added multiple particle effects since the blurring amounts are different
        ZStack {
            ParticlesView(
                color: config.particleColor,
                speedRange: 10...20,
                sizeRange: 0.5...1,
                particleCount: 10,
                opacityRange: 0...0.3
            )
            .blur(radius: 1)
            
            ParticlesView(
                color: config.particleColor,
                speedRange: 20...30,
                sizeRange: 0.2...1,
                particleCount: 10,
                opacityRange: 0.3...0.8
            )
        }
        .blendMode(.plusLighter)
    }

    private func wavyBlob(elapsedTime: Double) -> some View {
        GeometryReader { geometry in
            let size = min(geometry.size.width, geometry.size.height)
            // Fixed calm speed for stable blob animation
            let blobSpeed: Double = 20

            RotatingGlowView(color: .white.opacity(0.75),
                           rotationSpeed: blobSpeed * 1.5,
                           direction: .clockwise,
                           elapsedTime: elapsedTime)
                .mask {
                    WavyBlobView(color: .white, loopDuration: 60 / blobSpeed * 1.75)
                        .frame(maxWidth: size * 1.875)
                        .offset(x: 0, y: size * 0.31)
                }
                .blur(radius: 1)
                .blendMode(.plusLighter)
        }
    }

    private func wavyBlobTwo(elapsedTime: Double) -> some View {
        GeometryReader { geometry in
            let size = min(geometry.size.width, geometry.size.height)
            // Fixed calm speed for stable blob animation
            let blobSpeed: Double = 20

            RotatingGlowView(color: .white,
                           rotationSpeed: blobSpeed * 0.75,
                           direction: .counterClockwise,
                           elapsedTime: elapsedTime)
                .mask {
                    WavyBlobView(color: .white, loopDuration: 60 / blobSpeed * 2.25)
                        .frame(maxWidth: size * 1.25)
                        .rotationEffect(.degrees(90))
                        .offset(x: 0, y: size * -0.31)
                }
                .opacity(0.5)
                .blur(radius: 1)
                .blendMode(.plusLighter)
        }
    }

    private func coreGlowEffects(size: CGFloat, config effectiveConfig: EffectiveConfig, elapsedTime: Double) -> some View {
        ZStack {
            // Slower rotation multipliers for more organic, less frantic movement
            RotatingGlowView(color: effectiveConfig.glowColor,
                          rotationSpeed: effectiveConfig.speed * 1.2,
                          direction: .clockwise,
                          elapsedTime: elapsedTime)
                .blur(radius: size * 0.08)
                .opacity(effectiveConfig.coreGlowIntensity)

            RotatingGlowView(color: effectiveConfig.glowColor,
                          rotationSpeed: effectiveConfig.speed * 0.9,
                          direction: .clockwise,
                          elapsedTime: elapsedTime)
                .blur(radius: size * 0.06)
                .opacity(effectiveConfig.coreGlowIntensity)
                .blendMode(.plusLighter)
        }
        .padding(size * 0.08)
    }

    // New combined function replacing outerGlow and outerRing
    private func baseDepthGlows(size: CGFloat, config effectiveConfig: EffectiveConfig, elapsedTime: Double) -> some View {
        ZStack {
            // Outer glow (previously outerGlow function)
            RotatingGlowView(color: effectiveConfig.glowColor,
                          rotationSpeed: effectiveConfig.speed * 0.75,
                          direction: .counterClockwise,
                          elapsedTime: elapsedTime)
                .padding(size * 0.03)
                .blur(radius: size * 0.06)
                .rotationEffect(.degrees(180))
                .blendMode(.destinationOver)

            // Outer ring (previously outerRing function)
            RotatingGlowView(color: effectiveConfig.glowColor.opacity(0.5),
                          rotationSpeed: effectiveConfig.speed * 0.25,
                          direction: .clockwise,
                          elapsedTime: elapsedTime)
                .frame(maxWidth: size * 0.94)
                .rotationEffect(.degrees(180))
                .padding(8)
                .blur(radius: size * 0.032)
        }
    }

    private var realisticInnerGlows: some View {
        ZStack {
            // Outer stroke with heavy blur
            Circle()
                .stroke(orbOutlineColor, lineWidth: 8)
                .blur(radius: 32)
                .blendMode(.plusLighter)

            // Inner stroke with light blur
            Circle()
                .stroke(orbOutlineColor, lineWidth: 4)
                .blur(radius: 12)
                .blendMode(.plusLighter)
            
            Circle()
                .stroke(orbOutlineColor, lineWidth: 1)
                .blur(radius: 4)
                .blendMode(.plusLighter)
        }
        .padding(1)
    }

    private func breathingScale(at date: Date, config effectiveConfig: EffectiveConfig) -> CGFloat {
        let intensity = max(0, min(1, effectiveConfig.breathingIntensity))
        if intensity == 0 {
            return 1
        }

        // Use CUMULATIVE phase - this allows speed to change without jumps
        // Each frame we add (deltaTime * speed) to the phase, rather than (absoluteTime * speed)
        let state = OrbSharedState.shared
        let dt = date.timeIntervalSince(state.lastBreathingUpdate)
        state.lastBreathingUpdate = date

        let speed = max(0.01, effectiveConfig.breathingSpeed)
        state.breathingPhase += dt * speed * 2 * .pi

        // Punchy wave shape - sharper peaks to mimic speech rhythm
        let rawWave = sin(state.breathingPhase)
        // Apply power curve: sqrt for positive (sharp attack), squared for negative (slower return)
        let wave = rawWave >= 0
            ? pow(rawWave, 0.6)      // Sharp attack (expand fast)
            : -pow(abs(rawWave), 1.4) // Slower return (contract slower)

        // Amplitude ~0.17 at full intensity = 1/3 total range (0.83 to 1.17)
        let amplitude = CGFloat(intensity) * 0.17
        return 1 + amplitude * CGFloat(wave)  // Goes below AND above 1.0
    }
}
