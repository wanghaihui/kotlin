package com.imagine.story.common.websocket;

class InsufficientDataException extends WebSocketException {
    private static final long serialVersionUID = 1L;

    private final int mRequestedByteCount;
    private final int mReadByteCount;

    public InsufficientDataException(int requestedByteCount, int readByteCount) {
        super(WebSocketError.INSUFFICENT_DATA, "The end of the stream has been reached unexpectedly.");

        mRequestedByteCount = requestedByteCount;
        mReadByteCount      = readByteCount;
    }

    public int getRequestedByteCount() {
        return mRequestedByteCount;
    }

    public int getReadByteCount() {
        return mReadByteCount;
    }
}
