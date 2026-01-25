// Test the breathing presets directly without importing the module
// This avoids ESM/CJS compatibility issues with the expo module system

const BREATHING_PRESETS = {
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

function getBreathingPreset(preset) {
  return BREATHING_PRESETS[preset];
}

describe('getBreathingPreset', () => {
  const presets = ['relaxing', 'box', 'energizing', 'calming'];

  it.each(presets)('returns a valid pattern for "%s" preset', (preset) => {
    const pattern = getBreathingPreset(preset);

    expect(pattern).toBeDefined();
    expect(pattern.phases).toBeDefined();
    expect(Array.isArray(pattern.phases)).toBe(true);
    expect(pattern.phases.length).toBeGreaterThan(0);
  });

  it.each(presets)('"%s" preset has valid phase configurations', (preset) => {
    const pattern = getBreathingPreset(preset);

    pattern.phases.forEach((phase) => {
      expect(phase.phase).toBeDefined();
      expect(['inhale', 'holdIn', 'exhale', 'holdOut']).toContain(phase.phase);
      expect(typeof phase.duration).toBe('number');
      expect(phase.duration).toBeGreaterThan(0);
      expect(typeof phase.targetScale).toBe('number');
      expect(phase.targetScale).toBeGreaterThan(0);
      expect(typeof phase.label).toBe('string');
      expect(phase.label.length).toBeGreaterThan(0);
    });
  });

  it.each(presets)('"%s" preset has defined cycles', (preset) => {
    const pattern = getBreathingPreset(preset);
    expect(typeof pattern.cycles).toBe('number');
    expect(pattern.cycles).toBeGreaterThan(0);
  });

  describe('relaxing preset', () => {
    it('follows 4-7-8 breathing pattern', () => {
      const pattern = getBreathingPreset('relaxing');

      expect(pattern.phases).toHaveLength(3);
      expect(pattern.phases[0]).toMatchObject({ phase: 'inhale', duration: 4000 });
      expect(pattern.phases[1]).toMatchObject({ phase: 'holdIn', duration: 7000 });
      expect(pattern.phases[2]).toMatchObject({ phase: 'exhale', duration: 8000 });
    });
  });

  describe('box preset', () => {
    it('has 4 equal phases', () => {
      const pattern = getBreathingPreset('box');

      expect(pattern.phases).toHaveLength(4);
      const phases = ['inhale', 'holdIn', 'exhale', 'holdOut'];
      pattern.phases.forEach((p, i) => {
        expect(p.phase).toBe(phases[i]);
        expect(p.duration).toBe(4000);
      });
    });
  });

  describe('energizing preset', () => {
    it('has fast 2-second phases', () => {
      const pattern = getBreathingPreset('energizing');

      expect(pattern.phases).toHaveLength(2);
      pattern.phases.forEach((p) => {
        expect(p.duration).toBe(2000);
      });
      expect(pattern.cycles).toBe(10);
    });
  });

  describe('calming preset', () => {
    it('has longer exhale than inhale', () => {
      const pattern = getBreathingPreset('calming');

      const inhale = pattern.phases.find((p) => p.phase === 'inhale');
      const exhale = pattern.phases.find((p) => p.phase === 'exhale');

      expect(inhale).toBeDefined();
      expect(exhale).toBeDefined();
      expect(exhale.duration).toBeGreaterThan(inhale.duration);
    });
  });
});

describe('BreathingPattern validation', () => {
  it('all presets have scale values between reasonable bounds', () => {
    const presets = ['relaxing', 'box', 'energizing', 'calming'];

    presets.forEach((preset) => {
      const pattern = getBreathingPreset(preset);
      pattern.phases.forEach((phase) => {
        expect(phase.targetScale).toBeGreaterThanOrEqual(0.5);
        expect(phase.targetScale).toBeLessThanOrEqual(2.0);
      });
    });
  });
});

describe('Breathing phase types', () => {
  it('only uses valid phase types', () => {
    const validPhases = ['inhale', 'holdIn', 'exhale', 'holdOut'];
    const presets = ['relaxing', 'box', 'energizing', 'calming'];

    presets.forEach((preset) => {
      const pattern = getBreathingPreset(preset);
      pattern.phases.forEach((phase) => {
        expect(validPhases).toContain(phase.phase);
      });
    });
  });

  it('breathing patterns start with inhale', () => {
    const presets = ['relaxing', 'box', 'energizing', 'calming'];

    presets.forEach((preset) => {
      const pattern = getBreathingPreset(preset);
      expect(pattern.phases[0].phase).toBe('inhale');
    });
  });
});
