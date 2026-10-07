package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import java.lang.reflect.Method;

/**
 * FaceUnlock bridge. The current hook layer is observational only:
 * it records when the real SystemUI face-auth callback is reached and then
 * always proceeds with the original method.
 */
public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.6.0";
    private static final String SYSTEM_UI = "com.android.systemui";

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(50, "FaceUnlock",
            "Bridge loaded; waiting for package classloader. process="
                + param.getProcessName());
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!SYSTEM_UI.equals(param.getPackageName())) {
            return;
        }

        log(50, "FaceUnlock", "SystemUI classloader ready; starting discovery/hooks.");

        SafetyController safety = new SafetyController();
        safety.start();

        try {
            ClassLoader loader = param.getClassLoader();

            ProviderResolver.DiscoveryReport report =
                ProviderResolver.scan(loader);
            log(50, "FaceUnlock", report.toLogString());

            UniversalAuthCompatibility.Report ua =
                UniversalAuthCompatibility.scan(loader);
            log(50, "FaceUnlock", ua.toLogString());

            int hooks = installObservationHooks(loader);

            if (report.found.isEmpty()) {
                safety.failSafe();
                log(50, "FaceUnlock",
                    "No supported SystemUI biometric classes found; state=FALLBACK.");
            } else {
                safety.ready();
                log(50, "FaceUnlock",
                    "Discovery completed; observationHooks=" + hooks
                        + "; UniversalAuthCompatible=" + ua.looksCompatible());
            }
        } catch (Throwable t) {
            safety.error();
            log(50, "FaceUnlock", "SystemUI discovery/hook setup error; state=ERROR.");
        }

        log(50, "FaceUnlock",
            "Safe observation only: authentication results and Keyguard state are untouched.");
    }

    private int installObservationHooks(ClassLoader loader) {
        int installed = 0;
        installed += hookAllNamedMethods(loader,
                "com.android.keyguard.KeyguardUpdateMonitor",
                "onFaceAuthenticated");
        installed += hookAllNamedMethods(loader,
                "com.android.systemui.statusbar.phone.BiometricUnlockController",
                "onFaceAuthenticated");
        return installed;
    }

    private int hookAllNamedMethods(ClassLoader loader, String className, String methodName) {
        int installed = 0;
        try {
            Class<?> c = Class.forName(className, false, loader);
            for (Method method : c.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())) continue;

                hook(method).intercept(chain -> {
                    log(50, "FaceUnlock",
                        "OBSERVED " + className + "#" + methodName
                            + " args=" + chain.getArgs().length
                            + " return=" + method.getReturnType().getName());
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
