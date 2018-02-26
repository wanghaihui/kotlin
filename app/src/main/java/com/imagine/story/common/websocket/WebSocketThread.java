package com.imagine.story.common.websocket;

abstract class WebSocketThread extends Thread {

    protected final WebSocket mWebSocket;
    private final ThreadType mThreadType;

    WebSocketThread(String name, WebSocket ws, ThreadType type) {
        super(name);
        mWebSocket  = ws;
        mThreadType = type;
    }

    @Override
    public void run() {
        ListenerManager lm = mWebSocket.getListenerManager();

        if (lm != null) {
            // Execute onThreadStarted() of the listeners.
            lm.callOnThreadStarted(mThreadType, this);
        }

        runMain();

        if (lm != null) {
            // Execute onThreadStopping() of the listeners.
            lm.callOnThreadStopping(mThreadType, this);
        }
    }

    public void callOnThreadCreated() {
        ListenerManager lm = mWebSocket.getListenerManager();

        if (lm != null) {
            lm.callOnThreadCreated(mThreadType, this);
        }
    }

    protected abstract void runMain();
}
