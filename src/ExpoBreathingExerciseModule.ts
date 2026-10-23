import { NativeModule, requireNativeModule } from 'expo';

import type { BreathingPattern } from './ExpoBreathingExercise.types';
import { validateBreathingPattern } from './breathing-pattern';

declare class ExpoBreathingExerciseModuleType extends NativeModule {
  startBreathingExercise(pattern: BreathingPattern): void;
  stopBreathingExercise(): void;
  pauseBreathingExercise(): void;
  resumeBreathingExercise(): void;
}

const module = requireNativeModule<ExpoBreathingExerciseModuleType>('ExpoBreathingExercise');

export default module;

/** Throws a TypeError for an invalid pattern instead of letting native ignore it. */
export function startBreathingExercise(pattern: BreathingPattern): void {
  validateBreathingPattern(pattern);
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

export { getBreathingPreset } from './breathing-pattern';
