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
};
