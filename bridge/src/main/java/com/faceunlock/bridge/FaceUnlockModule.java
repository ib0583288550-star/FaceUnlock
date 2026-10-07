package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * FaceUnlock bridge. Observation only: it never changes biometric results
 * and never bypasses Keyguard.
 */
public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.10.0";
    private static final String SYSTEM_UI = "com.android.systemui";
    private SafetyController safetyController;

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(50, "FaceUnlock", "Bridge loaded; waiting for package classloader. process=" + param.getProcessName());
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!SYSTEM_UI.equals(param.getPackageName())) return;

        log(50, "FaceUnlock", "SystemUI classloader ready; starting discovery/hooks.");
        safetyController = new SafetyController();
        safetyController.start();

        try {
            ClassLoader loader = param.getClassLoader();
            ProviderResolver.DiscoveryReport report = ProviderResolver.scan(loader);
            log(50, "FaceUnlock", report.toLogString());
            FaceServiceDiscovery.Report face = FaceServiceDiscovery.scan(loader);
            log(50, "FaceUnlock", face.toLogString());
            UniversalAuthCompatibility.Report ua = UniversalAuthCompatibility.scan(loader);
            log(50, "FaceUnlock", ua.toLogString());

            int hooks = installObservationHooks(loader);

            if (report.found.isEmpty() || !face.hasFaceFramework()) {
                safetyController.failSafe();
                log(50, "FaceUnlock", "Required biometric/face framework discovery incomplete; state=FALLBACK.");
            } else {
                safetyController.ready();
                log(50, "FaceUnlock", "Discovery completed; observationHooks=" + hooks
                        + "; FaceFramework=" + face.hasFaceFramework()
                        + "; UniversalAuthCompatible=" + ua.looksCompatible());
            }
        } catch (Throwable t) {
            safetyController.error();
            log(50, "FaceUnlock", "SystemUI discovery/hook setup error; state=ERROR.");
        }

        log(50, "FaceUnlock", "Safe observation only: authentication results and Keyguard state are untouched.");
    }

    private int installObservationHooks(ClassLoader loader) {
        int installed = 0;
        installed += hookAllNamedMethods(loader, "com.android.keyguard.KeyguardUpdateMonitor", "onFaceAuthenticated", false);
        installed += hookAllNamedMethods(loader, "com.android.keyguard.KeyguardUpdateMonitor", "handleFaceAuthenticated", false);
        installed += hookAllNamedMethods(loader, "com.android.keyguard.KeyguardUpdateMonitor", "handleFaceAuthFailed", false);
        installed += hookAllNamedMethods(loader, "com.android.keyguard.KeyguardUpdateMonitor", "handleFaceError", false);
        installed += hookAllNamedMethods(loader, "com.android.systemui.statusbar.phone.BiometricUnlockController", "onFaceAuthenticated", false);
        installed += hookAllNamedMethods(loader, "com.android.systemui.statusbar.phone.BiometricUnlockController", "onBiometricAuthenticated", false);
        installed += hookAllNamedMethods(loader, "com.android.keyguard.KeyguardUpdateMonitor", "updateFaceListeningState", true);
        installed += hookAllNamedMethods(loader, "com.android.keyguard.KeyguardUpdateMonitor", "requestFaceAuth", true);
        installed += hookAllNamedMethods(loader, "com.android.systemui.statusbar.phone.BiometricUnlockController", "startListeningForFace", true);
        return installed;
    }

    private String describeArgs(Object[] args) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) s.append(", ");
            Object value = args[i];
            if (value == null) s.append("null");
            else if (value instanceof Number || value instanceof Boolean
                    || value instanceof Character || value instanceof String) s.append(String.valueOf(value));
            else s.append(value.getClass().getName());
        }
        return s.append("]").toString();
    }

    private FaceAuthObservation inspectFaceAuthArguments(Method method, Object[] args) {
        int userId = -1;
        Boolean strong = null;
        Class<?>[] types = method.getParameterTypes();

        for (int i = 0; i < Math.min(types.length, args.length); i++) {
            Object value = args[i];
            if (value instanceof Boolean && strong == null) {
                strong = (Boolean) value;
            } else if (value instanceof Integer && userId < 0
                    && ("int".equals(types[i].getName()) || Integer.class.equals(types[i]))) {
                userId = (Integer) value;
            }
        }
        return new FaceAuthObservation(userId, strong);
    }

    private int hookAllNamedMethods(ClassLoader loader, String className,
                                    String methodName, boolean markAuthenticating) {
        int installed = 0;
        try {
            Class<?> c = Class.forName(className, false, loader);
            for (Method method : c.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())) continue;

                hook(method).intercept(chain -> {
                    Object[] args = chain.getArgs().toArray();
                    log(50, "FaceUnlock", "OBSERVED " + className + "#" + methodName
                            + " args=" + args.length
                            + " values=" + describeArgs(args)
                            + " return=" + method.getReturnType().getName());

                    if (safetyController != null) {
                        if (markAuthenticating) {
                            safetyController.authenticating();
                            log(50, "FaceUnlock", "SafetyController: face-auth lifecycle observed; state=AUTHENTICATING.");
                        } else if ("onFaceAuthenticated".equals(methodName)
                                || "handleFaceAuthenticated".equals(methodName)) {
                            FaceAuthObservation observation = inspectFaceAuthArguments(method, args);
                            log(50, "FaceUnlock", "FACE_SUCCESS_OBSERVED userId="
                                    + observation.userId + " strong="
                                    + observation.isStrongBiometric + " method=" + methodName);

                            int currentUserId = resolveCurrentUserId();
                            if (observation.userId < 0 || currentUserId < 0) {
                                safetyController.failSafe();
                                log(50, "FaceUnlock",
                                    "Face success identity could not be validated; state=FALLBACK.");
                            } else if (observation.userId != currentUserId) {
                                safetyController.failSafe();
                                log(50, "FaceUnlock",
                                    "Face success rejected: authUserId=" + observation.userId
                                        + " currentUserId=" + currentUserId
                                        + "; state=FALLBACK.");
                            } else {
                                safetyController.faceAuthenticatedObserved();
                                log(50, "FaceUnlock",
                                    "Face success identity validated for current user; state=READY.");
                            }
                        }
                    }
                    return chain.proceed();
                });
                installed++;
            }
        } catch (Throwable t) {
            log(40, "FaceUnlock", "Observation hook unavailable: " + className + "#" + methodName);
        }
        return installed;
    }

    private int resolveCurrentUserId() {
        try {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            Method getCurrentUser = activityManager.getDeclaredMethod("getCurrentUser");
            getCurrentUser.setAccessible(true);
            Object userInfo = getCurrentUser.invoke(null);
            if (userInfo == null) return -1;
            Field id = userInfo.getClass().getField("id");
            return id.getInt(userInfo);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static final class FaceAuthObservation {
        final int userId;
        final Boolean isStrongBiometric;

        FaceAuthObservation(int userId, Boolean isStrongBiometric) {
            this.userId = userId;
            this.isStrongBiometric = isStrongBiometric;
        }
    }
}
