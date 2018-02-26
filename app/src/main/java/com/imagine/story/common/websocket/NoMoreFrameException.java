package com.imagine.story.common.websocket;

class NoMoreFrameException extends WebSocketException {
    private static final long serialVersionUID = 1L;

    public NoMoreFrameException() {
        super(WebSocketError.NO_MORE_FRAME, "No more WebSocket frame from the server.");
    }
}
