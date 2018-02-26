package com.imagine.story.common.websocket;

public interface PayloadGenerator {
    /**
     * Generate a payload of a frame.
     *
     * Note that the maximum payload length of control frames
     * (e.g. ping frames) is 125 in bytes. Therefore, the length
     * of a byte array returned from this method must not exceed
     * 125 bytes.
     *
     * @return A payload of a frame.
     */
    byte[] generate();
}
