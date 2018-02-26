package com.imagine.story.common.websocket;

import java.util.List;
import java.util.Map;

/**
 * An exception raised(产生) due to a violation(违反) against the WebSocket protocol
 */
public class OpeningHandshakeException extends WebSocketException {
    private static final long serialVersionUID = 1L;

    private final StatusLine mStatusLine;
    private final Map<String, List<String>> mHeaders;
    private final byte[] mBody;

    OpeningHandshakeException(WebSocketError error, String message, StatusLine statusLine, Map<String, List<String>> headers) {
        this(error, message, statusLine, headers, null);
    }

    OpeningHandshakeException(WebSocketError error, String message,
            StatusLine statusLine, Map<String, List<String>> headers, byte[] body) {
        super(error, message);

        mStatusLine = statusLine;
        mHeaders = headers;
        mBody = body;
    }

    public StatusLine getStatusLine() {
        return mStatusLine;
    }

    public Map<String, List<String>> getHeaders() {
        return mHeaders;
    }

    public byte[] getBody() {
        return mBody;
    }

}
