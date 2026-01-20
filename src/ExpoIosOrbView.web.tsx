import * as React from 'react';

import { ExpoIosOrbViewProps } from './ExpoIosOrb.types';

const defaultColors = ['#22c55e', '#3b82f6', '#ec4899'];

export default function ExpoIosOrbView(props: ExpoIosOrbViewProps) {
  const colors = Array.isArray(props.backgroundColors) &&
    props.backgroundColors.every((color) => typeof color === 'string')
    ? (props.backgroundColors as string[])
    : defaultColors;

  const gradient = `radial-gradient(circle at 30% 30%, ${colors[0]}, ${colors[1] ?? colors[0]}, ${colors[2] ?? colors[1] ?? colors[0]})`;

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        borderRadius: '9999px',
        background: gradient,
        boxShadow: '0 0 30px rgba(255,255,255,0.2)',
      }}
    />
  );
}
