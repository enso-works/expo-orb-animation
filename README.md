# expo-orb

[![npm](https://img.shields.io/npm/v/expo-orb)](https://www.npmjs.com/package/expo-orb)
[![CI](https://github.com/enso-works/expo-orb-animation/actions/workflows/ci.yml/badge.svg)](https://github.com/enso-works/expo-orb-animation/actions/workflows/ci.yml)
[![license](https://img.shields.io/npm/l/expo-orb)](LICENSE)

Two native animated views for Expo apps:

- **`ExpoOrbView`**, a glowing orb with particles and wavy blobs that reacts to an activity level (a voice assistant that is listening or speaking, for example).
- **`ExpoBreathingExerciseView`**, a morphing blob with a progress ring and a text cue that steps through timed phases: an interval timer, a countdown, any guided routine.

SwiftUI on iOS, Jetpack Compose on Android. The iOS orb is based on [metasidd/Orb](https://github.com/metasidd/Orb). Used in production by [Braele](https://braele.app).

| iOS | Android |
| :-: | :-: |
| ![Orb on iOS](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/orb-ios.gif) | ![Orb on Android](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/orb-android.gif) |

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

## Phase view

`ExpoBreathingExerciseView` grows and shrinks a blob through a sequence of timed phases, with a progress ring and a label for each one. The API keeps its original names (`startBreathingExercise` and so on); a phase can be anything you time.

| iOS | Android |
| :-: | :-: |
| ![Interval timer on iOS](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/intervals-ios.gif) | ![Interval timer on Android](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/intervals-android.gif) |

```tsx
import { Button } from 'react-native';
import {
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  type BreathingPattern,
} from 'expo-orb';

const intervals: BreathingPattern = {
  phases: [
    { phase: 'inhale', duration: 20000, targetScale: 1.3, label: 'Sprint' },
    { phase: 'exhale', duration: 10000, targetScale: 0.8, label: 'Rest' },
  ],
  cycles: 8,
};

export function IntervalTimer() {
  return (
    <>
      <ExpoBreathingExerciseView
        style={{ width: 300, height: 300 }}
        blobColors={['#f97316', '#ef4444', '#db2777']}
        onPhaseChange={({ nativeEvent }) => console.log(nativeEvent.label)}
        onExerciseComplete={() => console.log('done')}
      />
      <Button title="Start" onPress={() => startBreathingExercise(intervals)} />
      <Button title="Stop" onPress={stopBreathingExercise} />
    </>
  );
}
```

The view draws the sequence; the functions run it. There is one sequence at a time, shared by every view that is mounted.

### Patterns

A countdown that ends with a burst:

```ts
startBreathingExercise({
  phases: [
    { phase: 'holdOut', duration: 1000, targetScale: 0.8, label: '3' },
    { phase: 'holdOut', duration: 1000, targetScale: 0.9, label: '2' },
    { phase: 'holdOut', duration: 1000, targetScale: 1.0, label: '1' },
    { phase: 'inhale', duration: 800, targetScale: 1.3, label: 'Go' },
  ],
  cycles: 1, // leave out to loop until stopped
  startTime: Date.now(), // optional, see below
});
```

| Field | |
| --- | --- |
| `phases[].phase` | How the phase moves: `'inhale'` grows or shrinks to `targetScale` while the ring fills, `'exhale'` does the same while the ring empties, `'holdIn'` and `'holdOut'` jump to `targetScale` and stay there with the ring full or empty |
| `phases[].duration` | Milliseconds, above 0 |
| `phases[].targetScale` | Blob size at the end of the phase, `1` is the resting size |
| `phases[].label` | Text shown in the middle; translate it yourself |
| `cycles` | Whole number above 0, or leave out to loop |
| `startTime` | Epoch milliseconds the sequence counts from (iOS) |

`startBreathingExercise` throws a `TypeError` naming the problem when a pattern is invalid (no phases, a zero or negative duration, fractional cycles, and so on). Earlier versions ignored such patterns without a word.

**`startTime`** keeps the animation in step with anything else you time from the same moment, such as audio cues, haptics or a Live Activity. On iOS the phase is worked out from this time on every frame, so it is still right after the app comes back from the background. Android ignores it for now and counts from the moment the sequence starts.

`getBreathingPreset(name)` returns a copy of one of the built-in patterns (`'box'`, `'relaxing'`, `'calming'`, `'energizing'`).

### Controls

| Function | |
| --- | --- |
| `startBreathingExercise(pattern)` | Starts, replacing a running sequence |
| `pauseBreathingExercise()` | |
| `resumeBreathingExercise()` | |
| `stopBreathingExercise()` | |

### Events

| Event | `nativeEvent` | |
| --- | --- | --- |
| `onPhaseChange` | `{ phase, label, phaseIndex, cycle }` | At the start of every phase |
| `onExerciseComplete` | `{ totalCycles, totalDuration }` | After the last cycle; never fires for a looping pattern |

### Phase view props

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

`example/` shows both views, a few timer patterns and the controls:

```sh
cd example && npm install && npx expo run:ios
```

The GIFs above come from `example/Showcase.tsx`. Start Metro with `EXPO_PUBLIC_SHOWCASE=orb` or `EXPO_PUBLIC_SHOWCASE=intervals`, open the app, and run `scripts/record-demo.sh <ios|android> <orb|intervals>` from `example/` (needs `ffmpeg` and `gifski`).

## Contributing and releases

`npm test`, `npx eslint .` and `npx tsc -p tsconfig.json --noEmit` run on every pull request. To release, bump `version` in `package.json`, add a line to [CHANGELOG.md](CHANGELOG.md), merge, and push a `v<version>` tag: the Publish workflow checks, builds and publishes to npm with provenance.

## License

MIT
