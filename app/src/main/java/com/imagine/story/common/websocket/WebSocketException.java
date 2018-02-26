package com.imagine.story.common.websocket;

public class WebSocketException extends Exception {

    private static final long serialVersionUID = 1L;

    private final WebSocketError mError;

    public WebSocketException(WebSocketError error) {
        mError = error;
    }

    public WebSocketException(WebSocketError error, String message) {
        super(message);
        mError = error;
    }

    public WebSocketException(WebSocketError error, Throwable cause) {
        super(cause);
        mError = error;
    }

    public WebSocketException(WebSocketError error, String message, Throwable cause) {
        super(message, cause);
        mError = error;
    }

    public WebSocketError getError() {
        return mError;
    }

}
