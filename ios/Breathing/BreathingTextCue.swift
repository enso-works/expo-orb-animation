import SwiftUI

struct BreathingTextCue: View {
    let text: String
    let color: Color

    var body: some View {
        Text(text)
            .font(.system(size: 24, weight: .medium, design: .rounded))
            .foregroundColor(color)
            .shadow(color: color.opacity(0.5), radius: 4, x: 0, y: 2)
            .animation(.easeInOut(duration: 0.3), value: text)
    }
}
