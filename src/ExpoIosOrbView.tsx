import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoIosOrbViewProps } from './ExpoIosOrb.types';

const NativeView: React.ComponentType<ExpoIosOrbViewProps> =
  requireNativeView('ExpoIosOrb');

export default function ExpoIosOrbView(props: ExpoIosOrbViewProps) {
  return <NativeView {...props} />;
}
