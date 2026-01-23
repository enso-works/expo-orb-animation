import SwiftUI

public struct BreathingConfiguration {
    var blobColors: [Color]
    var innerBlobColor: Color
    var glowColor: Color
    var particleColor: Color
    var progressRingColor: Color
    var textColor: Color
    var showProgressRing: Bool
    var showTextCue: Bool
    var showInnerBlob: Bool
    var showShadow: Bool
    var showParticles: Bool
    var showWavyBlobs: Bool
    var showGlowEffects: Bool
    var pointCount: Int
    var wobbleIntensity: Double

    public init(
        blobColors: [Color] = [
            Color(red: 0.4, green: 0.7, blue: 0.9),
            Color(red: 0.3, green: 0.5, blue: 0.8),
            Color(red: 0.5, green: 0.3, blue: 0.7)
        ],
        innerBlobColor: Color = Color.white.opacity(0.3),
        glowColor: Color = .white,
        particleColor: Color = .white,
        progressRingColor: Color = Color.white.opacity(0.5),
        textColor: Color = .white,
        showProgressRing: Bool = true,
        showTextCue: Bool = true,
        showInnerBlob: Bool = true,
        showShadow: Bool = true,
        showParticles: Bool = true,
        showWavyBlobs: Bool = true,
        showGlowEffects: Bool = true,
        pointCount: Int = 8,
        wobbleIntensity: Double = 1.0
    ) {
        self.blobColors = blobColors
        self.innerBlobColor = innerBlobColor
        self.glowColor = glowColor
        self.particleColor = particleColor
        self.progressRingColor = progressRingColor
        self.textColor = textColor
        self.showProgressRing = showProgressRing
        self.showTextCue = showTextCue
        self.showInnerBlob = showInnerBlob
        self.showShadow = showShadow
        self.showParticles = showParticles
        self.showWavyBlobs = showWavyBlobs
        self.showGlowEffects = showGlowEffects
        self.pointCount = pointCount
        self.wobbleIntensity = wobbleIntensity
    }
}
