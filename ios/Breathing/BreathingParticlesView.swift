import SwiftUI

/// Pure SwiftUI particle system - no SpriteKit, no console warnings
struct BreathingParticlesView: View {
    let color: Color
    let particleCount: Int
    let speedRange: ClosedRange<Double>
    let sizeRange: ClosedRange<CGFloat>
    let opacityRange: ClosedRange<Double>

    init(
        color: Color = .white,
        particleCount: Int = 20,
        speedRange: ClosedRange<Double> = 10...30,
        sizeRange: ClosedRange<CGFloat> = 0.5...2.0,
        opacityRange: ClosedRange<Double> = 0.1...0.8
    ) {
        self.color = color
        self.particleCount = particleCount
        self.speedRange = speedRange
        self.sizeRange = sizeRange
        self.opacityRange = opacityRange
    }

    var body: some View {
        TimelineView(.animation) { timeline in
            Canvas { context, size in
                let time = timeline.date.timeIntervalSinceReferenceDate

                for i in 0..<particleCount {
                    // Use deterministic seed based on particle index
                    let seed = Double(i) * 1.618033988749895 // Golden ratio for good distribution

                    // Particle properties derived from seed
                    let baseX = fract(seed * 0.7)
                    let _ = fract(seed * 1.3) // Reserved for future horizontal variation
                    let particleSpeed = speedRange.lowerBound + fract(seed * 2.1) * (speedRange.upperBound - speedRange.lowerBound)
                    let particleSize = sizeRange.lowerBound + CGFloat(fract(seed * 3.7)) * (sizeRange.upperBound - sizeRange.lowerBound)
                    let baseOpacity = opacityRange.lowerBound + fract(seed * 4.3) * (opacityRange.upperBound - opacityRange.lowerBound)
                    let lifetime = 2.0 + fract(seed * 5.1) * 2.0 // 2-4 seconds

                    // Calculate current position in lifecycle
                    let phase = fract((time * particleSpeed / 100.0 + seed) / lifetime)

                    // Y position: rise from bottom to top
                    let y = size.height * (1.0 - phase)

                    // X position: slight horizontal drift
                    let drift = sin(time * 0.5 + seed * 10) * 20
                    let x = baseX * size.width + drift

                    // Opacity: fade in and out
                    let fadeIn = min(1.0, phase * 5.0) // Quick fade in
                    let fadeOut = min(1.0, (1.0 - phase) * 3.0) // Slower fade out
                    let opacity = baseOpacity * fadeIn * fadeOut

                    // Scale: grow then shrink
                    let scalePhase = phase < 0.3 ? phase / 0.3 : (1.0 - (phase - 0.3) / 0.7)
                    let scale = 0.5 + scalePhase * 0.5

                    // Draw particle
                    let rect = CGRect(
                        x: x - particleSize * scale / 2,
                        y: y - particleSize * scale / 2,
                        width: particleSize * scale,
                        height: particleSize * scale
                    )

                    context.fill(
                        Circle().path(in: rect),
                        with: .color(color.opacity(opacity))
                    )
                }
            }
        }
    }

    private func fract(_ x: Double) -> Double {
        x - floor(x)
    }
}
