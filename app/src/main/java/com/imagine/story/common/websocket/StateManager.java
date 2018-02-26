package com.imagine.story.common.websocket;

/**
 * 状态管理器
 */

class StateManager {

    // 标志关闭是哪里发生的
    enum CloseInitiator {
        NONE,
        SERVER,
        CLIENT
    }

    // WebSocket的状态
    private WebSocketState mState;
    // 初始未关闭
    private CloseInitiator mCloseInitiator = CloseInitiator.NONE;

    public StateManager() {
        mState = WebSocketState.CREATED;
    }

    public WebSocketState getState() {
        return mState;
    }

    public void setState(WebSocketState state) {
        mState = state;
    }

    // 转成关闭状态
    public void changeToClosing(CloseInitiator closeInitiator) {
        mState = WebSocketState.CLOSING;

        // Set the close initiator only when it has not been set yet.
        if (mCloseInitiator == CloseInitiator.NONE) {
            mCloseInitiator = closeInitiator;
        }
    }

    // 是否是服务端关闭的
    public boolean getClosedByServer() {
        return mCloseInitiator == CloseInitiator.SERVER;
    }
}
