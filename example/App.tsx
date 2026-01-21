import * as React from 'react';
import { SafeAreaView, Text, View } from 'react-native';

import { ExpoOrbView, setOrbActivity } from 'expo-orb';

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

export default function App() {
  // Only track activity for UI state (Speaking/Idle text), not for the orb animation
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
    <SafeAreaView style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.title}>Orb</Text>
        <MemoizedOrb />
        <Text style={[styles.status, isSpeaking && styles.statusActive]}>
          {isSpeaking ? 'Speaking' : 'Idle'}
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = {
  container: {
    flex: 1,
    backgroundColor: '#0b0b0f',
    alignItems: 'center',
    justifyContent: 'center',
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
  status: {
    marginTop: 24,
    fontSize: 18,
    fontWeight: '600' as const,
    color: '#64748b',
  },
  statusActive: {
    color: '#22c55e',
  },
};
