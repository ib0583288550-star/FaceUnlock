package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;

/**
 * Diagnostic-only modern LSPosed entry point.
 * It deliberately does not modify biometric results or Keyguard state.
 */
public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.3.0";

    @Override
    public void onModuleLoaded() {
        log("FaceUnlock Bridge loaded; diagnostic mode only.");
    }
}
