package com.imagine.story.common.websocket;

class FinishThread extends WebSocketThread {
    public FinishThread(WebSocket ws) {
        super("FinishThread", ws, ThreadType.FINISH_THREAD);
    }


    @Override
    public void runMain() {
        mWebSocket.finish();
    }
}
