package com.laoxin.mq.client.impl;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class HandlerState {

    private static final AtomicReferenceFieldUpdater<HandlerState, State> STATE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(HandlerState.class, HandlerState.State.class, "state");
    private volatile HandlerState.State state = null;

    enum State {
        Uninitialized, // Not initialized
        Connecting, // Client connecting to broker
        Ready, // Handler is being used
        Closing, // Close cmd has been sent to broker
        Closed, // Broker acked the close
        Failed // Handler is failed
    }

    public HandlerState() {
        STATE_UPDATER.set(this, HandlerState.State.Uninitialized);
    }

    protected HandlerState.State getState() {
        return (HandlerState.State)STATE_UPDATER.get(this);
    }

    protected void setState(HandlerState.State s) {
        STATE_UPDATER.set(this, s);
    }

    protected boolean changeToReadyState() {
        return STATE_UPDATER.compareAndSet(this, HandlerState.State.Uninitialized, HandlerState.State.Ready) || STATE_UPDATER.compareAndSet(this, HandlerState.State.Connecting, HandlerState.State.Ready);
    }

    abstract String getHandlerName();


}
