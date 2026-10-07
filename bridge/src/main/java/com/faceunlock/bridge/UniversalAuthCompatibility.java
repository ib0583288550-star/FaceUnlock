package com.faceunlock.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Observational compatibility discovery for UniversalAuth-style SystemUI
 * integrations. It does not invoke hooks, change biometric results, bypass
 * Keyguard, or send unlock broadcasts.
 */
public final class UniversalAuthCompatibility {
    public static final String UNIVERSAL_AUTH_PACKAGE = "ax.nd.universalauth";
    private UniversalAuthCompatibility() {}

    private static final String[] TARGETS = {
        "com.android.keyguard.KeyguardUpdateMonitor",
        "com.android.systemui.statusbar.phone.CentralSurfaces",
        "com.android.systemui.statusbar.phone.StatusBar",
        "com.android.systemui.statusbar.phone.BiometricUnlockController"
    };

    public static Report scan(ClassLoader loader) {
        List<String> found = new ArrayList<>();

        for (String name : TARGETS) {
            scanClass(loader, name, found);
        }

        return new Report(found);
    }

    private static void scanClass(ClassLoader loader, String name, List<String> found) {
        try {
            Class<?> c = Class.forName(name, false, loader);

            for (Method m : c.getDeclaredMethods()) {
                String n = m.getName();
                if (isInterestingMethod(n)) {
                    found.add("METHOD " + name + "#" + n
                            + signature(m));
                }
            }

            for (Field f : c.getDeclaredFields()) {
                String n = f.getName();
                if (isInterestingField(n)) {
                    found.add("FIELD " + name + "#" + n
                            + ":" + f.getType().getName());
                }
            }
        } catch (Throwable ignored) {
            // Discovery is best-effort and must never affect SystemUI.
        }
    }

    private static boolean isInterestingMethod(String name) {
        return name.contains("FaceAuthenticated")
                || name.contains("BiometricUnlockController")
                || name.contains("FaceAuth")
                || name.contains("FaceListening")
                || name.contains("UpdateFace")
                || name.contains("Unlock");
    }

    private static boolean isInterestingField(String name) {
        return name.contains("BiometricUnlockController")
                || name.contains("biometricUnlockController")
                || name.contains("KeyguardUpdateMonitor")
                || name.contains("keyguardUpdateMonitor")
                || name.contains("Face");
    }

    private static String signature(Method m) {
        StringBuilder s = new StringBuilder("(");
        Class<?>[] p = m.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) s.append(",");
            s.append(p[i].getName());
        }
        s.append("):").append(m.getReturnType().getName());
        return s.toString();
    }

    public static final class Report {
        public final List<String> found;

        Report(List<String> found) {
            this.found = found;
        }

        public boolean looksCompatible() {
            // UniversalAuth compatibility is observational only; no calls into the module are made.
            for (String item : found) {
                if (item.contains("FaceAuthenticated")
                        || item.contains("BiometricUnlockController")) {
                    return true;
                }
            }
            return false;
        }

        public String toLogString() {
            StringBuilder s = new StringBuilder();
            s.append("UniversalAuth compatibility discovery: found=")
                    .append(found.size());
            for (String item : found) {
                s.append("\n").append(item);
            }
            return s.toString();
        }
    }
}
