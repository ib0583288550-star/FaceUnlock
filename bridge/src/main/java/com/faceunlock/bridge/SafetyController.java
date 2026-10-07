package com.faceunlock.bridge;

public final class SafetyController {
    public enum State { DISABLED, STARTING, READY, AUTHENTICATING, FALLBACK, ERROR }
    private State state = State.DISABLED;
    public synchronized State getState() { return state; }
    public synchronized void failSafe() { state = State.FALLBACK; }
    public synchronized void disable() { state = State.DISABLED; }
}
