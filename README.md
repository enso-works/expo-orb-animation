# expo-orb

[![npm](https://img.shields.io/npm/v/expo-orb)](https://www.npmjs.com/package/expo-orb)
[![CI](https://github.com/enso-works/expo-orb-animation/actions/workflows/ci.yml/badge.svg)](https://github.com/enso-works/expo-orb-animation/actions/workflows/ci.yml)
[![license](https://img.shields.io/npm/l/expo-orb)](LICENSE)

Two native animated views for Expo apps:

- **`ExpoOrbView`**, a glowing orb with particles and wavy blobs that reacts to an activity level (a voice assistant that is listening or speaking, for example).
- **`ExpoBreathingExerciseView`**, a guided breathing exercise: a morphing blob, a progress ring and text cues that follow a pattern of inhale, hold and exhale phases.

SwiftUI on iOS, Jetpack Compose on Android. The iOS orb is based on [metasidd/Orb](https://github.com/metasidd/Orb). Used in production by [Braele](https://braele.app).

| iOS | Android |
| :-: | :-: |
| ![Orb on iOS](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/demo-ios.gif) | ![Orb on Android](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/demo-android.gif) |

## Install

```sh
npx expo install expo-orb
```

It is a native module, so it needs a development build (`npx expo run:ios` or EAS Build), not Expo Go. In a bare React Native app, run `npx pod-install` afterwards.

| | Supported |
| --- | --- |
| Expo SDK | 54 and 56 are tested |
| iOS | 15.1 and later |
| Android | API 24 and later |
| Web | Renders a still gradient circle; the animation and controls do nothing |

## Orb

```tsx
import { ExpoOrbView, setOrbActivity } from 'expo-orb';

export function Assistant() {
  return (
    <ExpoOrbView
      style={{ width: 220, height: 220 }}
      backgroundColors={['#7c3aed', '#3b82f6', '#ec4899']}
    />
  );
}

setOrbActivity(0.85); // speaking
setOrbActivity(0.1); // idle
```

`setOrbActivity(level)` sets how lively the orb is, from `0` (calm) to `1` (very active). It goes straight to native instead of through props, so frequent updates (every audio frame, say) do not re-render React or restart the animation. The level is shared by every orb on screen.

### Orb props

| Prop | Type | Default | |
| --- | --- | --- | --- |
| `backgroundColors` | `ColorValue[]` | green, blue, pink | Gradient colours, at least two |
| `glowColor` | `ColorValue` | white | |
| `particleColor` | `ColorValue` | white | |
| `coreGlowIntensity` | `number` | `1` | `0` and up |
| `breathingIntensity` | `number` | `0` | How much the orb swells, `0` to `1` |
| `breathingSpeed` | `number` | `0.25` | |
| `speed` | `number` | `60` | Overall animation speed |
| `showBackground` | `boolean` | `true` | |
| `showWavyBlobs` | `boolean` | `true` | |
| `showParticles` | `boolean` | `true` | |
| `showGlowEffects` | `boolean` | `true` | |
| `showShadow` | `boolean` | `true` | |
| `style` | `ViewStyle` | | Give it a width and height |

## Breathing exercise

```tsx
import { Button } from 'react-native';
import {
  ExpoBreathingExerciseView,
  getBreathingPreset,
  startBreathingExercise,
  stopBreathingExercise,
} from 'expo-orb';

export function Breathe() {
  return (
    <>
      <ExpoBreathingExerciseView
        style={{ width: 300, height: 300 }}
        onPhaseChange={({ nativeEvent }) => console.log(nativeEvent.label)}
        onExerciseComplete={() => console.log('done')}
      />
      <Button title="Box breathing" onPress={() => startBreathingExercise(getBreathingPreset('box'))} />
      <Button title="Stop" onPress={stopBreathingExercise} />
    </>
  );
}
```

The view draws the exercise; the functions run it. There is one exercise at a time, shared by every breathing view that is mounted.

### Patterns

```ts
startBreathingExercise({
  phases: [
    { phase: 'inhale', duration: 4000, targetScale: 1.35, label: 'Breathe in' },
    { phase: 'holdIn', duration: 7000, targetScale: 1.35, label: 'Hold' },
    { phase: 'exhale', duration: 8000, targetScale: 0.75, label: 'Breathe out' },
  ],
  cycles: 4, // leave out to loop until stopped
  startTime: Date.now(), // optional, see below
});
```

| Field | |
| --- | --- |
| `phases[].phase` | `'inhale'`, `'holdIn'`, `'exhale'` or `'holdOut'` |
| `phases[].duration` | Milliseconds, above 0 |
| `phases[].targetScale` | Blob size at the end of the phase, `1` is the resting size |
| `phases[].label` | Text shown in the middle; translate it yourself |
| `cycles` | Whole number above 0, or leave out to loop |
| `startTime` | Epoch milliseconds the exercise counts from (iOS) |

`startBreathingExercise` throws a `TypeError` naming the problem when a pattern is invalid (no phases, a zero or negative duration, fractional cycles, and so on). Earlier versions ignored such patterns without a word.

**`startTime`** keeps the animation in step with anything else you time from the same moment, such as voice cues, haptics or a Live Activity. On iOS the phase is worked out from this time on every frame, so it is still right after the app comes back from the background. Android ignores it for now and counts from the moment the exercise starts.

### Presets

`getBreathingPreset(name)` returns a copy you can change.

| Name | Pattern | Cycles |
| --- | --- | --- |
| `'box'` | 4 in, 4 hold, 4 out, 4 hold | 4 |
| `'relaxing'` | 4 in, 7 hold, 8 out (4-7-8) | 4 |
| `'calming'` | 4 in, 6 out | 6 |
| `'energizing'` | 2 in, 2 out | 10 |

### Controls

| Function | |
| --- | --- |
| `startBreathingExercise(pattern)` | Starts, replacing a running exercise |
| `pauseBreathingExercise()` | |
| `resumeBreathingExercise()` | |
| `stopBreathingExercise()` | |

### Events

| Event | `nativeEvent` | |
| --- | --- | --- |
| `onPhaseChange` | `{ phase, label, phaseIndex, cycle }` | At the start of every phase |
| `onExerciseComplete` | `{ totalCycles, totalDuration }` | After the last cycle; never fires for a looping pattern |

### Breathing view props

| Prop | Type | Default | |
| --- | --- | --- | --- |
| `blobColors` | `ColorValue[]` | soft blues and purple | Gradient colours, at least two |
| `innerBlobColor` | `ColorValue` | white, 30% | |
| `glowColor` | `ColorValue` | white | |
| `particleColor` | `ColorValue` | white | |
| `progressRingColor` | `ColorValue` | white, 50% | |
| `textColor` | `ColorValue` | white | |
| `pointCount` | `number` | `8` | Points on the morphing blob |
| `wobbleIntensity` | `number` | `1` | `0` to `1` |
| `showProgressRing` | `boolean` | `true` | |
| `showTextCue` | `boolean` | `true` | |
| `showInnerBlob` | `boolean` | `true` | |
| `showParticles` | `boolean` | `true` | |
| `showWavyBlobs` | `boolean` | `true` | |
| `showGlowEffects` | `boolean` | `true` | |
| `showShadow` | `boolean` | `true` | |
| `style` | `ViewStyle` | | Give it a width and height |

## Example app

`example/` shows both views, the presets and the controls:

```sh
cd example && npm install && npx expo run:ios
```

## Contributing and releases

`npm test`, `npx eslint .` and `npx tsc -p tsconfig.json --noEmit` run on every pull request. To release, bump `version` in `package.json`, add a line to [CHANGELOG.md](CHANGELOG.md), merge, and push a `v<version>` tag: the Publish workflow checks, builds and publishes to npm with provenance.

## License

MIT
