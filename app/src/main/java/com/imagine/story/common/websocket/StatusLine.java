package com.imagine.story.common.websocket;

/**
 * HTTP status line returned from an HTTP server
 */

public class StatusLine {

    private final String mHttpVersion;
    private final int mStatusCode;
    private final String mReasonPhrase;

    private final String mString;

    StatusLine(String line) {
        // HTTP-Version Status-Code Reason-Phrase
        String[] elements = line.split(" +", 3);

        if (elements.length < 2) {
            throw new IllegalArgumentException();
        }

        mHttpVersion  = elements[0];
        mStatusCode   = Integer.parseInt(elements[1]);
        mReasonPhrase = (elements.length == 3) ? elements[2] : null;
        mString       = line;
    }

    public String getHttpVersion() {
        return mHttpVersion;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public String getReasonPhrase() {
        return mReasonPhrase;
    }

    @Override
    public String toString() {
        return mString;
    }
}
