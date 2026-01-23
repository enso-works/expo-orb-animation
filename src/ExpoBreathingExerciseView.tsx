import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoBreathingExerciseViewProps } from './ExpoBreathingExercise.types';

const NativeView: React.ComponentType<ExpoBreathingExerciseViewProps> =
  requireNativeView('ExpoBreathingExercise');

export default function ExpoBreathingExerciseView(props: ExpoBreathingExerciseViewProps) {
  return <NativeView {...props} />;
}
