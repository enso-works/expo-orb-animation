import type { ColorValue, StyleProp, ViewStyle } from 'react-native';

export type ExpoIosOrbViewProps = {
  backgroundColors?: ColorValue[];
  glowColor?: ColorValue;
  particleColor?: ColorValue;
  coreGlowIntensity?: number;
  showBackground?: boolean;
  showWavyBlobs?: boolean;
  showParticles?: boolean;
  showGlowEffects?: boolean;
  showShadow?: boolean;
  speed?: number;
  style?: StyleProp<ViewStyle>;
};
