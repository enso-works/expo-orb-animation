import * as React from 'react';
import { SafeAreaView, Text, View, TouchableOpacity, StyleSheet } from 'react-native';

import {
  ExpoOrbView,
  setOrbActivity,
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  getBreathingPreset,
} from 'expo-orb';

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

type Screen = 'orb' | 'breathing';
type BreathingPreset = 'box' | 'relaxing' | 'energizing' | 'calming';

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

function BreathingScreen() {
  const [isRunning, setIsRunning] = React.useState(false);
  const [currentPhase, setCurrentPhase] = React.useState('');
  const [selectedPreset, setSelectedPreset] = React.useState<BreathingPreset>('box');

  const handleStart = () => {
    const pattern = getBreathingPreset(selectedPreset);
    startBreathingExercise(pattern);
    setIsRunning(true);
  };

  const handleStop = () => {
    stopBreathingExercise();
    setIsRunning(false);
    setCurrentPhase('');
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Breathing Exercise</Text>

      <ExpoBreathingExerciseView
        style={styles.breathingView}
        blobColors={['#06b6d4', '#3b82f6', '#8b5cf6']}
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
        {(['box', 'relaxing', 'energizing', 'calming'] as BreathingPreset[]).map((preset) => (
          <TouchableOpacity
            key={preset}
            style={[
              styles.presetButton,
              selectedPreset === preset && styles.presetButtonActive,
            ]}
            onPress={() => setSelectedPreset(preset)}
          >
            <Text
              style={[
                styles.presetText,
                selectedPreset === preset && styles.presetTextActive,
              ]}
            >
              {preset}
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

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.tabBar}>
        <TouchableOpacity
          style={[styles.tab, screen === 'orb' && styles.tabActive]}
          onPress={() => setScreen('orb')}
        >
          <Text style={[styles.tabText, screen === 'orb' && styles.tabTextActive]}>
            Orb
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, screen === 'breathing' && styles.tabActive]}
          onPress={() => setScreen('breathing')}
        >
          <Text style={[styles.tabText, screen === 'breathing' && styles.tabTextActive]}>
            Breathing
          </Text>
        </TouchableOpacity>
      </View>

      {screen === 'orb' ? <OrbScreen /> : <BreathingScreen />}
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
  breathingView: {
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
