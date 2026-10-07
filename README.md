# FaceUnlock

Android 13 / rooted-device face unlock project.

## Architecture
- `manager`: configuration and diagnostics UI.
- `bridge`: safety/bridge layer for the future LSPosed integration.

## Safety rules
- PIN/password remains the normal Android fallback.
- No fake biometric success.
- No forced Keyguard bypass.
- Provider/HAL errors must enter FALLBACK.
- Multiple face profiles will use genuine provider/HAL enrollment where supported.

## Current stage
0.1 — repository skeleton and safety layer. The actual provider/HAL integration is intentionally not guessed yet; it must be matched to the existing face/LSPosed component on the device.
