import { registerWebModule, NativeModule } from 'expo';

class ExpoOrbModule extends NativeModule {
  setActivity(_activity: number): void {}
}

const module = registerWebModule(ExpoOrbModule, 'ExpoOrbModule');

export default module;

// The web orb is a still gradient, so activity has nothing to drive.
export function setOrbActivity(_activity: number): void {}
