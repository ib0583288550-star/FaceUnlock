# FaceUnlock Bridge 0.2

This module is intentionally a loading/diagnostic stage.

It does **not** spoof biometric success, unlock Keyguard, change PIN state, or
hook an unknown biometric method. The next integration stage will be selected
from the actual biometric provider/HAL present on the device.

LSPosed's modern module format uses `META-INF/xposed/java_init.list` and a
scope list; see the official LSPosed documentation.
