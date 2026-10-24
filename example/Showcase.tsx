import * as React from 'react';
import { Animated, Easing, StatusBar, StyleSheet, View } from 'react-native';

import {
  ExpoOrbView,
  setOrbActivity,
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  type BreathingPattern,
} from 'expo-orb';

// Scripted, deterministic scenes used to record the README GIFs.
// Start Metro with EXPO_PUBLIC_SHOWCASE=orb or EXPO_PUBLIC_SHOWCASE=intervals.

export type ShowcaseScene = 'orb' | 'intervals';

const BACKGROUND = '#050507';

const THEMES = [
  { name: 'violet', colors: ['#7c3aed', '#3b82f6', '#ec4899'], accent: '#c4b5fd' },
  { name: 'aurora', colors: ['#10b981', '#06b6d4', '#3b82f6'], accent: '#67e8f9' },
  { name: 'sunset', colors: ['#f59e0b', '#ef4444', '#ec4899'], accent: '#fdba74' },
];

// One theme segment, in ms from its start. The orb blooms in, talks in two
// bursts and fades out, so the last frame of the loop matches the first.
const SEGMENT_MS = 4600;
const ACTIVITY_SCRIPT: [number, number][] = [
  [0, 0.05],
  [900, 0.9],
  [2300, 0.3],
  [2700, 1],
  [3500, 0.08],
];
const BLOOM_MS = 700;
const FADE_OUT_AT = SEGMENT_MS - BLOOM_MS;

const OrbScene = () => {
  const [themeIndex, setThemeIndex] = React.useState(0);
  const presence = React.useRef(new Animated.Value(0)).current;

  React.useEffect(() => {
    const timers = ACTIVITY_SCRIPT.map(([at, activity]) =>
      setTimeout(() => setOrbActivity(activity), at)
    );

    Animated.timing(presence, {
      toValue: 1,
      duration: BLOOM_MS,
      easing: Easing.out(Easing.cubic),
      useNativeDriver: true,
    }).start();

    timers.push(
      setTimeout(() => {
        Animated.timing(presence, {
          toValue: 0,
          duration: BLOOM_MS,
          easing: Easing.in(Easing.cubic),
          useNativeDriver: true,
        }).start();
      }, FADE_OUT_AT),
      setTimeout(() => setThemeIndex((index) => (index + 1) % THEMES.length), SEGMENT_MS)
    );

    return () => timers.forEach(clearTimeout);
  }, [themeIndex, presence]);

  const theme = THEMES[themeIndex];
  const scale = presence.interpolate({ inputRange: [0, 1], outputRange: [0.82, 1] });

  return (
    <Animated.View style={{ opacity: presence, transform: [{ scale }] }}>
      <ExpoOrbView
        style={styles.orb}
        backgroundColors={theme.colors}
        glowColor="#ffffff"
        particleColor={theme.accent}
        coreGlowIntensity={1.1}
        showBackground
        showWavyBlobs
        showParticles
        showGlowEffects
        showShadow
      />
    </Animated.View>
  );
};

// The phase engine is not tied to breathing: here it drives an interval
// workout timer. One round starts and ends at the rest scale, so the GIF loops.
const INTERVAL_PATTERN: BreathingPattern = {
  phases: [
    { phase: 'inhale', duration: 2500, targetScale: 1.3, label: 'Sprint' },
    { phase: 'holdIn', duration: 2000, targetScale: 1.1, label: 'Jog' },
    { phase: 'exhale', duration: 2500, targetScale: 0.8, label: 'Rest' },
  ],
};

const IntervalsScene = () => {
  React.useEffect(() => {
    startBreathingExercise(INTERVAL_PATTERN);
    return () => stopBreathingExercise();
  }, []);

  return (
    <ExpoBreathingExerciseView
      style={styles.intervals}
      blobColors={['#f97316', '#ef4444', '#db2777']}
      glowColor="#fb923c"
      particleColor="#fed7aa"
      progressRingColor="#fff7ed"
      textColor="#ffffff"
      showProgressRing
      showTextCue
      showInnerBlob
      showShadow
      showParticles
      showWavyBlobs
      showGlowEffects
      wobbleIntensity={0.7}
    />
  );
};

export default function Showcase({ scene }: { scene: ShowcaseScene }) {
  return (
    <View style={styles.container}>
      <StatusBar hidden />
      {scene === 'orb' ? <OrbScene /> : <IntervalsScene />}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: BACKGROUND,
    alignItems: 'center',
    justifyContent: 'center',
  },
  orb: {
    width: 300,
    height: 300,
  },
  intervals: {
    width: 340,
    height: 340,
  },
});
