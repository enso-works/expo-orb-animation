import type { BreathingPattern, BreathingPreset, BreathPhase } from './ExpoBreathingExercise.types';

// Pure JS, no native imports, so it can be tested and shared by every platform.

const PHASES: readonly BreathPhase[] = ['inhale', 'holdIn', 'exhale', 'holdOut'];

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

/**
 * Returns a copy of a built-in preset, so changing it never changes the next
 * call's result.
 */
export function getBreathingPreset(preset: BreathingPreset): BreathingPattern {
  const pattern = BREATHING_PRESETS[preset];
  if (!pattern) {
    throw new TypeError(
      `expo-orb: unknown breathing preset "${String(preset)}". Use one of: ${Object.keys(BREATHING_PRESETS).join(', ')}.`
    );
  }
  return { ...pattern, phases: pattern.phases.map((phase) => ({ ...phase })) };
}

const isPositive = (value: unknown): value is number =>
  typeof value === 'number' && Number.isFinite(value) && value > 0;

/**
 * Throws a TypeError naming the first problem in a pattern. The native side
 * used to drop a bad phase or the whole pattern without a word; a zero
 * duration also produced NaN progress on iOS.
 */
export function validateBreathingPattern(pattern: BreathingPattern): void {
  const fail = (message: string): never => {
    throw new TypeError(`expo-orb: ${message}`);
  };

  if (!pattern || typeof pattern !== 'object') fail('the breathing pattern must be an object.');
  if (!Array.isArray(pattern.phases) || pattern.phases.length === 0) {
    fail('a breathing pattern needs at least one phase.');
  }

  pattern.phases.forEach((phase, index) => {
    const at = `phases[${index}]`;
    if (!phase || typeof phase !== 'object') fail(`${at} must be an object.`);
    if (!PHASES.includes(phase.phase)) {
      fail(`${at}.phase is "${String(phase.phase)}", expected one of ${PHASES.join(', ')}.`);
    }
    if (!isPositive(phase.duration)) {
      fail(
        `${at}.duration must be a number of milliseconds above 0, got ${String(phase.duration)}.`
      );
    }
    if (!isPositive(phase.targetScale)) {
      fail(`${at}.targetScale must be a number above 0, got ${String(phase.targetScale)}.`);
    }
    if (typeof phase.label !== 'string') fail(`${at}.label must be a string.`);
  });

  if (pattern.cycles !== undefined && !(Number.isInteger(pattern.cycles) && pattern.cycles > 0)) {
    fail(
      `cycles must be a whole number above 0, or left out to loop, got ${String(pattern.cycles)}.`
    );
  }
  if (pattern.startTime !== undefined && !isPositive(pattern.startTime)) {
    fail(`startTime must be epoch milliseconds, got ${String(pattern.startTime)}.`);
  }
}
