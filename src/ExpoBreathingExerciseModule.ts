import { NativeModule, requireNativeModule } from 'expo';
import type { BreathingPattern, BreathingPreset } from './ExpoBreathingExercise.types';

declare class ExpoBreathingExerciseModuleType extends NativeModule {
  startBreathingExercise(pattern: BreathingPattern): void;
  stopBreathingExercise(): void;
  pauseBreathingExercise(): void;
  resumeBreathingExercise(): void;
}

const module = requireNativeModule<ExpoBreathingExerciseModuleType>('ExpoBreathingExercise');

export default module;

export function startBreathingExercise(pattern: BreathingPattern): void {
  module.startBreathingExercise(pattern);
}

export function stopBreathingExercise(): void {
  module.stopBreathingExercise();
}

export function pauseBreathingExercise(): void {
  module.pauseBreathingExercise();
}

export function resumeBreathingExercise(): void {
  module.resumeBreathingExercise();
}

const BREATHING_PRESETS: Record<BreathingPreset, BreathingPattern> = {
  relaxing: {
    phases: [
      { phase: 'inhale', duration: 4000, targetScale: 1.35, label: 'Breathe In' },
      { phase: 'holdIn', duration: 7000, targetScale: 1.35, label: 'Hold' },
      { phase: 'exhale', duration: 8000, targetScale: 0.75, label: 'Breathe Out' },
    ],
    cycles: 4,
  },
  box: {
    phases: [
      { phase: 'inhale', duration: 4000, targetScale: 1.35, label: 'Breathe In' },
      { phase: 'holdIn', duration: 4000, targetScale: 1.35, label: 'Hold' },
      { phase: 'exhale', duration: 4000, targetScale: 0.75, label: 'Breathe Out' },
      { phase: 'holdOut', duration: 4000, targetScale: 0.75, label: 'Hold' },
    ],
    cycles: 4,
  },
  energizing: {
    phases: [
      { phase: 'inhale', duration: 2000, targetScale: 1.35, label: 'Breathe In' },
      { phase: 'exhale', duration: 2000, targetScale: 0.75, label: 'Breathe Out' },
    ],
    cycles: 10,
  },
  calming: {
    phases: [
      { phase: 'inhale', duration: 4000, targetScale: 1.35, label: 'Breathe In' },
      { phase: 'exhale', duration: 6000, targetScale: 0.75, label: 'Breathe Out' },
    ],
    cycles: 6,
  },
};

export function getBreathingPreset(preset: BreathingPreset): BreathingPattern {
  return BREATHING_PRESETS[preset];
}
