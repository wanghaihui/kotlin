package com.imagine.story.common.websocket;

class PingSender extends PeriodicalFrameSender{
    private static final String TIMER_NAME = "PingSender";

    public PingSender(WebSocket webSocket, PayloadGenerator generator) {
        super(webSocket, TIMER_NAME, generator);
    }

    @Override
    protected WebSocketFrame createFrame(byte[] payload) {
        return WebSocketFrame.createPingFrame(payload);
    }
}
