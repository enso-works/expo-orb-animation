import * as React from 'react';
import { SafeAreaView, Text, View, TouchableOpacity, StyleSheet } from 'react-native';

import {
  ExpoOrbView,
  setOrbActivity,
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  type BreathingPattern,
} from 'expo-orb';
import EdgeCaseTests from './EdgeCaseTests';
import Showcase, { type ShowcaseScene } from './Showcase';

const SHOWCASE_SCENE = process.env.EXPO_PUBLIC_SHOWCASE as ShowcaseScene | undefined;

// Memoized orb component - prevents re-renders from parent state changes
const MemoizedOrb = React.memo(() => (
  <ExpoOrbView
    style={styles.orb}
    backgroundColors={['#7c3aed', '#3b82f6', '#ec4899']}
    glowColor="#ffffff"
    particleColor="#ffffff"
    showBackground={true}
    showWavyBlobs={true}
    showParticles={true}
    showGlowEffects={true}
    showShadow={true}
  />
));

type Screen = 'orb' | 'timer' | 'tests';
type TimerPattern = 'intervals' | 'countdown' | 'focus';

// Short timings so each pattern is quick to try out.
const TIMER_PATTERNS: Record<TimerPattern, BreathingPattern> = {
  intervals: {
    phases: [
      { phase: 'inhale', duration: 4000, targetScale: 1.3, label: 'Sprint' },
      { phase: 'exhale', duration: 3000, targetScale: 0.8, label: 'Rest' },
    ],
    cycles: 4,
  },
  countdown: {
    phases: [
      { phase: 'holdOut', duration: 1000, targetScale: 0.8, label: '3' },
      { phase: 'holdOut', duration: 1000, targetScale: 0.9, label: '2' },
      { phase: 'holdOut', duration: 1000, targetScale: 1.0, label: '1' },
      { phase: 'inhale', duration: 800, targetScale: 1.3, label: 'Go' },
    ],
    cycles: 1,
  },
  focus: {
    phases: [
      { phase: 'inhale', duration: 6000, targetScale: 1.2, label: 'Focus' },
      { phase: 'holdIn', duration: 2000, targetScale: 1.2, label: 'Wrap up' },
      { phase: 'exhale', duration: 3000, targetScale: 0.85, label: 'Break' },
    ],
    cycles: 3,
  },
};

function OrbScreen() {
  const [isSpeaking, setIsSpeaking] = React.useState(false);

  React.useEffect(() => {
    let timeoutId: ReturnType<typeof setTimeout> | null = null;
    let cancelled = false;
    const idleActivity = 0.08;
    const speakingActivity = 0.85;

    const scheduleNext = () => {
      if (cancelled) {
        return;
      }

      const speaking = Math.random() > 0.4;
      const durationMs = speaking
        ? 1800 + Math.random() * 1400
        : 1400 + Math.random() * 1600;

      setIsSpeaking(speaking);
      setOrbActivity(speaking ? speakingActivity : idleActivity);

      timeoutId = setTimeout(scheduleNext, durationMs);
    };

    scheduleNext();

    return () => {
      cancelled = true;
      if (timeoutId != null) {
        clearTimeout(timeoutId);
      }
    };
  }, []);

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Orb</Text>
      <MemoizedOrb />
      <Text style={[styles.status, isSpeaking && styles.statusActive]}>
        {isSpeaking ? 'Speaking' : 'Idle'}
      </Text>
    </View>
  );
}

function TimerScreen() {
  const [isRunning, setIsRunning] = React.useState(false);
  const [currentPhase, setCurrentPhase] = React.useState('');
  const [selectedPattern, setSelectedPattern] = React.useState<TimerPattern>('intervals');

  const handleStart = () => {
    startBreathingExercise(TIMER_PATTERNS[selectedPattern]);
    setIsRunning(true);
  };

  const handleStop = () => {
    stopBreathingExercise();
    setIsRunning(false);
    setCurrentPhase('');
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Timer</Text>

      <ExpoBreathingExerciseView
        style={styles.phaseView}
        blobColors={['#f97316', '#ef4444', '#db2777']}
        showProgressRing={true}
        showTextCue={true}
        showInnerBlob={true}
        showShadow={true}
        onPhaseChange={(e) => setCurrentPhase(e.nativeEvent.label)}
        onExerciseComplete={() => {
          setIsRunning(false);
          setCurrentPhase('Complete!');
        }}
      />

      <View style={styles.presetRow}>
        {(Object.keys(TIMER_PATTERNS) as TimerPattern[]).map((pattern) => (
          <TouchableOpacity
            key={pattern}
            style={[
              styles.presetButton,
              selectedPattern === pattern && styles.presetButtonActive,
            ]}
            onPress={() => setSelectedPattern(pattern)}
          >
            <Text
              style={[
                styles.presetText,
                selectedPattern === pattern && styles.presetTextActive,
              ]}
            >
              {pattern}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <TouchableOpacity
        style={[styles.actionButton, isRunning && styles.actionButtonStop]}
        onPress={isRunning ? handleStop : handleStart}
      >
        <Text style={styles.actionButtonText}>
          {isRunning ? 'Stop' : 'Start'}
        </Text>
      </TouchableOpacity>

      {currentPhase ? (
        <Text style={styles.phaseText}>{currentPhase}</Text>
      ) : null}
    </View>
  );
}

export default function App() {
  const [screen, setScreen] = React.useState<Screen>('orb');

  if (SHOWCASE_SCENE === 'orb' || SHOWCASE_SCENE === 'intervals') {
    return <Showcase scene={SHOWCASE_SCENE} />;
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.tabBar}>
        <TouchableOpacity
          style={[styles.tab, screen === 'orb' && styles.tabActive]}
          onPress={() => setScreen('orb')}
          testID="tab-orb"
        >
          <Text style={[styles.tabText, screen === 'orb' && styles.tabTextActive]}>
            Orb
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, screen === 'timer' && styles.tabActive]}
          onPress={() => setScreen('timer')}
          testID="tab-timer"
        >
          <Text style={[styles.tabText, screen === 'timer' && styles.tabTextActive]}>
            Timer
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, screen === 'tests' && styles.tabActive]}
          onPress={() => setScreen('tests')}
          testID="tab-tests"
        >
          <Text style={[styles.tabText, screen === 'tests' && styles.tabTextActive]}>
            Tests
          </Text>
        </TouchableOpacity>
      </View>

      {screen === 'orb' && <OrbScreen />}
      {screen === 'timer' && <TimerScreen />}
      {screen === 'tests' && <EdgeCaseTests />}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0b0b0f',
    alignItems: 'center',
  },
  tabBar: {
    flexDirection: 'row',
    marginTop: 16,
    marginBottom: 24,
    backgroundColor: '#1e1e2e',
    borderRadius: 12,
    padding: 4,
  },
  tab: {
    paddingHorizontal: 24,
    paddingVertical: 10,
    borderRadius: 8,
  },
  tabActive: {
    backgroundColor: '#3b82f6',
  },
  tabText: {
    color: '#64748b',
    fontSize: 16,
    fontWeight: '600',
  },
  tabTextActive: {
    color: '#ffffff',
  },
  card: {
    width: '90%',
    alignItems: 'center',
    paddingVertical: 32,
    borderRadius: 20,
  },
  title: {
    color: '#f8fafc',
    fontSize: 22,
    marginBottom: 16,
  },
  orb: {
    width: 220,
    height: 220,
  },
  phaseView: {
    width: 280,
    height: 280,
  },
  status: {
    marginTop: 24,
    fontSize: 18,
    fontWeight: '600',
    color: '#64748b',
  },
  statusActive: {
    color: '#22c55e',
  },
  presetRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: 8,
    marginTop: 24,
  },
  presetButton: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 8,
    backgroundColor: '#1e1e2e',
  },
  presetButtonActive: {
    backgroundColor: '#3b82f6',
  },
  presetText: {
    color: '#64748b',
    fontSize: 14,
    fontWeight: '500',
    textTransform: 'capitalize',
  },
  presetTextActive: {
    color: '#ffffff',
  },
  actionButton: {
    marginTop: 20,
    paddingHorizontal: 32,
    paddingVertical: 12,
    borderRadius: 10,
    backgroundColor: '#22c55e',
  },
  actionButtonStop: {
    backgroundColor: '#ef4444',
  },
  actionButtonText: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '600',
  },
  phaseText: {
    marginTop: 16,
    color: '#94a3b8',
    fontSize: 16,
  },
});
