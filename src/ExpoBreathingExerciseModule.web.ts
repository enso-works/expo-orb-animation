import { registerWebModule, NativeModule } from 'expo';

import type { BreathingPattern } from './ExpoBreathingExercise.types';
import { validateBreathingPattern } from './breathing-pattern';

// The breathing exercise is native only. On web these keep the import from
// throwing and do nothing, so a shared screen still renders.
class ExpoBreathingExerciseModule extends NativeModule {
  startBreathingExercise(_pattern: BreathingPattern): void {}
  stopBreathingExercise(): void {}
  pauseBreathingExercise(): void {}
  resumeBreathingExercise(): void {}
}

const module = registerWebModule(ExpoBreathingExerciseModule, 'ExpoBreathingExercise');

export default module;

export function startBreathingExercise(pattern: BreathingPattern): void {
  validateBreathingPattern(pattern);
}

export function stopBreathingExercise(): void {}

export function pauseBreathingExercise(): void {}

export function resumeBreathingExercise(): void {}

export { getBreathingPreset } from './breathing-pattern';
