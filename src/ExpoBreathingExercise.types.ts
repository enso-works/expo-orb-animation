import type { ColorValue, StyleProp, ViewStyle } from 'react-native';

export type BreathPhase = 'inhale' | 'holdIn' | 'exhale' | 'holdOut';

export interface BreathPhaseConfig {
  phase: BreathPhase;
  duration: number;      // milliseconds
  targetScale: number;   // e.g., 1.0 to 1.3
  label: string;         // "Breathe In"
}

export interface BreathingPattern {
  phases: BreathPhaseConfig[];
  cycles?: number;       // undefined = infinite
}

export type BreathingPreset = 'relaxing' | 'box' | 'energizing' | 'calming';

export interface PhaseChangeEvent {
  phase: BreathPhase;
  label: string;
  phaseIndex: number;
  cycle: number;
}

export interface ExerciseCompleteEvent {
  totalCycles: number;
  totalDuration: number;
}

export interface ExpoBreathingExerciseViewProps {
  blobColors?: ColorValue[];
  innerBlobColor?: ColorValue;
  glowColor?: ColorValue;
  particleColor?: ColorValue;
  progressRingColor?: ColorValue;
  textColor?: ColorValue;
  showProgressRing?: boolean;
  showTextCue?: boolean;
  showInnerBlob?: boolean;
  showShadow?: boolean;
  showParticles?: boolean;
  showWavyBlobs?: boolean;
  showGlowEffects?: boolean;
  pointCount?: number;           // Morphing points (default: 8)
  wobbleIntensity?: number;      // 0-1
  onPhaseChange?: (event: { nativeEvent: PhaseChangeEvent }) => void;
  onExerciseComplete?: (event: { nativeEvent: ExerciseCompleteEvent }) => void;
  style?: StyleProp<ViewStyle>;
}
