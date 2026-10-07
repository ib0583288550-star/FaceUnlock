package com.faceunlock.bridge;

/**
 * Safe LSPosed entry point.
 * This first stage only proves module loading; it deliberately does not alter
 * biometric results or Keyguard state.
 */
public final class FaceUnlockModule {
    public static final String VERSION = "0.2.0";
}
