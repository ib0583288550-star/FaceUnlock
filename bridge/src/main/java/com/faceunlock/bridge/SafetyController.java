package com.faceunlock.bridge;

public final class SafetyController {
    public enum State { DISABLED, STARTING, READY, AUTHENTICATING, FALLBACK, ERROR }

    private State state = State.DISABLED;
    private long operationStartedAt = 0L;
    private long timeoutMs = 3000L;

    public synchronized State getState() { return state; }

    public synchronized void setTimeoutMs(long timeoutMs) {
        if (timeoutMs >= 1000L) this.timeoutMs = timeoutMs;
    }

    public synchronized boolean isEnabled() {
        return state != State.DISABLED;
    }

    public synchronized void start() {
        operationStartedAt = System.currentTimeMillis();
        state = State.STARTING;
    }

    public synchronized void ready() {
        if (state == State.STARTING || state == State.AUTHENTICATING) {
            state = State.READY;
            operationStartedAt = 0L;
        }
    }

    public synchronized void authenticating() {
        if (state == State.READY || state == State.STARTING) {
            operationStartedAt = System.currentTimeMillis();
            state = State.AUTHENTICATING;
        }
    }

    /** Marks that a genuine SystemUI face-auth callback was observed. */
    public synchronized void faceAuthenticatedObserved() {
        if (state == State.AUTHENTICATING || state == State.READY) {
            operationStartedAt = 0L;
            state = State.READY;
        }
    }

    public synchronized boolean checkTimeout() {
        if (operationStartedAt == 0L) return false;
        if (System.currentTimeMillis() - operationStartedAt >= timeoutMs) {
            failSafe();
            return true;
        }
        return false;
    }

    public synchronized void failSafe() {
        operationStartedAt = 0L;
        state = State.FALLBACK;
    }

    public synchronized void error() {
        operationStartedAt = 0L;
        state = State.ERROR;
    }

    public synchronized void disable() {
        operationStartedAt = 0L;
        state = State.DISABLED;
    }
}