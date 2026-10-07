package com.faceunlock.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility discovery for devices where UniversalAuth (or a similar
 * LSPosed module) already provides the SystemUI face-unlock integration.
 *
 * This class is intentionally observational: it does not call unlock APIs,
 * alter authentication results, bypass Keyguard, or send UniversalAuth
 * unlock broadcasts.
 */
public final class UniversalAuthCompatibility {
    private UniversalAuthCompatibility() {}

    public static Report scan(ClassLoader loader) {
        List<String> found = new ArrayList<>();

        scanClass(loader,
                "com.android.keyguard.KeyguardUpdateMonitor",
                found);
        scanClass(loader,
                "com.android.systemui.statusbar.phone.CentralSurfaces",
                found);
        scanClass(loader,
                "com.android.systemui.statusbar.phone.StatusBar",
                found);

        return new Report(found);
    }

    private static void scanClass(ClassLoader loader, String name, List<String> found) {
        try {
            Class<?> c = Class.forName(name, false, loader);

            for (Method m : c.getDeclaredMethods()) {
                String n = m.getName();
                if (n.contains("FaceAuthenticated")
                        || n.contains("BiometricUnlockController")
                        || n.contains("FaceAuth")
                        || n.contains("Unlock")) {
                    found.add("METHOD " + name + "#" + n);
                }
            }

            for (Field f : c.getDeclaredFields()) {
                String n = f.getName();
                if (n.contains("BiometricUnlockController")
                        || n.contains("biometricUnlockController")
                        || n.contains("KeyguardUpdateMonitor")
                        || n.contains("keyguardUpdateMonitor")) {
                    found.add("FIELD " + name + "#" + n);
                }
            }
        } catch (Throwable ignored) {
            // Discovery is best-effort and must never affect SystemUI.
        }
    }

    public static final class Report {
        public final List<String> found;

        Report(List<String> found) {
            this.found = found;
        }

        public boolean looksCompatible() {
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
