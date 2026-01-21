import { NativeModule, requireNativeModule } from 'expo';

declare class ExpoIosOrbModule extends NativeModule {
  /**
   * Set the activity level (0-1) for the orb animation.
   * Uses a native function instead of a prop to bypass React's reconciliation
   * and prevent view re-renders that cause animation flickering.
   */
  setActivity(activity: number): void;
}

const module = requireNativeModule<ExpoIosOrbModule>('ExpoIosOrb');

export default module;

/**
 * Set the orb activity level directly via native function.
 * This bypasses React's prop system to prevent animation interference.
 */
export function setOrbActivity(activity: number): void {
  module.setActivity(activity);
}
