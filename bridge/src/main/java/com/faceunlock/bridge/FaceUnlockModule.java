package com.faceunlock.bridge;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;

public final class FaceUnlockModule extends XposedModule {
    public static final String VERSION = "0.3.1";

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(io.github.libxposed.api.XposedInterface.LogLevel.INFO, "FaceUnlock", "Bridge loaded; diagnostic mode only.");
    }
}
