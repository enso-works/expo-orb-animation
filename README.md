# expo-orb

An animated orb component for React Native with Expo. Renders a glowing, animated sphere with particles, wavy blobs, and customizable visual effects.

Inspired by and iOS implementation from [metasidd/Orb](https://github.com/metasidd/Orb)

## Demo

| iOS | Android |
|:---:|:-------:|
| ![iOS Demo](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/demo-ios.gif) | ![Android Demo](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/demo-android.gif) |

## Features

- Smooth 60fps animations
- Activity-based animation states (idle, speaking, etc.)
- Breathing animation effect
- Customizable colors, particles, and glow effects
- Native performance on iOS (SwiftUI/SpriteKit) and Android (Jetpack Compose)

## Installation

```bash
npm install expo-orb
```

For bare React Native projects, run `npx pod-install` after installation.

## Usage

```tsx
import { ExpoOrbView, setOrbActivity } from 'expo-orb';

function MyComponent() {
  return (
    <ExpoOrbView
      style={{ width: 200, height: 200 }}
      backgroundColors={['#7c3aed', '#3b82f6', '#ec4899']}
      glowColor="#ffffff"
      particleColor="#ffffff"
      showBackground={true}
      showWavyBlobs={true}
      showParticles={true}
      showGlowEffects={true}
      showShadow={true}
    />
  );
}

// Control animation intensity (0-1)
setOrbActivity(0.8); // Active/speaking state
setOrbActivity(0.1); // Idle state
```

## Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `backgroundColors` | `ColorValue[]` | `['green', 'blue', 'pink']` | Gradient colors for the orb background (min 2 colors) |
| `glowColor` | `ColorValue` | `'white'` | Color of the glow effects |
| `particleColor` | `ColorValue` | `'white'` | Color of floating particles |
| `coreGlowIntensity` | `number` | `1.0` | Intensity of the core glow effect (0+) |
| `breathingIntensity` | `number` | `0` | Intensity of breathing animation (0-1) |
| `breathingSpeed` | `number` | `0.25` | Speed of breathing animation |
| `showBackground` | `boolean` | `true` | Show/hide background gradient |
| `showWavyBlobs` | `boolean` | `true` | Show/hide animated wavy blob overlays |
| `showParticles` | `boolean` | `true` | Show/hide floating particles |
| `showGlowEffects` | `boolean` | `true` | Show/hide rotating glow effects |
| `showShadow` | `boolean` | `true` | Show/hide drop shadow |
| `speed` | `number` | `60` | Animation speed multiplier |
| `style` | `StyleProp<ViewStyle>` | - | Container style (set width/height here) |

## Methods

### `setOrbActivity(activity: number)`

Controls the orb's animation intensity. Pass a value between 0 and 1:

- `0` - Minimal activity (idle)
- `0.1-0.3` - Low activity
- `0.5-0.7` - Medium activity
- `0.8-1.0` - High activity (speaking/active)

This function bypasses React's prop system to prevent animation flickering during rapid updates.

```tsx
import { setOrbActivity } from 'expo-orb';

// Transition to speaking state
setOrbActivity(0.85);

// Return to idle
setOrbActivity(0.08);
```

## Example

See the `/example` directory for a complete working example with activity simulation.

```tsx
import * as React from 'react';
import { View } from 'react-native';
import { ExpoOrbView, setOrbActivity } from 'expo-orb';

export default function App() {
  React.useEffect(() => {
    // Simulate activity changes
    const interval = setInterval(() => {
      const activity = Math.random() > 0.5 ? 0.85 : 0.08;
      setOrbActivity(activity);
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <ExpoOrbView
        style={{ width: 220, height: 220 }}
        backgroundColors={['#7c3aed', '#3b82f6', '#ec4899']}
      />
    </View>
  );
}
```

---

## Breathing Exercise Component

A guided breathing exercise component with morphing blob animation, progress ring, and text cues. Supports custom breathing patterns and built-in presets.

### Breathing Demo

| iOS | Android |
|:---:|:-------:|
| ![iOS Breathing Demo](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/breathing-ios.gif) | ![Android Breathing Demo](https://raw.githubusercontent.com/enso-works/expo-orb-animation/main/docs/breathing-android.gif) |

### Basic Usage

```tsx
import {
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  getBreathingPreset,
} from 'expo-orb';

function BreathingScreen() {
  const handleStart = () => {
    // Use a built-in preset
    startBreathingExercise(getBreathingPreset('box'));
  };

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <ExpoBreathingExerciseView
        style={{ width: 300, height: 300 }}
        blobColors={['#7c3aed', '#3b82f6', '#ec4899']}
        showProgressRing={true}
        showTextCue={true}
        onPhaseChange={(e) => console.log('Phase:', e.nativeEvent.label)}
        onExerciseComplete={(e) => console.log('Done! Cycles:', e.nativeEvent.totalCycles)}
      />
      <Button title="Start" onPress={handleStart} />
      <Button title="Stop" onPress={stopBreathingExercise} />
    </View>
  );
}
```

### Breathing Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `blobColors` | `ColorValue[]` | `['green', 'blue', 'pink']` | Gradient colors for the morphing blob |
| `innerBlobColor` | `ColorValue` | `'white'` | Color of the inner blob |
| `glowColor` | `ColorValue` | `'white'` | Color of the glow effects |
| `particleColor` | `ColorValue` | `'white'` | Color of floating particles |
| `progressRingColor` | `ColorValue` | `'white'` | Color of the progress ring |
| `textColor` | `ColorValue` | `'white'` | Color of the instruction text |
| `showProgressRing` | `boolean` | `true` | Show/hide the progress ring |
| `showTextCue` | `boolean` | `true` | Show/hide the instruction text (e.g., "Breathe In") |
| `showInnerBlob` | `boolean` | `true` | Show/hide the inner blob |
| `showShadow` | `boolean` | `true` | Show/hide drop shadow |
| `showParticles` | `boolean` | `true` | Show/hide floating particles |
| `showWavyBlobs` | `boolean` | `true` | Show/hide wavy blob overlays |
| `showGlowEffects` | `boolean` | `true` | Show/hide rotating glow effects |
| `pointCount` | `number` | `8` | Number of points for blob morphing |
| `wobbleIntensity` | `number` | `0.5` | Intensity of wobble animation (0-1) |
| `style` | `StyleProp<ViewStyle>` | - | Container style (set width/height here) |

### Breathing Events

| Event | Payload | Description |
|-------|---------|-------------|
| `onPhaseChange` | `{ phase, label, phaseIndex, cycle }` | Fired when the breathing phase changes |
| `onExerciseComplete` | `{ totalCycles, totalDuration }` | Fired when all cycles complete |

### Breathing Control Functions

```tsx
import {
  startBreathingExercise,
  stopBreathingExercise,
  pauseBreathingExercise,
  resumeBreathingExercise,
  getBreathingPreset,
} from 'expo-orb';
```

| Function | Description |
|----------|-------------|
| `startBreathingExercise(pattern)` | Start the exercise with a custom pattern or preset |
| `stopBreathingExercise()` | Stop the current exercise |
| `pauseBreathingExercise()` | Pause the current exercise |
| `resumeBreathingExercise()` | Resume a paused exercise |
| `getBreathingPreset(name)` | Get a built-in preset by name |

### Built-in Presets

| Preset | Pattern | Cycles | Description |
|--------|---------|--------|-------------|
| `'relaxing'` | 4s in, 7s hold, 8s out | 4 | 4-7-8 technique for relaxation |
| `'box'` | 4s in, 4s hold, 4s out, 4s hold | 4 | Box breathing for focus |
| `'energizing'` | 2s in, 2s out | 10 | Quick breathing for energy |
| `'calming'` | 4s in, 6s out | 6 | Extended exhale for calm |

### Custom Breathing Patterns

Create your own breathing patterns:

```tsx
import { startBreathingExercise, BreathingPattern } from 'expo-orb';

const customPattern: BreathingPattern = {
  phases: [
    { phase: 'inhale', duration: 5000, targetScale: 1.35, label: 'Breathe In' },
    { phase: 'holdIn', duration: 3000, targetScale: 1.35, label: 'Hold' },
    { phase: 'exhale', duration: 7000, targetScale: 0.75, label: 'Breathe Out' },
    { phase: 'holdOut', duration: 2000, targetScale: 0.75, label: 'Rest' },
  ],
  cycles: 5, // undefined for infinite
};

startBreathingExercise(customPattern);
```

### Phase Types

| Phase | Description |
|-------|-------------|
| `'inhale'` | Breathing in - blob expands |
| `'holdIn'` | Holding breath after inhale |
| `'exhale'` | Breathing out - blob contracts |
| `'holdOut'` | Holding breath after exhale |

### Complete Breathing Example

```tsx
import * as React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import {
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  pauseBreathingExercise,
  resumeBreathingExercise,
  getBreathingPreset,
  BreathingPreset,
} from 'expo-orb';

export default function BreathingApp() {
  const [currentPhase, setCurrentPhase] = React.useState('');
  const [isPaused, setIsPaused] = React.useState(false);

  const startPreset = (preset: BreathingPreset) => {
    setIsPaused(false);
    startBreathingExercise(getBreathingPreset(preset));
  };

  return (
    <View style={styles.container}>
      <ExpoBreathingExerciseView
        style={styles.breathingView}
        blobColors={['#667eea', '#764ba2', '#f093fb']}
        progressRingColor="#ffffff"
        textColor="#ffffff"
        showProgressRing={true}
        showTextCue={true}
        showParticles={true}
        onPhaseChange={(e) => setCurrentPhase(e.nativeEvent.label)}
        onExerciseComplete={() => setCurrentPhase('Complete!')}
      />

      <View style={styles.presets}>
        <TouchableOpacity onPress={() => startPreset('box')}>
          <Text>Box</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => startPreset('relaxing')}>
          <Text>Relaxing</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => startPreset('calming')}>
          <Text>Calming</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.controls}>
        <TouchableOpacity onPress={() => {
          if (isPaused) {
            resumeBreathingExercise();
          } else {
            pauseBreathingExercise();
          }
          setIsPaused(!isPaused);
        }}>
          <Text>{isPaused ? 'Resume' : 'Pause'}</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={stopBreathingExercise}>
          <Text>Stop</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  breathingView: { width: 280, height: 280 },
  presets: { flexDirection: 'row', gap: 16, marginTop: 32 },
  controls: { flexDirection: 'row', gap: 24, marginTop: 16 },
});
```

## License

MIT
