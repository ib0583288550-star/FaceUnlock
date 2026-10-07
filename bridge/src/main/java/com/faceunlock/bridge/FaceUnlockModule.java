package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;

public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.4.0";

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(50, "FaceUnlock", "Bridge loaded; discovery mode only.");

        try {
            ProviderResolver.DiscoveryReport report =
                ProviderResolver.scan(param.getClassLoader());
            log(50, "FaceUnlock", report.toLogString());
        } catch (Throwable t) {
            log(50, "FaceUnlock",
                "Discovery failed safely: " + t.getClass().getSimpleName());
        }

        log(50, "FaceUnlock",
            "No biometric result, Keyguard state, or provider behavior is modified.");
    }
}
