import SwiftUI

struct BreathingTextCue: View {
    let text: String
    let color: Color

    var body: some View {
        Text(text)
            .font(.system(size: 24, weight: .semibold, design: .rounded))
            .foregroundColor(color)
            .shadow(color: Color.black.opacity(0.5), radius: 8, x: 0, y: 2)
            .animation(.easeInOut(duration: 0.3), value: text)
    }
}
