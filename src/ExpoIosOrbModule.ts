import { NativeModule, requireNativeModule } from 'expo';

import { ExpoIosOrbModuleEvents } from './ExpoIosOrb.types';

declare class ExpoIosOrbModule extends NativeModule<ExpoIosOrbModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoIosOrbModule>('ExpoIosOrb');
