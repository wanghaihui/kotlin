package com.imagine.story.common.websocket;

/**
 * Created by conquer on 2018/2/9.
 *
 */

public enum  WebSocketState {
    /**
     * The initial state of a {@link WebSocket} instance.
     */
    CREATED,


    /**
     * An <a href="https://tools.ietf.org/html/rfc6455#section-4">opening
     * handshake</a> is being performed.
     */
    CONNECTING,


    /**
     * The WebSocket connection is established (= the <a href=
     * "https://tools.ietf.org/html/rfc6455#section-4">opening handshake</a>
     * has succeeded) and usable.
     */
    OPEN,


    /**
     * A <a href="https://tools.ietf.org/html/rfc6455#section-7">closing
     * handshake</a> is being performed.
     */
    CLOSING,


    /**
     * The WebSocket connection is closed.
     */
    CLOSED
}
