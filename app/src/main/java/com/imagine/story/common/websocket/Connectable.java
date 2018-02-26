package com.imagine.story.common.websocket;

import java.util.concurrent.Callable;

/**
 * An implementation of {@link Callable} interface that calls {@link WebSocket#connect()}
 */
class Connectable implements Callable<WebSocket> {
    private final WebSocket mWebSocket;

    public Connectable(WebSocket ws) {
        mWebSocket = ws;
    }

    @Override
    public WebSocket call() throws WebSocketException {
        return mWebSocket.connect();
    }
}
