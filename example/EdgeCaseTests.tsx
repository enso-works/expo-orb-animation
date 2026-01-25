import * as React from 'react';
import { ScrollView, Text, View, TouchableOpacity, StyleSheet, Alert } from 'react-native';

import {
  ExpoOrbView,
  ExpoBreathingExerciseView,
  startBreathingExercise,
  stopBreathingExercise,
  pauseBreathingExercise,
  resumeBreathingExercise,
  getBreathingPreset,
} from 'expo-orb';

type TestResult = 'pending' | 'pass' | 'fail';

interface TestCase {
  id: string;
  ticket: string;
  name: string;
  description: string;
  run: () => void | Promise<void>;
}

export default function EdgeCaseTests() {
  const isDetox = Boolean((global as any).__detox__);
  const [results, setResults] = React.useState<Record<string, TestResult>>({});
  const [activeTest, setActiveTest] = React.useState<string | null>(null);
  const [orbColors, setOrbColors] = React.useState<string[]>(['#7c3aed', '#3b82f6', '#ec4899']);
  const [blobColors, setBlobColors] = React.useState<string[]>(['#06b6d4', '#3b82f6', '#8b5cf6']);

  const markResult = (id: string, result: TestResult) => {
    setResults((prev) => ({ ...prev, [id]: result }));
  };

  const tests: TestCase[] = [
    // AND-001: Guard gradient color arrays
    {
      id: 'and001-empty-orb',
      ticket: 'AND-001',
      name: 'Empty backgroundColors',
      description: 'Set orb backgroundColors to empty array - should not crash',
      run: () => {
        setActiveTest('and001-empty-orb');
        setOrbColors([]);
        setTimeout(() => {
          markResult('and001-empty-orb', 'pass');
          setOrbColors(['#7c3aed', '#3b82f6', '#ec4899']);
        }, 1000);
      },
    },
    {
      id: 'and001-single-orb',
      ticket: 'AND-001',
      name: 'Single backgroundColors',
      description: 'Set orb backgroundColors to single color - should not crash',
      run: () => {
        setActiveTest('and001-single-orb');
        setOrbColors(['#ff0000']);
        setTimeout(() => {
          markResult('and001-single-orb', 'pass');
          setOrbColors(['#7c3aed', '#3b82f6', '#ec4899']);
        }, 1000);
      },
    },
    {
      id: 'and001-empty-blob',
      ticket: 'AND-001',
      name: 'Empty blobColors',
      description: 'Set breathing blobColors to empty array - should not crash',
      run: () => {
        setActiveTest('and001-empty-blob');
        setBlobColors([]);
        setTimeout(() => {
          markResult('and001-empty-blob', 'pass');
          setBlobColors(['#06b6d4', '#3b82f6', '#8b5cf6']);
        }, 1000);
      },
    },
    {
      id: 'and001-single-blob',
      ticket: 'AND-001',
      name: 'Single blobColors',
      description: 'Set breathing blobColors to single color - should not crash',
      run: () => {
        setActiveTest('and001-single-blob');
        setBlobColors(['#00ff00']);
        setTimeout(() => {
          markResult('and001-single-blob', 'pass');
          setBlobColors(['#06b6d4', '#3b82f6', '#8b5cf6']);
        }, 1000);
      },
    },

    // AND-002: Main-thread breathing state updates
    {
      id: 'and002-rapid-toggle',
      ticket: 'AND-002',
      name: 'Rapid start/stop',
      description: 'Rapidly toggle breathing exercise - should not crash',
      run: async () => {
        setActiveTest('and002-rapid-toggle');
        const pattern = getBreathingPreset('box');

        for (let i = 0; i < 10; i++) {
          startBreathingExercise(pattern);
          await new Promise((r) => setTimeout(r, 50));
          stopBreathingExercise();
          await new Promise((r) => setTimeout(r, 50));
        }

        markResult('and002-rapid-toggle', 'pass');
      },
    },
    {
      id: 'and002-rapid-pause-resume',
      ticket: 'AND-002',
      name: 'Rapid pause/resume',
      description: 'Rapidly pause and resume breathing - should not crash',
      run: async () => {
        setActiveTest('and002-rapid-pause-resume');
        const pattern = getBreathingPreset('box');

        startBreathingExercise(pattern);
        await new Promise((r) => setTimeout(r, 100));

        for (let i = 0; i < 20; i++) {
          pauseBreathingExercise();
          await new Promise((r) => setTimeout(r, 30));
          resumeBreathingExercise();
          await new Promise((r) => setTimeout(r, 30));
        }

        stopBreathingExercise();
        markResult('and002-rapid-pause-resume', 'pass');
      },
    },

    // AND-003: Safe color parsing
    {
      id: 'and003-invalid-color',
      ticket: 'AND-003',
      name: 'Invalid color string',
      description: 'Pass invalid color string - should not crash',
      run: () => {
        setActiveTest('and003-invalid-color');
        // TypeScript prevents this at compile time, but native code should handle it
        // Cast to any to bypass TS checks for this test
        setOrbColors(['#notAColor', 'invalidHex', 'rgb(300,300,300)'] as any);
        setTimeout(() => {
          markResult('and003-invalid-color', 'pass');
          setOrbColors(['#7c3aed', '#3b82f6', '#ec4899']);
        }, 1000);
      },
    },
  ];

  const allPassed = tests.length > 0 && tests.every((test) => results[test.id] === 'pass');

  const runAllTests = async () => {
    setResults({});
    for (const test of tests) {
      try {
        await test.run();
        await new Promise((r) => setTimeout(r, 500));
      } catch (error) {
        markResult(test.id, 'fail');
        if (!isDetox) {
          Alert.alert('Test Failed', `${test.name}: ${error}`);
        }
      }
    }
    setActiveTest(null);
    if (!isDetox) {
      Alert.alert('Tests Complete', 'All edge case tests finished');
    }
  };

  const getResultColor = (result: TestResult) => {
    switch (result) {
      case 'pass':
        return '#22c55e';
      case 'fail':
        return '#ef4444';
      default:
        return '#64748b';
    }
  };

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      testID="edge-case-scroll"
    >
      <Text style={styles.title}>Edge Case Tests</Text>
      <Text style={styles.subtitle}>Tests for tickets AND-001, AND-002, AND-003</Text>
      {allPassed ? (
        <Text style={styles.allPassed} testID="all-tests-passed">
          All tests passed
        </Text>
      ) : null}

      <View style={styles.viewContainer}>
        <View style={styles.viewRow}>
          <View style={styles.viewWrapper}>
            <Text style={styles.viewLabel}>Orb</Text>
            <ExpoOrbView
              style={styles.smallView}
              backgroundColors={orbColors}
              showBackground={true}
              showWavyBlobs={true}
              showParticles={false}
              showGlowEffects={false}
              showShadow={false}
            />
          </View>
          <View style={styles.viewWrapper}>
            <Text style={styles.viewLabel}>Breathing</Text>
            <ExpoBreathingExerciseView
              style={styles.smallView}
              blobColors={blobColors}
              showProgressRing={false}
              showTextCue={false}
              showParticles={false}
              showGlowEffects={false}
              showShadow={false}
            />
          </View>
        </View>
      </View>

      <TouchableOpacity style={styles.runAllButton} onPress={runAllTests} testID="run-all-tests">
        <Text style={styles.runAllText}>Run All Tests</Text>
      </TouchableOpacity>

      {tests.map((test) => (
        <View
          key={test.id}
          style={[styles.testCard, activeTest === test.id && styles.testCardActive]}
          testID={`test-card-${test.id}`}
        >
          <View style={styles.testHeader}>
            <Text style={styles.ticket}>{test.ticket}</Text>
            <View
              style={[styles.resultBadge, { backgroundColor: getResultColor(results[test.id]) }]}
            >
              <Text style={styles.resultText} testID={`result-${test.id}`}>
                {results[test.id] === 'pass'
                  ? 'PASS'
                  : results[test.id] === 'fail'
                    ? 'FAIL'
                    : 'PENDING'}
              </Text>
            </View>
          </View>
          <Text style={styles.testName}>{test.name}</Text>
          <Text style={styles.testDesc}>{test.description}</Text>
          <TouchableOpacity
            style={styles.runButton}
            onPress={() => test.run()}
            disabled={activeTest !== null}
            testID={`run-test-${test.id}`}
          >
            <Text style={styles.runButtonText}>Run Test</Text>
          </TouchableOpacity>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0b0b0f',
  },
  content: {
    padding: 16,
    paddingBottom: 40,
  },
  title: {
    color: '#f8fafc',
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 4,
  },
  subtitle: {
    color: '#64748b',
    fontSize: 14,
    textAlign: 'center',
    marginBottom: 20,
  },
  allPassed: {
    color: '#22c55e',
    fontSize: 14,
    textAlign: 'center',
    marginBottom: 16,
  },
  viewContainer: {
    marginBottom: 20,
  },
  viewRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  viewWrapper: {
    alignItems: 'center',
  },
  viewLabel: {
    color: '#94a3b8',
    fontSize: 12,
    marginBottom: 8,
  },
  smallView: {
    width: 120,
    height: 120,
  },
  runAllButton: {
    backgroundColor: '#3b82f6',
    padding: 16,
    borderRadius: 12,
    marginBottom: 20,
  },
  runAllText: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '600',
    textAlign: 'center',
  },
  testCard: {
    backgroundColor: '#1e1e2e',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  testCardActive: {
    borderWidth: 2,
    borderColor: '#3b82f6',
  },
  testHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  ticket: {
    color: '#3b82f6',
    fontSize: 12,
    fontWeight: '600',
  },
  resultBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  resultText: {
    color: '#ffffff',
    fontSize: 10,
    fontWeight: 'bold',
  },
  testName: {
    color: '#f8fafc',
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
  },
  testDesc: {
    color: '#94a3b8',
    fontSize: 14,
    marginBottom: 12,
  },
  runButton: {
    backgroundColor: '#334155',
    padding: 10,
    borderRadius: 8,
  },
  runButtonText: {
    color: '#f8fafc',
    fontSize: 14,
    fontWeight: '500',
    textAlign: 'center',
  },
});
