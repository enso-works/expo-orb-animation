import * as React from 'react';

import { ExpoIosOrbViewProps } from './ExpoIosOrb.types';

export default function ExpoIosOrbView(props: ExpoIosOrbViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
