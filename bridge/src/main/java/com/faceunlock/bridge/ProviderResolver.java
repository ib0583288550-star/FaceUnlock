package com.faceunlock.bridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Safe discovery helper.
 *
 * This class only detects classes that are already present in the target
 * process. It never changes biometric results, Keyguard state, or provider
 * behavior.
 */
public final class ProviderResolver {
    private static final String TAG = "FaceUnlock/ProviderResolver";

    private static final String[] SYSTEMUI_CANDIDATES = {
        "com.android.systemui.statusbar.phone.BiometricUnlockController",
        "com.android.systemui.statusbar.phone.KeyguardBouncer",
        "com.android.systemui.statusbar.policy.KeyguardMonitor",
        "com.android.systemui.keyguard.KeyguardViewMediator",
        "com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager",
        "com.android.systemui.biometrics.AuthController",
        "com.android.systemui.biometrics.UdfpsController"
    };

    private ProviderResolver() {}

    public static DiscoveryReport scan(ClassLoader loader) {
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String name : SYSTEMUI_CANDIDATES) {
            try {
                Class.forName(name, false, loader);
                found.add(name);
            } catch (Throwable ignored) {
                missing.add(name);
            }
        }

        return new DiscoveryReport(found, missing);
    }

    public static final class DiscoveryReport {
        public final List<String> found;
        public final List<String> missing;

        DiscoveryReport(List<String> found, List<String> missing) {
            this.found = found;
            this.missing = missing;
        }

        public String toLogString() {
            StringBuilder s = new StringBuilder();
            s.append("SystemUI biometric discovery: found=")
             .append(found.size())
             .append(", missing=")
             .append(missing.size());
            for (String name : found) {
                s.append("\nFOUND ").append(name);
            }
            return s.toString();
        }
    }
}
