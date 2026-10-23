import type { BreathingPattern } from '../ExpoBreathingExercise.types';
import { getBreathingPreset, validateBreathingPattern } from '../breathing-pattern';

const valid = (): BreathingPattern => ({
  phases: [
    { phase: 'inhale', duration: 4000, targetScale: 1.35, label: 'In' },
    { phase: 'exhale', duration: 6000, targetScale: 0.75, label: 'Out' },
  ],
  cycles: 3,
});

describe('getBreathingPreset', () => {
  it.each(['relaxing', 'box', 'energizing', 'calming'] as const)(
    '%s is a valid pattern',
    (name) => {
      expect(() => validateBreathingPattern(getBreathingPreset(name))).not.toThrow();
    }
  );

  it('keeps the documented timings', () => {
    expect(getBreathingPreset('box').phases.map((p) => p.duration)).toEqual([
      4000, 4000, 4000, 4000,
    ]);
    expect(getBreathingPreset('relaxing').phases.map((p) => p.duration)).toEqual([
      4000, 7000, 8000,
    ]);
  });

  it('returns a copy, so editing it leaves the preset alone', () => {
    const box = getBreathingPreset('box');
    box.phases[0].duration = 1;
    box.cycles = 99;
    expect(getBreathingPreset('box').phases[0].duration).toBe(4000);
    expect(getBreathingPreset('box').cycles).toBe(4);
  });

  it('names the presets when given an unknown one', () => {
    expect(() => getBreathingPreset('nope' as never)).toThrow(/relaxing, box, energizing, calming/);
  });
});

describe('validateBreathingPattern', () => {
  it('accepts a pattern without cycles (loops) and with a start time', () => {
    const { cycles: _cycles, ...loop } = valid();
    expect(() => validateBreathingPattern(loop)).not.toThrow();
    expect(() => validateBreathingPattern({ ...valid(), startTime: Date.now() })).not.toThrow();
  });

  it.each<[string, (p: BreathingPattern) => unknown, RegExp]>([
    ['no phases', (p) => (p.phases = []), /at least one phase/],
    ['an unknown phase', (p) => ((p.phases[0] as any).phase = 'hold'), /phases\[0\]\.phase/],
    ['a zero duration', (p) => (p.phases[1].duration = 0), /phases\[1\]\.duration/],
    ['a NaN duration', (p) => (p.phases[0].duration = NaN), /phases\[0\]\.duration/],
    ['a negative scale', (p) => (p.phases[0].targetScale = -1), /targetScale/],
    ['a missing label', (p) => ((p.phases[0] as any).label = undefined), /label/],
    ['zero cycles', (p) => (p.cycles = 0), /cycles/],
    ['fractional cycles', (p) => (p.cycles = 2.5), /cycles/],
    ['a bad start time', (p) => (p.startTime = -5), /startTime/],
  ])('rejects %s', (_name, mutate, message) => {
    const pattern = valid();
    mutate(pattern);
    expect(() => validateBreathingPattern(pattern)).toThrow(message);
  });

  it('rejects something that is not a pattern', () => {
    expect(() => validateBreathingPattern(null as never)).toThrow(TypeError);
  });
});
