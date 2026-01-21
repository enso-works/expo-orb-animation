import ExpoModulesCore
import SwiftUI

private final class OrbConfigurationModel: ObservableObject {
  @Published var configuration: OrbConfiguration

  init(configuration: OrbConfiguration) {
    self.configuration = configuration
  }
}

private struct OrbContainerView: View {
  @ObservedObject var model: OrbConfigurationModel

  var body: some View {
    // OrbView reads activity from OrbSharedState directly during animation loop
    OrbView(configuration: model.configuration, useSharedActivityState: true)
  }
}

struct OrbProps {
  private static let defaultBackgroundColors: [UIColor] = [
    .systemGreen,
    .systemBlue,
    .systemPink,
  ]

  var backgroundColors: [UIColor] = OrbProps.defaultBackgroundColors
  var glowColor: UIColor = .white
  var particleColor: UIColor = .white
  var coreGlowIntensity: Double = 1.0
  var breathingIntensity: Double = 0
  var breathingSpeed: Double = 0.25
  var showBackground: Bool = true
  var showWavyBlobs: Bool = true
  var showParticles: Bool = true
  var showGlowEffects: Bool = true
  var showShadow: Bool = true
  var speed: Double = 60

  func makeConfiguration() -> OrbConfiguration {
    let resolvedBackgroundColors = backgroundColors.count >= 2
      ? backgroundColors
      : OrbProps.defaultBackgroundColors
    let safeSpeed = max(1, speed)
    let safeIntensity = max(0, coreGlowIntensity)
    let safeBreathingIntensity = max(0, breathingIntensity)
    let safeBreathingSpeed = max(0.05, breathingSpeed)

    return OrbConfiguration(
      backgroundColors: resolvedBackgroundColors.map { Color(uiColor: $0) },
      glowColor: Color(uiColor: glowColor),
      particleColor: Color(uiColor: particleColor),
      coreGlowIntensity: safeIntensity,
      showBackground: showBackground,
      showWavyBlobs: showWavyBlobs,
      showParticles: showParticles,
      showGlowEffects: showGlowEffects,
      showShadow: showShadow,
      speed: safeSpeed,
      breathingIntensity: safeBreathingIntensity,
      breathingSpeed: safeBreathingSpeed
    )
  }
}

class ExpoOrbView: ExpoView {
  private var props = OrbProps()
  private let model: OrbConfigurationModel
  private let hostingController: UIHostingController<OrbContainerView>

  required init(appContext: AppContext? = nil) {
    let initialConfig = OrbConfiguration()
    let model = OrbConfigurationModel(configuration: initialConfig)
    self.model = model
    self.hostingController = UIHostingController(rootView: OrbContainerView(model: model))

    super.init(appContext: appContext)

    clipsToBounds = false
    hostingController.view.backgroundColor = .clear
    hostingController.view.clipsToBounds = false
    hostingController.view.isOpaque = false
    hostingController.view.translatesAutoresizingMaskIntoConstraints = false
    addSubview(hostingController.view)

    NSLayoutConstraint.activate([
      hostingController.view.leadingAnchor.constraint(equalTo: leadingAnchor),
      hostingController.view.trailingAnchor.constraint(equalTo: trailingAnchor),
      hostingController.view.topAnchor.constraint(equalTo: topAnchor),
      hostingController.view.bottomAnchor.constraint(equalTo: bottomAnchor),
    ])
  }

  func updateProps(_ update: (inout OrbProps) -> Void) {
    update(&props)
    model.configuration = props.makeConfiguration()
  }

  func setTargetActivity(_ target: Double) {
    // Write to shared state - no SwiftUI re-render triggered
    // OrbView reads this during its animation loop
    OrbSharedState.shared.targetActivity = max(0, min(1, target))
  }
}
