import SwiftUI

public struct BreathingConfiguration {
    var blobColors: [Color]
    var innerBlobColor: Color
    var progressRingColor: Color
    var textColor: Color
    var showProgressRing: Bool
    var showTextCue: Bool
    var showInnerBlob: Bool
    var showShadow: Bool
    var pointCount: Int
    var wobbleIntensity: Double

    public init(
        blobColors: [Color] = [
            Color(red: 0.4, green: 0.7, blue: 0.9),
            Color(red: 0.3, green: 0.5, blue: 0.8),
            Color(red: 0.5, green: 0.3, blue: 0.7)
        ],
        innerBlobColor: Color = Color.white.opacity(0.3),
        progressRingColor: Color = Color.white.opacity(0.5),
        textColor: Color = .white,
        showProgressRing: Bool = true,
        showTextCue: Bool = true,
        showInnerBlob: Bool = true,
        showShadow: Bool = true,
        pointCount: Int = 8,
        wobbleIntensity: Double = 1.0
    ) {
        self.blobColors = blobColors
        self.innerBlobColor = innerBlobColor
        self.progressRingColor = progressRingColor
        self.textColor = textColor
        self.showProgressRing = showProgressRing
        self.showTextCue = showTextCue
        self.showInnerBlob = showInnerBlob
        self.showShadow = showShadow
        self.pointCount = pointCount
        self.wobbleIntensity = wobbleIntensity
    }
}
