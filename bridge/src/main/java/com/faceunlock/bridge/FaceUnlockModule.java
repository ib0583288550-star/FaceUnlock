package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.5.1";
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

        log(50, "FaceUnlock", "SystemUI classloader ready; starting safe discovery.");

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

            if (report.found.isEmpty()) {
                safety.failSafe();
                log(50, "FaceUnlock",
                    "No supported SystemUI biometric classes found; state=FALLBACK.");
            } else {
                safety.ready();
                log(50, "FaceUnlock",
                    "SystemUI discovery completed; state=READY. "
                        + "UniversalAuthCompatible=" + ua.looksCompatible());
            }
        } catch (Throwable t) {
            safety.error();
            log(50, "FaceUnlock",
                "SystemUI discovery error; state=ERROR.");
        }

        log(50, "FaceUnlock",
            "Discovery only: no biometric result, Keyguard state, or provider "
                + "behavior is modified. Runtime state=" + safety.getState());
    }
}
