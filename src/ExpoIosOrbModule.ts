import { NativeModule, requireNativeModule } from 'expo';

declare class ExpoIosOrbModule extends NativeModule {}

export default requireNativeModule<ExpoIosOrbModule>('ExpoIosOrb');
