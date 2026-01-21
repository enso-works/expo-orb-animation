import ExpoModulesCore
import UIKit

public class ExpoIosOrbModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoIosOrb")

    // Use a Function instead of Prop for activity - bypasses React's prop reconciliation
    // Dispatch async to avoid blocking the JS thread and causing animation hiccups
    Function("setActivity") { (activity: Double) in
      let clampedActivity = max(0, min(1, activity))
      DispatchQueue.main.async {
        OrbSharedState.shared.targetActivity = clampedActivity
      }
    }

    View(ExpoIosOrbView.self) {
      Prop("backgroundColors") { (view: ExpoIosOrbView, colors: [UIColor]) in
        view.updateProps { $0.backgroundColors = colors }
      }

      Prop("glowColor") { (view: ExpoIosOrbView, color: UIColor) in
        view.updateProps { $0.glowColor = color }
      }

      Prop("particleColor") { (view: ExpoIosOrbView, color: UIColor) in
        view.updateProps { $0.particleColor = color }
      }

      Prop("coreGlowIntensity") { (view: ExpoIosOrbView, value: Double) in
        view.updateProps { $0.coreGlowIntensity = value }
      }

      Prop("breathingIntensity") { (view: ExpoIosOrbView, value: Double) in
        view.updateProps { $0.breathingIntensity = value }
      }

      Prop("breathingSpeed") { (view: ExpoIosOrbView, value: Double) in
        view.updateProps { $0.breathingSpeed = value }
      }

      Prop("showBackground") { (view: ExpoIosOrbView, value: Bool) in
        view.updateProps { $0.showBackground = value }
      }

      Prop("showWavyBlobs") { (view: ExpoIosOrbView, value: Bool) in
        view.updateProps { $0.showWavyBlobs = value }
      }

      Prop("showParticles") { (view: ExpoIosOrbView, value: Bool) in
        view.updateProps { $0.showParticles = value }
      }

      Prop("showGlowEffects") { (view: ExpoIosOrbView, value: Bool) in
        view.updateProps { $0.showGlowEffects = value }
      }

      Prop("showShadow") { (view: ExpoIosOrbView, value: Bool) in
        view.updateProps { $0.showShadow = value }
      }

      Prop("speed") { (view: ExpoIosOrbView, value: Double) in
        view.updateProps { $0.speed = value }
      }

      // NOTE: activity is handled via Function("setActivity") instead of Prop
      // to bypass React's prop reconciliation and prevent view re-renders
    }
  }
}
