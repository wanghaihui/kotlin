package com.imagine.story.common.websocket;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 核心驱动力--最关键的类
 */
public class WebSocket {
    public static final String TAG = WebSocket.class.getSimpleName();

    private static final long DEFAULT_CLOSE_DELAY = 10 * 1000L;

    private final WebSocketFactory mWebSocketFactory;
    // 执行具体的Socket连接
    private final SocketConnector mSocketConnector;
    private final StateManager mStateManager;
    private HandshakeBuilder mHandshakeBuilder;
    // 状态监听器
    private final ListenerManager mListenerManager;
    private final PingSender mPingSender;
    private final PongSender mPongSender;

    // 输入输出流--Socket
    private WebSocketInputStream mInput;
    private WebSocketOutputStream mOutput;

    private Map<String, List<String>> mServerHeaders;

    private PerMessageCompressionExtension mPerMessageCompressionExtension;

    private List<WebSocketExtension> mAgreedExtensions;

    private final Object mThreadsLock = new Object();

    private ReadingThread mReadingThread;
    private WritingThread mWritingThread;

    private boolean mReadingThreadStarted;
    private boolean mWritingThreadStarted;
    private boolean mReadingThreadFinished;
    private boolean mWritingThreadFinished;

    private final Object mOnConnectedCalledLock = new Object();
    private boolean mOnConnectedCalled;

    private boolean mExtended;

    private boolean mMissingCloseFrameAllowed = true;

    private int mMaxPayloadSize;

    private WebSocketFrame mServerCloseFrame;
    private WebSocketFrame mClientCloseFrame;

    private int mFrameQueueSize;

    private boolean mAutoFlush = true;

    private String mAgreedProtocol;

    WebSocket(WebSocketFactory factory, boolean secure, String userInfo,
              String host, String path, SocketConnector connector) {
        // for recreate and etc.
        mWebSocketFactory  = factory;
        mSocketConnector   = connector;
        mStateManager      = new StateManager();
        mHandshakeBuilder  = new HandshakeBuilder(secure, userInfo, host, path);
        mListenerManager   = new ListenerManager(this);
        mPingSender        = new PingSender(this, new CounterPayloadGenerator());
        mPongSender        = new PongSender(this, new CounterPayloadGenerator());
    }

    public WebSocket recreate() throws IOException {
        return recreate(mSocketConnector.getConnectionTimeout());
    }

    public WebSocket recreate(int timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException("The given timeout value is negative.");
        }

        WebSocket instance = mWebSocketFactory.createSocket(getURI(), timeout);

        // Copy the settings.
        instance.mHandshakeBuilder = new HandshakeBuilder(mHandshakeBuilder);
        instance.setPingInterval(getPingInterval());
        instance.setPongInterval(getPongInterval());
        instance.setPingPayloadGenerator(getPingPayloadGenerator());
        instance.setPongPayloadGenerator(getPongPayloadGenerator());
        instance.mExtended = mExtended;
        instance.mAutoFlush = mAutoFlush;
        instance.mMissingCloseFrameAllowed = mMissingCloseFrameAllowed;
        instance.mFrameQueueSize = mFrameQueueSize;

        // Copy listeners.
        List<WebSocketListener> listeners = mListenerManager.getListeners();
        synchronized (listeners) {
            instance.addListeners(listeners);
        }

        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        if (isInState(WebSocketState.CREATED)) {
            // The raw socket needs to be closed.
            finish();
        }
        super.finalize();
    }

    public WebSocket addListener(WebSocketListener listener) {
        mListenerManager.addListener(listener);
        return this;
    }

    public WebSocket addListeners(List<WebSocketListener> listeners) {
        mListenerManager.addListeners(listeners);
        return this;
    }

    public WebSocket removeListener(WebSocketListener listener) {
        mListenerManager.removeListener(listener);
        return this;
    }

    public WebSocket removeListeners(List<WebSocketListener> listeners) {
        mListenerManager.removeListeners(listeners);
        return this;
    }

    public WebSocket clearListeners() {
        mListenerManager.clearListeners();
        return this;
    }

    /**
     * 最重要的连接过程
     * @throws WebSocketException 抛出异常
     */
    public WebSocket connect() throws WebSocketException {
        // Change the state to CONNECTING. If the state before the change is not CREATED, an exception is thrown.
        changeStateOnConnect();

        // HTTP headers from the server -- 所有的HTTP头部
        Map<String, List<String>> headers;

        try {
            // Connect to the server -- 连接到服务器
            mSocketConnector.connect();

            // Perform WebSocket handshake
            headers = shakeHands();

            Log.d(TAG, "hand shake successful");
        } catch (WebSocketException e) {
            // Close the socket.
            mSocketConnector.closeSilently();

            // Change the state to CLOSED.
            mStateManager.setState(WebSocketState.CLOSED);
            // Notify the listener of the state change.
            mListenerManager.callOnStateChanged(WebSocketState.CLOSED);

            // The handshake failed.
            throw e;
        }

        // HTTP headers in the response from the server.
        mServerHeaders = headers;

        // Extensions -- 扩展
        mPerMessageCompressionExtension = findAgreedPerMessageCompressionExtension();

        // Change the state to OPEN.
        mStateManager.setState(WebSocketState.OPEN);
        // Notify the listener of the state change.
        mListenerManager.callOnStateChanged(WebSocketState.OPEN);

        // Start threads that communicate with the server.
        Log.d(TAG, "start reading and writing threads");
        startThreads();

        return this;
    }

    /**
     * Perform the opening handshake.
     */
    private Map<String, List<String>> shakeHands() throws WebSocketException {
        // The raw socket created by WebSocketFactory.
        Socket socket = mSocketConnector.getSocket();
        // Get the input stream of the socket.
        WebSocketInputStream input = openInputStream(socket);
        // Get the output stream of the socket.
        WebSocketOutputStream output = openOutputStream(socket);
        // Generate a value for Sec-WebSocket-Key.
        String key = generateWebSocketKey();

        // Send an opening handshake to the server.
        writeHandshake(output, key);
        // Read the response from the server.
        Map<String, List<String>> headers = readHandshake(input, key);

        // Keep the input stream and the output stream to pass them
        // to the reading thread and the writing thread later.
        mInput  = input;
        mOutput = output;

        // The handshake succeeded.
        return headers;
    }

    /**
     * Open the input stream of the WebSocket connection.
     * The stream is used by the reading thread.
     */
    private WebSocketInputStream openInputStream(Socket socket) throws WebSocketException {
        try {
            // Get the input stream of the raw socket through which
            // this client receives data from the server.
            return new WebSocketInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            // Failed to get the input stream of the raw socket.
            throw new WebSocketException(WebSocketError.SOCKET_INPUT_STREAM_FAILURE,
                    "Failed to get the input stream of the raw socket: " + e.getMessage(), e);
        }
    }

    /**
     * Open the output stream of the WebSocket connection.
     * The stream is used by the writing thread.
     */
    private WebSocketOutputStream openOutputStream(Socket socket) throws WebSocketException {
        try {
            // Get the output stream of the socket through which
            // this client sends data to the server.
            return new WebSocketOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            // Failed to get the output stream from the raw socket.
            throw new WebSocketException(WebSocketError.SOCKET_OUTPUT_STREAM_FAILURE,
                    "Failed to get the output stream from the raw socket: " + e.getMessage(), e);
        }
    }

    private static String generateWebSocketKey() {
        // "16-byte value"
        byte[] data = new byte[16];
        // "randomly selected"
        Misc.nextBytes(data);
        // "base64-encoded"
        return Base64.encode(data);
    }

    private void writeHandshake(WebSocketOutputStream output, String key) throws WebSocketException {
        // Generate an opening handshake sent to the server from this client.
        mHandshakeBuilder.setKey(key);
        String requestLine = mHandshakeBuilder.buildRequestLine();
        List<String[]> headers = mHandshakeBuilder.buildHeaders();
        String handshake = HandshakeBuilder.build(requestLine, headers);

        // Call onSendingHandshake() method of listeners.
        mListenerManager.callOnSendingHandshake(requestLine, headers);

        try {
            // Send the opening handshake to the server.
            output.write(handshake);
            output.flush();
        } catch (IOException e) {
            // Failed to send an opening handshake request to the server.
            throw new WebSocketException(WebSocketError.OPENING_HAHDSHAKE_REQUEST_FAILURE,
                    "Failed to send an opening handshake request to the server: " + e.getMessage(), e);
        }
    }

    /**
     * Receive an opening handshake response from the WebSocket server.
     */
    private Map<String, List<String>> readHandshake(WebSocketInputStream input, String key) throws WebSocketException {
        return new HandshakeReader(this).readHandshake(input, key);
    }

    /**
     * Find a per-message compression extension from among the agreed extensions.
     */
    private PerMessageCompressionExtension findAgreedPerMessageCompressionExtension() {
        if (mAgreedExtensions == null) {
            return null;
        }

        for (WebSocketExtension extension : mAgreedExtensions) {
            if (extension instanceof PerMessageCompressionExtension) {
                return (PerMessageCompressionExtension) extension;
            }
        }

        return null;
    }

    /**
     * 开启执行线程
     */
    private void startThreads() {
        ReadingThread readingThread = new ReadingThread(this);
        WritingThread writingThread = new WritingThread(this);

        synchronized (mThreadsLock) {
            mReadingThread = readingThread;
            mWritingThread = writingThread;
        }

        // Execute onThreadCreated of the listeners.
        readingThread.callOnThreadCreated();
        writingThread.callOnThreadCreated();

        readingThread.start();
        writingThread.start();
    }

    private void stopThreads(long closeDelay) {
        ReadingThread readingThread;
        WritingThread writingThread;

        synchronized (mThreadsLock) {
            readingThread = mReadingThread;
            writingThread = mWritingThread;

            mReadingThread = null;
            mWritingThread = null;
        }

        if (readingThread != null) {
            readingThread.requestStop(closeDelay);
        }

        if (writingThread != null) {
            writingThread.requestStop();
        }
    }

    void onReadingThreadStarted() {
        boolean bothStarted = false;

        // 线程锁
        synchronized (mThreadsLock) {
            mReadingThreadStarted = true;

            if (mWritingThreadStarted) {
                // Both the reading thread and the writing thread have started.
                bothStarted = true;
            }
        }

        // Call onConnected() method of listeners if not called yet.
        callOnConnectedIfNotYet();

        // If both the reading thread and the writing thread have started.
        if (bothStarted) {
            onThreadsStarted();
        }
    }

    private void callOnConnectedIfNotYet() {
        synchronized (mOnConnectedCalledLock) {
            // If onConnected() has already been called.
            if (mOnConnectedCalled) {
                // Do not call onConnected() twice.
                return;
            }

            mOnConnectedCalled = true;
        }

        // Notify the listeners that the handshake succeeded.
        mListenerManager.callOnConnected(mServerHeaders);
    }

    private void onThreadsStarted() {
        // Start sending ping frames periodically.
        // If the interval is zero, this call does nothing.
        mPingSender.start();

        // Likewise, start the pong sender.
        mPongSender.start();
    }

    private void changeStateOnConnect() throws WebSocketException {
        // 状态同步
        synchronized (mStateManager) {
            // If the current state is not CREATED.
            if (mStateManager.getState() != WebSocketState.CREATED) {
                throw new WebSocketException(WebSocketError.NOT_IN_CREATED_STATE, "The current state of the WebSocket is not CREATED");
            }

            // Change the state to CONNECTING.
            mStateManager.setState(WebSocketState.CONNECTING);
        }

        // Notify the listeners of the state change -- 通知监听器状态改变
        mListenerManager.callOnStateChanged(WebSocketState.CONNECTING);
    }

    ListenerManager getListenerManager() {
        return mListenerManager;
    }

    public WebSocket sendFrame(WebSocketFrame frame) {
        if (frame == null) {
            return this;
        }

        synchronized (mStateManager) {
            WebSocketState state = mStateManager.getState();

            if (state != WebSocketState.OPEN && state != WebSocketState.CLOSING) {
                return this;
            }
        }

        // The current state is either OPEN or CLOSING. Or, CLOSED.

        // Get the reference to the writing thread.
        WritingThread wt = mWritingThread;

        // Some applications call sendFrame() without waiting for the
        // notification of WebSocketListener.onConnected() (Issue #23),
        // and/or even after the connection is closed. That is, there
        // are chances that sendFrame() is called when mWritingThread
        // is null. So, it should be checked whether an instance of
        // WritingThread is available or not before calling queueFrame().
        if (wt == null) {
            // An instance of WritingThread is not available.
            return this;
        }

        // Split the frame into multiple frames if necessary.
        List<WebSocketFrame> frames = splitIfNecessary(frame);

        // Queue the frame or the frames. Even if the current state is
        // CLOSED, queueing won't be a big issue.

        // If the frame was not split.
        if (frames == null) {
            // Queue the frame.
            wt.queueFrame(frame);
        } else {
            for (WebSocketFrame f : frames) {
                // Queue the frame.
                wt.queueFrame(f);
            }
        }

        return this;
    }

    private List<WebSocketFrame> splitIfNecessary(WebSocketFrame frame) {
        return WebSocketFrame.splitIfNecessary(frame, mMaxPayloadSize, mPerMessageCompressionExtension);
    }

    void onReadingThreadFinished(WebSocketFrame closeFrame) {
        synchronized (mThreadsLock) {
            mReadingThreadFinished = true;
            mServerCloseFrame = closeFrame;

            if (!mWritingThreadFinished) {
                // Wait for the writing thread to finish.
                return;
            }
        }

        // Both the reading thread and the writing thread have finished.
        onThreadsFinished();
    }

    WebSocketInputStream getInput() {
        return mInput;
    }

    WebSocketOutputStream getOutput() {
        return mOutput;
    }

    public boolean isExtended() {
        return mExtended;
    }
    public WebSocket setExtended(boolean extended) {
        mExtended = extended;
        return this;
    }

    public boolean isMissingCloseFrameAllowed() {
        return mMissingCloseFrameAllowed;
    }
    public WebSocket setMissingCloseFrameAllowed(boolean allowed) {
        mMissingCloseFrameAllowed = allowed;
        return this;
    }

    StateManager getStateManager() {
        return mStateManager;
    }

    public Socket getSocket() {
        return mSocketConnector.getSocket();
    }

    PerMessageCompressionExtension getPerMessageCompressionExtension() {
        return mPerMessageCompressionExtension;
    }

    HandshakeBuilder getHandshakeBuilder() {
        return mHandshakeBuilder;
    }

    public int getFrameQueueSize() {
        return mFrameQueueSize;
    }

    public WebSocket setFrameQueueSize(int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative.");
        }
        mFrameQueueSize = size;
        return this;
    }

    public boolean isOpen() {
        return isInState(WebSocketState.OPEN);
    }

    private boolean isInState(WebSocketState state) {
        synchronized (mStateManager) {
            return (mStateManager.getState() == state);
        }
    }

    public boolean isAutoFlush() {
        return mAutoFlush;
    }
    public WebSocket setAutoFlush(boolean auto) {
        mAutoFlush = auto;
        return this;
    }

    void setAgreedExtensions(List<WebSocketExtension> extensions) {
        mAgreedExtensions = extensions;
    }

    void setAgreedProtocol(String protocol) {
        mAgreedProtocol = protocol;
    }

    public URI getURI() {
        return mHandshakeBuilder.getURI();
    }

    public WebSocket setPingInterval(long interval) {
        mPingSender.setInterval(interval);
        return this;
    }
    public long getPingInterval() {
        return mPingSender.getInterval();
    }

    public WebSocket setPongInterval(long interval) {
        mPongSender.setInterval(interval);
        return this;
    }
    public long getPongInterval() {
        return mPongSender.getInterval();
    }

    public WebSocket setPingPayloadGenerator(PayloadGenerator generator) {
        mPingSender.setPayloadGenerator(generator);
        return this;
    }
    public PayloadGenerator getPingPayloadGenerator() {
        return mPingSender.getPayloadGenerator();
    }

    public WebSocket setPongPayloadGenerator(PayloadGenerator generator) {
        mPongSender.setPayloadGenerator(generator);
        return this;
    }
    public PayloadGenerator getPongPayloadGenerator() {
        return mPongSender.getPayloadGenerator();
    }

    public WebSocketState getState() {
        synchronized (mStateManager) {
            return mStateManager.getState();
        }
    }

    public WebSocket addProtocol(String protocol) {
        mHandshakeBuilder.addProtocol(protocol);
        return this;
    }

    public WebSocket removeProtocol(String protocol) {
        mHandshakeBuilder.removeProtocol(protocol);
        return this;
    }

    public WebSocket clearProtocols() {
        mHandshakeBuilder.clearProtocols();
        return this;
    }

    public WebSocket addExtension(WebSocketExtension extension) {
        mHandshakeBuilder.addExtension(extension);
        return this;
    }

    public WebSocket addExtension(String extension) {
        mHandshakeBuilder.addExtension(extension);
        return this;
    }

    public WebSocket removeExtension(WebSocketExtension extension) {
        mHandshakeBuilder.removeExtension(extension);
        return this;
    }

    public WebSocket removeExtensions(String name) {
        mHandshakeBuilder.removeExtensions(name);
        return this;
    }

    public WebSocket clearExtensions() {
        mHandshakeBuilder.clearExtensions();
        return this;
    }

    public WebSocket addHeader(String name, String value) {
        mHandshakeBuilder.addHeader(name, value);
        return this;
    }

    public WebSocket removeHeaders(String name) {
        mHandshakeBuilder.removeHeaders(name);
        return this;
    }

    public WebSocket clearHeaders() {
        mHandshakeBuilder.clearHeaders();
        return this;
    }

    public WebSocket setUserInfo(String userInfo) {
        mHandshakeBuilder.setUserInfo(userInfo);
        return this;
    }

    public WebSocket setUserInfo(String id, String password) {
        mHandshakeBuilder.setUserInfo(id, password);
        return this;
    }

    public WebSocket clearUserInfo() {
        mHandshakeBuilder.clearUserInfo();
        return this;
    }

    public int getMaxPayloadSize() {
        return mMaxPayloadSize;
    }

    public WebSocket setMaxPayloadSize(int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative.");
        }
        mMaxPayloadSize = size;
        return this;
    }

    public List<WebSocketExtension> getAgreedExtensions() {
        return mAgreedExtensions;
    }

    public String getAgreedProtocol() {
        return mAgreedProtocol;
    }

    public WebSocket flush() {
        synchronized (mStateManager) {
            WebSocketState state = mStateManager.getState();

            if (state != WebSocketState.OPEN && state != WebSocketState.CLOSING) {
                return this;
            }
        }

        // Get the reference to the instance of WritingThread.
        WritingThread wt = mWritingThread;

        // If and only if an instance of WritingThread is available.
        if (wt != null) {
            // Request flush.
            wt.queueFlush();
        }

        return this;
    }

    public Future<WebSocket> connect(ExecutorService executorService) {
        return executorService.submit(connectable());
    }

    public Callable<WebSocket> connectable() {
        return new Connectable(this);
    }

    public WebSocket connectAsynchronously() {
        Thread thread = new ConnectThread(this);

        // Get the reference (just in case)
        ListenerManager lm = mListenerManager;

        if (lm != null) {
            lm.callOnThreadCreated(ThreadType.CONNECT_THREAD, thread);
        }

        thread.start();

        return this;
    }

    void onWritingThreadStarted() {
        boolean bothStarted = false;

        synchronized (mThreadsLock) {
            mWritingThreadStarted = true;

            if (mReadingThreadStarted) {
                // Both the reading thread and the writing thread have started.
                bothStarted = true;
            }
        }

        // Call onConnected() method of listeners if not called yet.
        callOnConnectedIfNotYet();

        // If both the reading thread and the writing thread have started.
        if (bothStarted) {
            onThreadsStarted();
        }
    }

    void onWritingThreadFinished(WebSocketFrame closeFrame) {
        synchronized (mThreadsLock) {
            mWritingThreadFinished = true;
            mClientCloseFrame = closeFrame;

            if (!mReadingThreadFinished) {
                // Wait for the reading thread to finish.
                return;
            }
        }

        // Both the reading thread and the writing thread have finished.
        onThreadsFinished();
    }

    private void onThreadsFinished() {
        finish();
    }

    void finish() {
        // Stop the ping sender and the pong sender.
        mPingSender.stop();
        mPongSender.stop();

        try {
            // Close the raw socket.
            mSocketConnector.getSocket().close();
        } catch (Throwable t) {
            // Ignore any error raised by close().
        }

        synchronized (mStateManager) {
            // Change the state to CLOSED.
            mStateManager.setState(WebSocketState.CLOSED);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(WebSocketState.CLOSED);

        // Notify the listeners that the WebSocket was disconnected.
        mListenerManager.callOnDisconnected(mServerCloseFrame, mClientCloseFrame, mStateManager.getClosedByServer());
    }

    public WebSocket disconnect() {
        return disconnect(WebSocketCloseCode.NORMAL, null);
    }

    public WebSocket disconnect(int closeCode) {
        return disconnect(closeCode, null);
    }

    public WebSocket disconnect(String reason) {
        return disconnect(WebSocketCloseCode.NORMAL, reason);
    }

    public WebSocket disconnect(int closeCode, String reason) {
        return disconnect(closeCode, reason, DEFAULT_CLOSE_DELAY);
    }

    public WebSocket disconnect(int closeCode, String reason, long closeDelay) {
        synchronized (mStateManager) {
            switch (mStateManager.getState()) {
                case CREATED:
                    finishAsynchronously();
                    return this;

                case OPEN:
                    break;

                default:
                    // - CONNECTING
                    //     It won't happen unless the programmer dare call
                    //     open() and disconnect() in parallel.
                    //
                    // - CLOSING
                    //     A closing handshake has already been started.
                    //
                    // - CLOSED
                    //     The connection has already been closed.
                    return this;
            }

            // Change the state to CLOSING.
            mStateManager.changeToClosing(StateManager.CloseInitiator.CLIENT);

            // Create a close frame.
            WebSocketFrame frame = WebSocketFrame.createCloseFrame(closeCode, reason);

            // Send the close frame to the server.
            sendFrame(frame);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(WebSocketState.CLOSING);

        // If a negative value is given.
        if (closeDelay < 0)
        {
            // Use the default value.
            closeDelay = DEFAULT_CLOSE_DELAY;
        }

        // Request the threads to stop.
        stopThreads(closeDelay);

        return this;
    }

    private void finishAsynchronously() {
        WebSocketThread thread = new FinishThread(this);

        // Execute onThreadCreated() of the listeners.
        thread.callOnThreadCreated();

        thread.start();
    }

    public WebSocket sendContinuation() {
        return sendFrame(WebSocketFrame.createContinuationFrame());
    }

    public WebSocket sendContinuation(boolean fin) {
        return sendFrame(WebSocketFrame.createContinuationFrame().setFin(fin));
    }

    public WebSocket sendContinuation(String payload) {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload));
    }

    public WebSocket sendContinuation(String payload, boolean fin) {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload).setFin(fin));
    }

    public WebSocket sendContinuation(byte[] payload) {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload));
    }

    public WebSocket sendContinuation(byte[] payload, boolean fin) {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload).setFin(fin));
    }

    public WebSocket sendText(String message) {
        return sendFrame(WebSocketFrame.createTextFrame(message));
    }

    public WebSocket sendText(String payload, boolean fin) {
        return sendFrame(WebSocketFrame.createTextFrame(payload).setFin(fin));
    }

    public WebSocket sendBinary(byte[] message) {
        return sendFrame(WebSocketFrame.createBinaryFrame(message));
    }

    public WebSocket sendBinary(byte[] payload, boolean fin) {
        return sendFrame(WebSocketFrame.createBinaryFrame(payload).setFin(fin));
    }

    public WebSocket sendClose() {
        return sendFrame(WebSocketFrame.createCloseFrame());
    }

    public WebSocket sendClose(int closeCode) {
        return sendFrame(WebSocketFrame.createCloseFrame(closeCode));
    }

    public WebSocket sendClose(int closeCode, String reason) {
        return sendFrame(WebSocketFrame.createCloseFrame(closeCode, reason));
    }

    public WebSocket sendPing() {
        return sendFrame(WebSocketFrame.createPingFrame());
    }

    public WebSocket sendPing(byte[] payload) {
        return sendFrame(WebSocketFrame.createPingFrame(payload));
    }

    public WebSocket sendPing(String payload) {
        return sendFrame(WebSocketFrame.createPingFrame(payload));
    }

    public WebSocket sendPong() {
        return sendFrame(WebSocketFrame.createPongFrame());
    }

    public WebSocket sendPong(byte[] payload) {
        return sendFrame(WebSocketFrame.createPongFrame(payload));
    }

    public WebSocket sendPong(String payload) {
        return sendFrame(WebSocketFrame.createPongFrame(payload));
    }


}
