import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoOrbViewProps } from './ExpoOrb.types';

const NativeView: React.ComponentType<ExpoOrbViewProps> =
  requireNativeView('ExpoOrb');

export default function ExpoOrbView(props: ExpoOrbViewProps) {
  return <NativeView {...props} />;
}
