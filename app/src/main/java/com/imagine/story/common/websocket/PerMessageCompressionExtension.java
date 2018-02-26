package com.imagine.story.common.websocket;

/**
 * Per-Message Compression Extension (<a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>)
 */
abstract class PerMessageCompressionExtension extends WebSocketExtension {

    public PerMessageCompressionExtension(String name) {
        super(name);
    }


    public PerMessageCompressionExtension(WebSocketExtension source) {
        super(source);
    }


    /**
     * Decompress the compressed message.
     */
    protected abstract byte[] decompress(byte[] compressed) throws WebSocketException;


    /**
     * Compress the plain message.
     */
    protected abstract byte[] compress(byte[] plain) throws WebSocketException;
}
