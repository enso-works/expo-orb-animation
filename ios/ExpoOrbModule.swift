import ExpoModulesCore
import UIKit

public class ExpoOrbModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoOrb")

    // Use a Function instead of Prop for activity - bypasses React's prop reconciliation
    // Dispatch async to avoid blocking the JS thread and causing animation hiccups
    Function("setActivity") { (activity: Double) in
      let clampedActivity = max(0, min(1, activity))
      DispatchQueue.main.async {
        OrbSharedState.shared.targetActivity = clampedActivity
      }
    }

    View(ExpoOrbView.self) {
      Prop("backgroundColors") { (view: ExpoOrbView, colors: [UIColor]) in
        view.updateProps { $0.backgroundColors = colors }
      }

      Prop("glowColor") { (view: ExpoOrbView, color: UIColor) in
        view.updateProps { $0.glowColor = color }
      }

      Prop("particleColor") { (view: ExpoOrbView, color: UIColor) in
        view.updateProps { $0.particleColor = color }
      }

      Prop("coreGlowIntensity") { (view: ExpoOrbView, value: Double) in
        view.updateProps { $0.coreGlowIntensity = value }
      }

      Prop("breathingIntensity") { (view: ExpoOrbView, value: Double) in
        view.updateProps { $0.breathingIntensity = value }
      }

      Prop("breathingSpeed") { (view: ExpoOrbView, value: Double) in
        view.updateProps { $0.breathingSpeed = value }
      }

      Prop("showBackground") { (view: ExpoOrbView, value: Bool) in
        view.updateProps { $0.showBackground = value }
      }

      Prop("showWavyBlobs") { (view: ExpoOrbView, value: Bool) in
        view.updateProps { $0.showWavyBlobs = value }
      }

      Prop("showParticles") { (view: ExpoOrbView, value: Bool) in
        view.updateProps { $0.showParticles = value }
      }

      Prop("showGlowEffects") { (view: ExpoOrbView, value: Bool) in
        view.updateProps { $0.showGlowEffects = value }
      }

      Prop("showShadow") { (view: ExpoOrbView, value: Bool) in
        view.updateProps { $0.showShadow = value }
      }

      Prop("speed") { (view: ExpoOrbView, value: Double) in
        view.updateProps { $0.speed = value }
      }

      // NOTE: activity is handled via Function("setActivity") instead of Prop
      // to bypass React's prop reconciliation and prevent view re-renders
    }
  }
}
