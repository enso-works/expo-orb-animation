import { SafeAreaView, Text, View } from 'react-native';

import { ExpoIosOrbView } from 'expo-ios-orb';

export default function App() {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.title}>Orb</Text>
        <ExpoIosOrbView
          style={styles.orb}
         backgroundColors={['#7c3aed', '#3b82f6', '#ec4899']}
         glowColor="#ffffff"
          particleColor="#ffffff"
          coreGlowIntensity={1.2}
          showBackground={true}
          showWavyBlobs={true}
          showParticles={true}
          showGlowEffects={true}
          showShadow={true}
          speed={60}
        />
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
};
