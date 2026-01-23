import ExpoModulesCore
import SwiftUI

private final class BreathingConfigurationModel: ObservableObject {
    @Published var configuration: BreathingConfiguration

    init(configuration: BreathingConfiguration) {
        self.configuration = configuration
    }
}

private struct BreathingContainerView: View {
    @ObservedObject var model: BreathingConfigurationModel

    var body: some View {
        BreathingExerciseView(configuration: model.configuration)
    }
}

struct BreathingProps {
    private static let defaultBlobColors: [UIColor] = [
        UIColor(red: 0.4, green: 0.7, blue: 0.9, alpha: 1.0),
        UIColor(red: 0.3, green: 0.5, blue: 0.8, alpha: 1.0),
        UIColor(red: 0.5, green: 0.3, blue: 0.7, alpha: 1.0)
    ]

    var blobColors: [UIColor] = BreathingProps.defaultBlobColors
    var innerBlobColor: UIColor = UIColor.white.withAlphaComponent(0.3)
    var glowColor: UIColor = .white
    var particleColor: UIColor = .white
    var progressRingColor: UIColor = UIColor.white.withAlphaComponent(0.5)
    var textColor: UIColor = .white
    var showProgressRing: Bool = true
    var showTextCue: Bool = true
    var showInnerBlob: Bool = true
    var showShadow: Bool = true
    var showParticles: Bool = true
    var showWavyBlobs: Bool = true
    var showGlowEffects: Bool = true
    var pointCount: Int = 8
    var wobbleIntensity: Double = 1.0

    func makeConfiguration() -> BreathingConfiguration {
        let resolvedBlobColors = blobColors.count >= 2
            ? blobColors
            : BreathingProps.defaultBlobColors

        return BreathingConfiguration(
            blobColors: resolvedBlobColors.map { Color(uiColor: $0) },
            innerBlobColor: Color(uiColor: innerBlobColor),
            glowColor: Color(uiColor: glowColor),
            particleColor: Color(uiColor: particleColor),
            progressRingColor: Color(uiColor: progressRingColor),
            textColor: Color(uiColor: textColor),
            showProgressRing: showProgressRing,
            showTextCue: showTextCue,
            showInnerBlob: showInnerBlob,
            showShadow: showShadow,
            showParticles: showParticles,
            showWavyBlobs: showWavyBlobs,
            showGlowEffects: showGlowEffects,
            pointCount: max(6, pointCount),
            wobbleIntensity: max(0, min(1, wobbleIntensity))
        )
    }
}

class ExpoBreathingExerciseView: ExpoView {
    private var props = BreathingProps()
    private let model: BreathingConfigurationModel
    private let hostingController: UIHostingController<BreathingContainerView>

    private let onPhaseChange = EventDispatcher()
    private let onExerciseComplete = EventDispatcher()

    required init(appContext: AppContext? = nil) {
        let initialConfig = BreathingConfiguration()
        let model = BreathingConfigurationModel(configuration: initialConfig)
        self.model = model
        self.hostingController = UIHostingController(rootView: BreathingContainerView(model: model))

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

        // Setup callbacks
        setupCallbacks()
    }

    private func setupCallbacks() {
        BreathingSharedState.shared.onPhaseChange = { [weak self] phase, label, phaseIndex, cycle in
            self?.onPhaseChange([
                "phase": phase.rawValue,
                "label": label,
                "phaseIndex": phaseIndex,
                "cycle": cycle
            ])
        }

        BreathingSharedState.shared.onExerciseComplete = { [weak self] totalCycles, totalDuration in
            self?.onExerciseComplete([
                "totalCycles": totalCycles,
                "totalDuration": totalDuration
            ])
        }
    }

    func updateProps(_ update: (inout BreathingProps) -> Void) {
        update(&props)
        model.configuration = props.makeConfiguration()
    }
}
