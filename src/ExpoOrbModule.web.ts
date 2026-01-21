import { registerWebModule, NativeModule } from 'expo';

class ExpoOrbModule extends NativeModule {}

export default registerWebModule(ExpoOrbModule, 'ExpoOrbModule');
