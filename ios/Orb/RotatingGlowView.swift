//
//  BackgroundView.swift
//  Prototype-Orb
//
//  Created by Siddhant Mehta on 2024-11-06.
//

import SwiftUI

enum RotationDirection {
    case clockwise
    case counterClockwise

    var multiplier: Double {
        switch self {
        case .clockwise: return 1
        case .counterClockwise: return -1
        }
    }
}

struct RotatingGlowView: View {
    private let color: Color
    private let rotationSpeed: Double
    private let direction: RotationDirection
    private let elapsedTime: Double

    init(color: Color,
         rotationSpeed: Double = 30,
         direction: RotationDirection,
         elapsedTime: Double = 0)
    {
        self.color = color
        self.rotationSpeed = rotationSpeed
        self.direction = direction
        self.elapsedTime = elapsedTime
    }

    var body: some View {
        GeometryReader { geometry in
            let size = min(geometry.size.width, geometry.size.height)
            let safeSpeed = max(0.01, rotationSpeed)
            let rotation = elapsedTime * safeSpeed * direction.multiplier

            Circle()
                .fill(color)
                .mask {
                    ZStack {
                        Circle()
                            .frame(width: size, height: size)
                            .blur(radius: size * 0.16)
                        Circle()
                            .frame(width: size * 1.31, height: size * 1.31)
                            .offset(y: size * 0.31)
                            .blur(radius: size * 0.16)
                            .blendMode(.destinationOut)
                    }
                }
                .rotationEffect(.degrees(rotation))
        }
    }
}
