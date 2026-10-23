import * as React from 'react';

import { ExpoBreathingExerciseViewProps } from './ExpoBreathingExercise.types';

const defaultColors = ['#22c55e', '#3b82f6', '#ec4899'];

// A still circle in the pattern's colours: the animation is native only.
export default function ExpoBreathingExerciseView(props: ExpoBreathingExerciseViewProps) {
  const colors =
    Array.isArray(props.blobColors) && props.blobColors.every((color) => typeof color === 'string')
      ? (props.blobColors as string[])
      : defaultColors;

  const gradient = `radial-gradient(circle at 30% 30%, ${colors[0]}, ${colors[1] ?? colors[0]}, ${colors[2] ?? colors[1] ?? colors[0]})`;

  return (
    <div style={{ width: '100%', height: '100%', borderRadius: '9999px', background: gradient }} />
  );
}
