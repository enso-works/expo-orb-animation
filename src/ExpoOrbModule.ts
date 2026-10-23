import { NativeModule, requireNativeModule } from 'expo';

declare class ExpoOrbModule extends NativeModule {
  /**
   * Set the activity level (0-1) for the orb animation.
   * Uses a native function instead of a prop to bypass React's reconciliation
   * and prevent view re-renders that cause animation flickering.
   */
  setActivity(activity: number): void;
}

const module = requireNativeModule<ExpoOrbModule>('ExpoOrb');

export default module;

/**
 * Set the orb activity level directly via native function (clamped to 0..1).
 * This bypasses React's prop system to prevent animation interference.
 */
export function setOrbActivity(activity: number): void {
  // Native clamps to 0..1 but passes NaN straight through to the animation.
  if (typeof activity !== 'number' || Number.isNaN(activity)) return;
  module.setActivity(activity);
}
