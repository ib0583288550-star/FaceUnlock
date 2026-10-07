# FaceUnlock

Android 13 / rooted-device face unlock project.

## Architecture
- `manager`: configuration and diagnostics UI.
- `bridge`: safety/bridge layer for the LSPosed integration.
- GitHub Actions builds both debug APK modules and publishes direct APK release assets.

## Manager settings
- Face Unlock enable/disable.
- Optional **instant unlock after face recognition**, without an extra swipe when Android permits it.
- Multiple face-profile names are currently stored locally; real provider/HAL enrollment is not implemented yet.

## Safety rules
- PIN/password remains the normal Android fallback.
- No fake biometric success.
- No forced Keyguard bypass.
- Provider/HAL errors must enter FALLBACK.
- Authentication watchdog timeout is currently 3 seconds.
- The instant-unlock setting is only a preference; Android may ignore confirmation hints according to system security settings.
- Multiple face profiles will use genuine provider/HAL enrollment where supported.

## Current stage
0.4 — Manager settings and the fail-safe watchdog are in place. The provider/HAL integration is intentionally not guessed yet; it must be matched to the existing face/LSPosed component on the device.

## Build
The repository contains a GitHub Actions workflow under `.github/workflows/build.yml`. It builds the Manager and Bridge debug APKs and publishes the latest successful debug APKs as direct assets on the `nightly` GitHub release.
