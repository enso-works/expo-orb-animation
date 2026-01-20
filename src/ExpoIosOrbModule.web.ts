import { registerWebModule, NativeModule } from 'expo';

import { ExpoIosOrbModuleEvents } from './ExpoIosOrb.types';

class ExpoIosOrbModule extends NativeModule<ExpoIosOrbModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoIosOrbModule, 'ExpoIosOrbModule');
