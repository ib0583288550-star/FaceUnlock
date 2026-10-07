package com.faceunlock.bridge;

import android.os.Handler;
import android.os.Looper;

public final class SafetyController {
    public enum State { DISABLED, STARTING, READY, AUTHENTICATING, FALLBACK, ERROR }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private State state = State.DISABLED;
    private long operationStartedAt = 0L;
    private long timeoutMs = 3000L;
    private long generation = 0L;

    public synchronized State getState() { return state; }

    public synchronized void setTimeoutMs(long timeoutMs) {
        if (timeoutMs >= 1000L) this.timeoutMs = timeoutMs;
    }

    public synchronized boolean isEnabled() {
        return state != State.DISABLED;
    }

    public synchronized void start() {
        cancelWatchdogLocked();
        operationStartedAt = System.currentTimeMillis();
        state = State.STARTING;
    }

    public synchronized void ready() {
        cancelWatchdogLocked();
        if (state == State.STARTING || state == State.AUTHENTICATING) {
            state = State.READY;
            operationStartedAt = 0L;
        }
    }

    public synchronized void authenticating() {
        if (state == State.READY || state == State.STARTING) {
            operationStartedAt = System.currentTimeMillis();
            state = State.AUTHENTICATING;
            scheduleWatchdogLocked();
        }
    }

    /** Marks that a genuine SystemUI face-auth callback was observed. */
    public synchronized void faceAuthenticatedObserved() {
        cancelWatchdogLocked();
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
        cancelWatchdogLocked();
        operationStartedAt = 0L;
        state = State.FALLBACK;
    }

    public synchronized void error() {
        cancelWatchdogLocked();
        operationStartedAt = 0L;
        state = State.ERROR;
    }

    public synchronized void disable() {
        cancelWatchdogLocked();
        operationStartedAt = 0L;
        state = State.DISABLED;
    }

    private void scheduleWatchdogLocked() {
        final long expectedGeneration = ++generation;
        final long delay = timeoutMs;
        handler.postDelayed(() -> {
            synchronized (SafetyController.this) {
                if (expectedGeneration != generation) return;
                if (state == State.AUTHENTICATING
                        && operationStartedAt != 0L
                        && System.currentTimeMillis() - operationStartedAt >= timeoutMs) {
                    operationStartedAt = 0L;
                    state = State.FALLBACK;
                }
            }
        }, delay);
    }

    private void cancelWatchdogLocked() {
        generation++;
        handler.removeCallbacksAndMessages(null);
    }
}
