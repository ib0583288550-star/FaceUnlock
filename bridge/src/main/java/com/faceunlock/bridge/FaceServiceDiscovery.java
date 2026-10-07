package com.faceunlock.bridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only discovery of Android face/biometric framework classes.
 * It never starts authentication and never changes authentication results.
 */
public final class FaceServiceDiscovery {
    private static final String[] CANDIDATES = {
        "android.hardware.face.FaceManager",
        "android.hardware.face.Face",
        "android.hardware.biometrics.BiometricManager",
        "android.hardware.biometrics.BiometricAuthenticator",
        "android.hardware.biometrics.IBiometricService",
        "android.hardware.face.IFaceService",
        "android.hardware.face.IFaceServiceReceiver"
    };

    private FaceServiceDiscovery() {}

    public static Report scan(ClassLoader loader) {
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String name : CANDIDATES) {
            try {
                Class<?> c = Class.forName(name, false, loader);
                found.add(describe(c));
            } catch (Throwable ignored) {
                missing.add(name);
            }
        }

        return new Report(found, missing);
    }

    private static String describe(Class<?> c) {
        int methods = 0;
        int fields = 0;
        int faceRelatedMethods = 0;

        try {
            methods = c.getDeclaredMethods().length;
            for (Method m : c.getDeclaredMethods()) {
                String n = m.getName().toLowerCase();
                if (n.contains("face") || n.contains("auth")
                        || n.contains("authenticate") || n.contains("enroll")) {
                    faceRelatedMethods++;
                }
            }
        } catch (Throwable ignored) {}

        try {
            fields = c.getDeclaredFields().length;
        } catch (Throwable ignored) {}

        return "FOUND " + c.getName()
                + " methods=" + methods
                + " fields=" + fields
                + " faceAuthMethods=" + faceRelatedMethods;
    }

    public static final class Report {
        public final List<String> found;
        public final List<String> missing;

        Report(List<String> found, List<String> missing) {
            this.found = found;
            this.missing = missing;
        }

        public boolean hasFaceFramework() {
            for (String item : found) {
                if (item.contains("FaceManager") || item.contains("IFaceService")) {
                    return true;
                }
            }
            return false;
        }

        public String toLogString() {
            StringBuilder s = new StringBuilder();
            s.append("Face framework discovery: found=")
                    .append(found.size())
                    .append(", missing=")
                    .append(missing.size());
            for (String item : found) s.append("\n").append(item);
            for (String item : missing) s.append("\nMISSING ").append(item);
            return s.toString();
        }
    }
}
