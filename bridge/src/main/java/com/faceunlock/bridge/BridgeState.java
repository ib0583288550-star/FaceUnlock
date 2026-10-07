package com.faceunlock.bridge;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Conservative bridge state shared with the Manager.
 * No authentication result is fabricated here.
 */
public final class BridgeState {
    private static final String PREFS = "face_unlock_bridge";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_STATE = "state";

    public enum State { DISABLED, STARTING, READY, AUTHENTICATING, FALLBACK, ERROR }

    private BridgeState() {}

    public static void setEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
        if (!enabled) setState(context, State.DISABLED);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setState(Context context, State state) {
        prefs(context).edit().putString(KEY_STATE, state.name()).apply();
    }

    public static State getState(Context context) {
        try {
            return State.valueOf(prefs(context).getString(KEY_STATE, State.DISABLED.name()));
        } catch (Exception e) {
            return State.ERROR;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
