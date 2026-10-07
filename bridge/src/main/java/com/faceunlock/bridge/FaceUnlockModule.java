package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;

public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.5.0";

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(50, "FaceUnlock", "Bridge loaded; safety/discovery stage.");

        SafetyController safety = new SafetyController();
        safety.start();

        try {
            ProviderResolver.DiscoveryReport report =
                ProviderResolver.scan(param.getClassLoader());

            log(50, "FaceUnlock", report.toLogString());

            if (report.found.isEmpty()) {
                safety.failSafe();
                log(50, "FaceUnlock",
                    "No supported SystemUI biometric classes found; state=FALLBACK.");
            } else {
                safety.ready();
                log(50, "FaceUnlock",
                    "Provider discovery completed; state=READY.");
            }
        } catch (Throwable t) {
            safety.error();
            log(50, "FaceUnlock",
                "Provider discovery error; state=ERROR.");
        }

        log(50, "FaceUnlock",
            "No biometric result, Keyguard state, or provider behavior is modified. "
            + "Runtime state=" + safety.getState());
    }
}
