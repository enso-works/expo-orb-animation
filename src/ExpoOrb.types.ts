import type { ColorValue, StyleProp, ViewStyle } from 'react-native';

export type ExpoOrbViewProps = {
  backgroundColors?: ColorValue[];
  glowColor?: ColorValue;
  particleColor?: ColorValue;
  coreGlowIntensity?: number;
  breathingIntensity?: number;
  breathingSpeed?: number;
  showBackground?: boolean;
  showWavyBlobs?: boolean;
  showParticles?: boolean;
  showGlowEffects?: boolean;
  showShadow?: boolean;
  speed?: number;
  style?: StyleProp<ViewStyle>;
  // NOTE: activity is NOT a prop - use setOrbActivity() function instead
  // to bypass React's prop reconciliation and prevent animation flickering
};
