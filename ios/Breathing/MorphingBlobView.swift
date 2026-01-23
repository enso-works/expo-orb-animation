import SwiftUI

struct MorphingBlobView: View {
    let baseRadius: CGFloat
    let pointCount: Int
    let offsets: [Double]
    let colors: [Color]
    let innerColor: Color
    let showInnerBlob: Bool
    var elapsedTime: Double = 0

    var body: some View {
        GeometryReader { geometry in
            let size = min(geometry.size.width, geometry.size.height)
            let center = CGPoint(x: size / 2, y: size / 2)
            let effectiveRadius = baseRadius * size / 2

            ZStack {
                // Main blob with linear gradient fill (like OrbView)
                MorphingBlobShape(
                    center: center,
                    baseRadius: effectiveRadius,
                    pointCount: pointCount,
                    offsets: offsets
                )
                .fill(
                    LinearGradient(
                        colors: colors,
                        startPoint: .bottom,
                        endPoint: .top
                    )
                )

                // Inner glow overlay (like OrbView's realisticInnerGlows)
                MorphingBlobShape(
                    center: center,
                    baseRadius: effectiveRadius,
                    pointCount: pointCount,
                    offsets: offsets
                )
                .stroke(
                    LinearGradient(
                        colors: [.white.opacity(0.6), .clear],
                        startPoint: .bottom,
                        endPoint: .top
                    ),
                    lineWidth: 3
                )
                .blur(radius: 4)
                .blendMode(.plusLighter)

                // Inner animated blob like OrbView's WavyBlobView
                if showInnerBlob {
                    InnerWavyBlob(
                        center: center,
                        baseRadius: effectiveRadius * 0.55,
                        color: innerColor,
                        elapsedTime: elapsedTime
                    )
                    .blur(radius: 10)
                    .blendMode(.plusLighter)
                }
            }
        }
    }
}

// Inner blob with time-based animation like WavyBlobView
struct InnerWavyBlob: View {
    let center: CGPoint
    let baseRadius: CGFloat
    let color: Color
    let elapsedTime: Double

    private let pointCount = 6
    private let loopDuration: Double = 4.0

    var body: some View {
        Canvas { context, size in
            let angle = (elapsedTime.truncatingRemainder(dividingBy: loopDuration) / loopDuration) * 2 * .pi

            var path = Path()

            // Generate points with time-based smooth movement
            var points: [CGPoint] = []
            for i in 0..<pointCount {
                let baseAngle = (Double(i) / Double(pointCount)) * 2 * .pi
                let phaseOffset = Double(i) * .pi / 3

                // Smooth oscillating offsets
                let radiusOffset = sin(angle * 2 + phaseOffset) * 0.15 + cos(angle * 1.5 + phaseOffset * 0.7) * 0.1
                let angleOffset = cos(angle + phaseOffset) * 0.1

                let r = baseRadius * (1.0 + radiusOffset)
                let a = baseAngle + angleOffset

                points.append(CGPoint(
                    x: center.x + cos(a) * r,
                    y: center.y + sin(a) * r
                ))
            }

            // Start path
            path.move(to: points[0])

            // Create smooth curves between points
            let handleLength = baseRadius * 0.5

            for i in 0..<pointCount {
                let current = points[i]
                let next = points[(i + 1) % pointCount]

                let currentAngle = atan2(current.y - center.y, current.x - center.x)
                let nextAngle = atan2(next.y - center.y, next.x - center.x)

                let control1 = CGPoint(
                    x: current.x + cos(currentAngle + .pi / 2) * handleLength,
                    y: current.y + sin(currentAngle + .pi / 2) * handleLength
                )

                let control2 = CGPoint(
                    x: next.x + cos(nextAngle - .pi / 2) * handleLength,
                    y: next.y + sin(nextAngle - .pi / 2) * handleLength
                )

                path.addCurve(to: next, control1: control1, control2: control2)
            }

            path.closeSubpath()
            context.fill(path, with: .color(color))
        }
    }
}

struct MorphingBlobShape: Shape {
    var center: CGPoint
    var baseRadius: CGFloat
    var pointCount: Int
    var offsets: [Double]

    var animatableData: AnimatableVector {
        get { AnimatableVector(values: offsets) }
        set { offsets = newValue.values }
    }

    func path(in rect: CGRect) -> Path {
        var path = Path()

        guard pointCount >= 3 else { return path }

        // Generate points around the circle with offsets
        var points: [CGPoint] = []
        for i in 0..<pointCount {
            let angle = (Double(i) / Double(pointCount)) * 2 * .pi - .pi / 2
            let offset = i < offsets.count ? offsets[i] : 0
            let radius = baseRadius * (1.0 + offset)

            points.append(CGPoint(
                x: center.x + CGFloat(cos(angle)) * radius,
                y: center.y + CGFloat(sin(angle)) * radius
            ))
        }

        // Tangent coefficient for smooth cubic bezier curves
        let tangentCoeff = CGFloat((4.0 / 3.0) * tan(.pi / (2.0 * Double(pointCount))))

        // Start at first point
        path.move(to: points[0])

        // Draw cubic bezier curves between each pair of points
        for i in 0..<pointCount {
            let current = points[i]
            let next = points[(i + 1) % pointCount]

            let currentAngle = (Double(i) / Double(pointCount)) * 2 * .pi - .pi / 2
            let nextAngle = (Double((i + 1) % pointCount) / Double(pointCount)) * 2 * .pi - .pi / 2

            let currentOffset = i < offsets.count ? offsets[i] : 0
            let currentRadius = baseRadius * CGFloat(1.0 + currentOffset)
            let currentTangentLength = currentRadius * tangentCoeff

            let nextOffset = (i + 1) % pointCount < offsets.count ? offsets[(i + 1) % pointCount] : 0
            let nextRadius = baseRadius * CGFloat(1.0 + nextOffset)
            let nextTangentLength = nextRadius * tangentCoeff

            let control1 = CGPoint(
                x: current.x + CGFloat(cos(currentAngle + .pi / 2)) * currentTangentLength,
                y: current.y + CGFloat(sin(currentAngle + .pi / 2)) * currentTangentLength
            )

            let control2 = CGPoint(
                x: next.x + CGFloat(cos(nextAngle - .pi / 2)) * nextTangentLength,
                y: next.y + CGFloat(sin(nextAngle - .pi / 2)) * nextTangentLength
            )

            path.addCurve(to: next, control1: control1, control2: control2)
        }

        path.closeSubpath()
        return path
    }
}

struct AnimatableVector: VectorArithmetic {
    var values: [Double]

    static var zero: AnimatableVector {
        AnimatableVector(values: [])
    }

    static func + (lhs: AnimatableVector, rhs: AnimatableVector) -> AnimatableVector {
        let maxCount = max(lhs.values.count, rhs.values.count)
        var result = [Double](repeating: 0, count: maxCount)
        for i in 0..<maxCount {
            let l = i < lhs.values.count ? lhs.values[i] : 0
            let r = i < rhs.values.count ? rhs.values[i] : 0
            result[i] = l + r
        }
        return AnimatableVector(values: result)
    }

    static func - (lhs: AnimatableVector, rhs: AnimatableVector) -> AnimatableVector {
        let maxCount = max(lhs.values.count, rhs.values.count)
        var result = [Double](repeating: 0, count: maxCount)
        for i in 0..<maxCount {
            let l = i < lhs.values.count ? lhs.values[i] : 0
            let r = i < rhs.values.count ? rhs.values[i] : 0
            result[i] = l - r
        }
        return AnimatableVector(values: result)
    }

    mutating func scale(by rhs: Double) {
        for i in 0..<values.count {
            values[i] *= rhs
        }
    }

    var magnitudeSquared: Double {
        values.reduce(0) { $0 + $1 * $1 }
    }
}
