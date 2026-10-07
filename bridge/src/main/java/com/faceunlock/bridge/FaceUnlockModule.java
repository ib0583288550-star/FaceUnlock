package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import java.lang.reflect.Method;

/**
 * FaceUnlock bridge. Observation only: it never changes biometric results
 * and never bypasses Keyguard.
 */
public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.8.0";
    private static final String SYSTEM_UI = "com.android.systemui";
    private SafetyController safetyController;

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(50, "FaceUnlock",
            "Bridge loaded; waiting for package classloader. process="
                + param.getProcessName());
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
                log(50, "FaceUnlock",
                    "Required biometric/face framework discovery incomplete; state=FALLBACK.");
            } else {
                safetyController.ready();
                log(50, "FaceUnlock",
                    "Discovery completed; observationHooks=" + hooks
                        + "; FaceFramework=" + face.hasFaceFramework()
                        + "; UniversalAuthCompatible=" + ua.looksCompatible());
            }
        } catch (Throwable t) {
            safetyController.error();
            log(50, "FaceUnlock", "SystemUI discovery/hook setup error; state=ERROR.");
        }

        log(50, "FaceUnlock",
            "Safe observation only: authentication results and Keyguard state are untouched.");
    }

    private int installObservationHooks(ClassLoader loader) {
        int installed = 0;

        installed += hookAllNamedMethods(loader,
            "com.android.keyguard.KeyguardUpdateMonitor", "onFaceAuthenticated", false);
        installed += hookAllNamedMethods(loader,
            "com.android.systemui.statusbar.phone.BiometricUnlockController",
            "onFaceAuthenticated", false);

        installed += hookAllNamedMethods(loader,
            "com.android.keyguard.KeyguardUpdateMonitor", "updateFaceListeningState", true);
        installed += hookAllNamedMethods(loader,
            "com.android.keyguard.KeyguardUpdateMonitor", "requestFaceAuth", true);
        installed += hookAllNamedMethods(loader,
            "com.android.systemui.statusbar.phone.BiometricUnlockController",
            "startListeningForFace", true);

        return installed;
    }

    private int hookAllNamedMethods(ClassLoader loader, String className,
                                    String methodName, boolean markAuthenticating) {
        int installed = 0;
        try {
            Class<?> c = Class.forName(className, false, loader);
            for (Method method : c.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())) continue;

                hook(method).intercept(chain -> {
                    log(50, "FaceUnlock",
                        "OBSERVED " + className + "#" + methodName
                            + " args=" + chain.getArgs().size()
                            + " return=" + method.getReturnType().getName());

                    if (safetyController != null) {
                        if (markAuthenticating) {
                            safetyController.authenticating();
                            log(50, "FaceUnlock",
                                "SafetyController: face-auth lifecycle observed; state=AUTHENTICATING.");
                        } else if ("onFaceAuthenticated".equals(methodName)) {
                            safetyController.faceAuthenticatedObserved();
                            log(50, "FaceUnlock",
                                "SafetyController: genuine face-auth callback observed; state=READY.");
                        }
                    }

                    return chain.proceed();
                });
                installed++;
            }
        } catch (Throwable t) {
            log(40, "FaceUnlock",
                "Observation hook unavailable: " + className + "#" + methodName);
        }
        return installed;
    }
}
