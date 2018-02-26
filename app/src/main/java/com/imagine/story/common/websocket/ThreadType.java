package com.imagine.story.common.websocket;

/**
 * Types of threads which are created internally in the implementation.
 */

public enum ThreadType {

    /**
     * A thread which reads WebSocket frames from the server
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/ReadingThread.java'>ReadingThread</a></code>).
     */
    READING_THREAD,

    /**
     * A thread which sends WebSocket frames to the server
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/WritingThread.java'>WritingThread</a></code>).
     */
    WRITING_THREAD,

    /**
     * A thread which calls {@link WebSocket#connect()} asynchronously
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/ConnectThread.java'>ConnectThread</a></code>).
     */
    CONNECT_THREAD,

    /**
     * A thread which does finalization of a {@link WebSocket} instance.
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/FinishThread.java'>FinishThread</a></code>).
     */
    FINISH_THREAD

}
