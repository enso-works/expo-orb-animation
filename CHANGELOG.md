# Changelog

## 0.3.0

- `startBreathingExercise` validates the pattern and throws a `TypeError` that names the problem (no phases, a duration or scale that is not above 0, an unknown phase, fractional or zero cycles, a bad `startTime`). Native used to drop such patterns, or phases, without a word, and a zero duration gave NaN progress on iOS.
- `getBreathingPreset` returns a copy, and throws for an unknown name.
- `setOrbActivity` ignores `NaN`.
- Web: importing the package no longer throws. The breathing view renders a still circle and the controls do nothing, like the orb already did.
- Released from GitHub Actions with npm provenance; CI runs types, lint, tests and a package contents check on every pull request.
- README rewritten: compatibility, `startTime`, one exercise at a time, corrected defaults (`wobbleIntensity` is `1`).

## 0.2.4

- iOS: the breathing phase is worked out from the exercise's start time on every frame, so it no longer drifts a phase after the app was in the background. New optional `startTime` on a pattern.
- iOS: `cycles`, `duration` and `targetScale` are read as `NSNumber`, so `cycles` works (it was always treated as infinite and `onExerciseComplete` never fired).
- Android: views attach their Compose content and event callbacks in `onAttachedToWindow`, fixing the "Cannot locate windowRecomposer" crash and events going to a recycled view under Fabric.

## 0.2.3

- Progress ring drawn outside the blob mask, so it shows.
- Android and iOS edge-case fixes from the example's test screen.

## 0.2.2

- Breathing exercise: particles, wavy blobs and glow effects; closer Android and iOS visuals.

## 0.1.0

- First release: `ExpoOrbView` and `setOrbActivity` on iOS and Android.
