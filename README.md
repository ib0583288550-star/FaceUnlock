# FaceUnlock

Android 13 / rooted-device face unlock project.

## Architecture
- `manager`: configuration and diagnostics UI.
- `bridge`: safety/bridge layer for the LSPosed integration.
- GitHub Actions builds both debug APK modules.

## Safety rules
- PIN/password remains the normal Android fallback.
- No fake biometric success.
- No forced Keyguard bypass.
- Provider/HAL errors must enter FALLBACK.
- Multiple face profiles will use genuine provider/HAL enrollment where supported.

## Current stage
0.3 — Modern LSPosed diagnostic entry point is in place. The provider/HAL integration is intentionally not guessed yet; it must be matched to the existing face/LSPosed component on the device.

## Build
The repository contains a GitHub Actions workflow under `.github/workflows/build.yml`. It builds the Manager and Bridge debug APKs and publishes them as Actions artifacts.
