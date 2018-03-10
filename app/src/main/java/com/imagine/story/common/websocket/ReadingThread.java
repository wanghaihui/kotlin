package com.imagine.story.common.websocket;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class ReadingThread extends WebSocketThread {

    private boolean mStopRequested;
    private List<WebSocketFrame> mContinuation = new ArrayList<>();
    private boolean mNotWaitForCloseFrame;
    private WebSocketFrame mCloseFrame;

    // Message压缩扩展
    private final PerMessageCompressionExtension mPMCE;
    private final Object mCloseLock = new Object();

    private Timer mCloseTimer;
    private CloseTask mCloseTask;

    private long mCloseDelay;

    public ReadingThread(WebSocket websocket) {
        super("ReadingThread", websocket, ThreadType.READING_THREAD);

        mPMCE = websocket.getPerMessageCompressionExtension();
    }

    @Override
    public void runMain() {
        try {
            main();
        } catch (Throwable t) {
            // An uncaught throwable was detected in the reading thread.
            WebSocketException cause = new WebSocketException(WebSocketError.UNEXPECTED_ERROR_IN_READING_THREAD,
                    "An uncaught throwable was detected in the reading thread: " + t.getMessage(), t);

            // Notify the listeners.
            ListenerManager manager = mWebSocket.getListenerManager();
            manager.callOnError(cause);
            manager.callOnUnexpectedError(cause);
        }

        // Notify this reading thread finished.
        notifyFinished();
    }

    private void main() {
        mWebSocket.onReadingThreadStarted();
        Log.d(WebSocket.TAG, "reading thread started");

        while (true) {
            synchronized (this) {
                if (mStopRequested) {
                    break;
                }
            }

            // Receive a frame from the server.
            WebSocketFrame frame = readFrame();

            if (frame == null) {
                // Something unexpected happened.
                Log.e(WebSocket.TAG, "read frame is null");
                break;
            }

            Log.d(WebSocket.TAG, frame.toString());
            // Handle the frame.
            boolean keepReading = handleFrame(frame);

            if (!keepReading) {
                break;
            }
        }

        // Wait for a close frame if one has not been received yet.
        waitForCloseFrame();

        // Cancel a task which calls Socket.close() if running.
        cancelClose();
    }

    private WebSocketFrame readFrame() {
        WebSocketFrame frame = null;
        WebSocketException wse;

        try {
            // Receive a frame from the server.
            frame = mWebSocket.getInput().readFrame();

            // Verify the frame. If invalid, WebSocketException is thrown.
            verifyFrame(frame);

            // Return the verified frame.
            return frame;
        } catch (InterruptedIOException e) {
            if (mStopRequested) {
                // Thread.interrupt() interrupted a blocking socket read operation.
                // This thread has been interrupted intentionally.
                return null;
            } else {
                // Interruption occurred while a frame was being read from the web socket.
                wse = new WebSocketException(WebSocketError.INTERRUPTED_IN_READING,
                        "Interruption occurred while a frame was being read from the web socket: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            if (mStopRequested && isInterrupted()) {
                // Socket.close() interrupted a blocking socket read operation.
                // This thread has been interrupted intentionally.
                return null;
            } else {
                // An I/O error occurred while a frame was being read from the web socket.
                wse = new WebSocketException(WebSocketError.IO_ERROR_IN_READING,
                        "An I/O error occurred while a frame was being read from the web socket: " + e.getMessage(), e);
            }
        } catch (WebSocketException e) {
            // A protocol error.
            wse = e;
        }

        boolean error = true;

        // If the input stream of the WebSocket connection has reached the end
        // without receiving a close frame from the server.
        if (wse instanceof NoMoreFrameException) {
            // Not wait for a close frame in waitForCloseFrame() which will be called later.
            mNotWaitForCloseFrame = true;

            // If the configuration of the WebSocket instance allows the behavior.
            if (mWebSocket.isMissingCloseFrameAllowed()) {
                error = false;
            }
        }

        if (error) {
            // Notify the listeners that an error occurred while a frame was being read.
            callOnError(wse);
            callOnFrameError(wse, frame);
        }

        // Create a close frame.
        WebSocketFrame closeFrame = createCloseFrame(wse);

        // Send the close frame.
        mWebSocket.sendFrame(closeFrame);

        // No WebSocket frame is available.
        return null;
    }

    private boolean handleFrame(WebSocketFrame frame) {
        // Notify the listeners that a frame was received.
        callOnFrame(frame);

        // Dispatch based on the opcode.
        switch (frame.getOpcode()) {
            case WebSocketOpcode.CONTINUATION:
                return handleContinuationFrame(frame);

            case WebSocketOpcode.TEXT:
                return handleTextFrame(frame);

            case WebSocketOpcode.BINARY:
                return handleBinaryFrame(frame);

            case WebSocketOpcode.CLOSE:
                return handleCloseFrame(frame);

            case WebSocketOpcode.PING:
                return handlePingFrame(frame);

            case WebSocketOpcode.PONG:
                return handlePongFrame(frame);

            default:
                // Ignore the frame whose opcode is unknown. Keep reading.
                return true;
        }
    }

    private void verifyFrame(WebSocketFrame frame) throws WebSocketException {
        // Verify RSV1, RSV2 and RSV3.
        verifyReservedBits(frame);

        // The opcode of the frame must be known.
        verifyFrameOpcode(frame);

        // Frames from the server must not be masked.
        verifyFrameMask(frame);

        // Verify fragmentation conditions.
        verifyFrameFragmentation(frame);

        // Verify the size of the payload.
        verifyFrameSize(frame);
    }

    private void verifyReservedBits(WebSocketFrame frame) throws WebSocketException {
        // If extended use of web socket frames is allowed.
        if (mWebSocket.isExtended()) {
            // Do not check RSV1/RSV2/RSV3 bits.
            return;
        }

        // RSV1, RSV2, RSV3
        //
        // The specification requires that these bits "be 0 unless an extension
        // is negotiated that defines meanings for non-zero values".

        verifyReservedBit1(frame);
        verifyReservedBit2(frame);
        verifyReservedBit3(frame);
    }

    /**
     * Verify the RSV1 bit of a frame.
     */
    private void verifyReservedBit1(WebSocketFrame frame) throws WebSocketException {
        // If a per-message compression extension has been agreed.
        if (mPMCE != null) {
            // Verify the RSV1 bit using the rule described in RFC 7692.
            boolean verified = verifyReservedBit1ForPMCE(frame);

            if (verified) {
                return;
            }
        }

        if (!frame.getRsv1()) {
            // No problem.
            return;
        }

        // The RSV1 bit of a frame is set unexpectedly.
        throw new WebSocketException(WebSocketError.UNEXPECTED_RESERVED_BIT, "The RSV1 bit of a frame is set unexpectedly.");
    }

    private boolean verifyReservedBit1ForPMCE(WebSocketFrame frame) throws WebSocketException {
        if (frame.isTextFrame() || frame.isBinaryFrame()) {
            // The RSV1 of the first frame of a message is called
            // "Per-Message Compressed" bit. It can be either 0 or 1.
            // In other words, any value is okay.
            return true;
        }

        // Further checking is required.
        return false;
    }

    private void verifyReservedBit2(WebSocketFrame frame) throws WebSocketException {
        if (!frame.getRsv2()) {
            // No problem.
            return;
        }

        // The RSV2 bit of a frame is set unexpectedly.
        throw new WebSocketException(WebSocketError.UNEXPECTED_RESERVED_BIT, "The RSV2 bit of a frame is set unexpectedly.");
    }

    private void verifyReservedBit3(WebSocketFrame frame) throws WebSocketException {
        if (!frame.getRsv3()) {
            // No problem.
            return;
        }

        // The RSV3 bit of a frame is set unexpectedly.
        throw new WebSocketException(WebSocketError.UNEXPECTED_RESERVED_BIT, "The RSV3 bit of a frame is set unexpectedly.");
    }

    private void verifyFrameOpcode(WebSocketFrame frame) throws WebSocketException {
        switch (frame.getOpcode()) {
            case WebSocketOpcode.CONTINUATION:
            case WebSocketOpcode.TEXT:
            case WebSocketOpcode.BINARY:
            case WebSocketOpcode.CLOSE:
            case WebSocketOpcode.PING:
            case WebSocketOpcode.PONG:
                // Known opcode
                return;

            default:
                break;
        }

        // If extended use of web socket frames is allowed.
        if (mWebSocket.isExtended()) {
            // Allow the unknown opcode.
            return;
        }

        // A frame has an unknown opcode.
        throw new WebSocketException(WebSocketError.UNKNOWN_OPCODE,
                "A frame has an unknown opcode: 0x" + Integer.toHexString(frame.getOpcode()));
    }

    private void verifyFrameMask(WebSocketFrame frame) throws WebSocketException {
        // If the frame is masked.
        if (frame.getMask()) {
            // A frame from the server is masked.
            throw new WebSocketException(WebSocketError.FRAME_MASKED,
                    "A frame from the server is masked.");
        }
    }

    private void verifyFrameFragmentation(WebSocketFrame frame) throws WebSocketException {
        // Control frames (see Section 5.5) MAY be injected in the
        // middle of a fragmented message. Control frames themselves
        // MUST NOT be fragmented.
        if (frame.isControlFrame()) {
            // If fragmented.
            if (!frame.getFin()) {
                // A control frame is fragmented.
                throw new WebSocketException(WebSocketError.FRAGMENTED_CONTROL_FRAME,
                        "A control frame is fragmented.");
            }

            // No more requirements on a control frame.
            return;
        }

        // True if a continuation has already started.
        boolean continuationExists = (mContinuation.size() != 0);

        // If the frame is a continuation frame.
        if (frame.isContinuationFrame()) {
            // There must already exist a continuation sequence.
            if (!continuationExists) {
                // A continuation frame was detected although a continuation had not started.
                throw new WebSocketException(WebSocketError.UNEXPECTED_CONTINUATION_FRAME,
                        "A continuation frame was detected although a continuation had not started.");
            }

            // No more requirements on a continuation frame.
            return;
        }

        // A data frame.

        if (continuationExists) {
            // A non-control frame was detected although the existing continuation had not been closed.
            throw new WebSocketException(WebSocketError.CONTINUATION_NOT_CLOSED,
                    "A non-control frame was detected although the existing continuation had not been closed.");
        }
    }

    private void verifyFrameSize(WebSocketFrame frame) throws WebSocketException {
        // If the frame is not a control frame.
        if (!frame.isControlFrame()) {
            // Nothing to check.
            return;
        }

        // RFC 6455, 5.5. Control Frames.
        //
        //   All control frames MUST have a payload length of 125 bytes or less
        //   and MUST NOT be fragmented.
        //

        byte[] payload = frame.getPayload();

        if (payload == null) {
            // The frame does not have payload.
            return;
        }

        if (125 < payload.length) {
            // The payload size of a control frame exceeds the maximum size (125 bytes).
            throw new WebSocketException(WebSocketError.TOO_LONG_CONTROL_FRAME_PAYLOAD,
                    "The payload size of a control frame exceeds the maximum size (125 bytes): " + payload.length);
        }
    }

    private void callOnError(WebSocketException cause) {
        mWebSocket.getListenerManager().callOnError(cause);
    }

    private void callOnFrameError(WebSocketException cause, WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnFrameError(cause, frame);
    }

    private WebSocketFrame createCloseFrame(WebSocketException wse)
    {
        int closeCode;

        switch (wse.getError())
        {
            // In WebSocketInputStream.readFrame()

            case INSUFFICENT_DATA:
            case INVALID_PAYLOAD_LENGTH:
            case NO_MORE_FRAME:
                closeCode = WebSocketCloseCode.UNCONFORMED;
                break;

            case TOO_LONG_PAYLOAD:
            case INSUFFICIENT_MEMORY_FOR_PAYLOAD:
                closeCode = WebSocketCloseCode.OVERSIZE;
                break;

            // In this.verifyFrame(WebSocketFrame)

            case NON_ZERO_RESERVED_BITS:
            case UNEXPECTED_RESERVED_BIT:
            case UNKNOWN_OPCODE:
            case FRAME_MASKED:
            case FRAGMENTED_CONTROL_FRAME:
            case UNEXPECTED_CONTINUATION_FRAME:
            case CONTINUATION_NOT_CLOSED:
            case TOO_LONG_CONTROL_FRAME_PAYLOAD:
                closeCode = WebSocketCloseCode.UNCONFORMED;
                break;

            // In this.readFrame()

            case INTERRUPTED_IN_READING:
            case IO_ERROR_IN_READING:
                closeCode = WebSocketCloseCode.VIOLATED;
                break;

            // Others (unexpected)

            default:
                closeCode = WebSocketCloseCode.VIOLATED;
                break;
        }

        return WebSocketFrame.createCloseFrame(closeCode, wse.getMessage());
    }

    private void callOnFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnFrame(frame);
    }

    private boolean handleContinuationFrame(WebSocketFrame frame) {
        // Notify the listeners that a continuation frame was received.
        callOnContinuationFrame(frame);

        // Append the continuation frame to the existing continuation sequence.
        mContinuation.add(frame);

        // If the frame is not the last one for the continuation.
        if (!frame.getFin()) {
            // Keep reading.
            return true;
        }

        // Concatenate payloads of the frames. Decompression is performed
        // when necessary.
        byte[] data = getMessage(mContinuation);

        // If the concatenation failed.
        if (data == null) {
            // Stop reading.
            return false;
        }

        // If the continuation forms a text message.
        if (mContinuation.get(0).isTextFrame()) {
            // Notify the listeners that a text message was received.
            callOnTextMessage(data);
        } else {
            // Notify the listeners that a binary message was received.
            callOnBinaryMessage(data);
        }

        // Clear the continuation.
        mContinuation.clear();

        // Keep reading.
        return true;
    }

    private byte[] getMessage(List<WebSocketFrame> frames) {
        // Concatenate payloads of the frames.
        byte[] data = concatenatePayloads(mContinuation);

        // If the concatenation failed.
        if (data == null) {
            // Stop reading.
            return null;
        }

        // If a per-message compression extension is enabled and
        // the Per-Message Compressed bit of the first frame is set.
        if (mPMCE != null && frames.get(0).getRsv1()) {
            // Decompress the data.
            data = decompress(data);
        }

        return data;
    }

    private byte[] concatenatePayloads(List<WebSocketFrame> frames) {
        Throwable cause;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // For each web socket frame.
            for (WebSocketFrame frame : frames) {
                // Get the payload of the frame.
                byte[] payload = frame.getPayload();

                // If the payload is null or empty.
                if (payload == null || payload.length == 0) {
                    continue;
                }

                // Append the payload.
                baos.write(payload);
            }

            // Return the concatenated byte array.
            return baos.toByteArray();
        } catch (IOException e) {
            cause = e;
        } catch (OutOfMemoryError e) {
            cause = e;
        }

        // Create a WebSocketException which has a cause.
        WebSocketException wse = new WebSocketException(WebSocketError.MESSAGE_CONSTRUCTION_ERROR,
                "Failed to concatenate payloads of multiple frames to construct a message: " + cause.getMessage(), cause);

        // Notify the listeners that message construction failed.
        callOnError(wse);
        callOnMessageError(wse, frames);

        // Create a close frame with a close code of 1009 which
        // indicates that the message is too big to process.
        WebSocketFrame frame = WebSocketFrame
                .createCloseFrame(WebSocketCloseCode.OVERSIZE, wse.getMessage());

        // Send the close frame.
        mWebSocket.sendFrame(frame);

        // Failed to construct a message.
        return null;
    }

    private void callOnContinuationFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnContinuationFrame(frame);
    }

    private void callOnTextMessage(byte[] data) {
        try {
            // Interpret the byte array as a string.
            // OutOfMemoryError may happen when the size of data is too big.
            String message = Misc.toStringUTF8(data);

            // Call onTextMessage() method of the listeners.
            callOnTextMessage(message);
        } catch (Throwable t) {
            // Failed to convert payload data into a string.
            WebSocketException wse = new WebSocketException(WebSocketError.TEXT_MESSAGE_CONSTRUCTION_ERROR,
                    "Failed to convert payload data into a string: " + t.getMessage(), t);

            // Notify the listeners that text message construction failed.
            callOnError(wse);
            callOnTextMessageError(wse, data);
        }
    }

    private void callOnTextMessage(String message) {
        mWebSocket.getListenerManager().callOnTextMessage(message);
    }

    private void callOnBinaryMessage(byte[] message) {
        mWebSocket.getListenerManager().callOnBinaryMessage(message);
    }

    private void callOnMessageError(WebSocketException cause, List<WebSocketFrame> frames) {
        mWebSocket.getListenerManager().callOnMessageError(cause, frames);
    }

    private void callOnTextMessageError(WebSocketException cause, byte[] data) {
        mWebSocket.getListenerManager().callOnTextMessageError(cause, data);
    }

    private byte[] decompress(byte[] input) {
        WebSocketException wse;

        try {
            // Decompress the message.
            return mPMCE.decompress(input);
        } catch (WebSocketException e) {
            wse = e;
        }

        // Notify the listeners that decompression failed.
        callOnError(wse);
        callOnMessageDecompressionError(wse, input);

        // Create a close frame with a close code of 1003 which
        // indicates that the message cannot be accepted.
        WebSocketFrame frame = WebSocketFrame
                .createCloseFrame(WebSocketCloseCode.UNACCEPTABLE, wse.getMessage());

        // Send the close frame.
        mWebSocket.sendFrame(frame);

        // Failed to construct a message.
        return null;
    }

    private void callOnMessageDecompressionError(WebSocketException cause, byte[] compressed) {
        mWebSocket.getListenerManager().callOnMessageDecompressionError(cause, compressed);
    }

    private boolean handleTextFrame(WebSocketFrame frame) {
        // Notify the listeners that a text frame was received.
        callOnTextFrame(frame);

        // If the frame indicates the start of fragmentation.
        if (!frame.getFin()) {
            // Start a continuation sequence.
            mContinuation.add(frame);

            // Keep reading.
            return true;
        }

        // Get the payload of the frame. Decompression is performed
        // when necessary.
        byte[] payload = getMessage(frame);

        // Notify the listeners that a text message was received.
        callOnTextMessage(payload);

        // Keep reading.
        return true;
    }


    private boolean handleBinaryFrame(WebSocketFrame frame) {
        // Notify the listeners that a binary frame was received.
        callOnBinaryFrame(frame);

        // If the frame indicates the start of fragmentation.
        if (!frame.getFin()) {
            // Start a continuation sequence.
            mContinuation.add(frame);

            // Keep reading.
            return true;
        }

        // Get the payload of the frame. Decompression is performed
        // when necessary.
        byte[] payload = getMessage(frame);

        // Notify the listeners that a binary message was received.
        callOnBinaryMessage(payload);

        // Keep reading.
        return true;
    }


    private boolean handleCloseFrame(WebSocketFrame frame) {
        // Get the manager which manages the state of the web socket.
        StateManager manager = mWebSocket.getStateManager();

        // The close frame sent from the server.
        mCloseFrame = frame;

        boolean stateChanged = false;

        synchronized (manager) {
            // The current state of the web socket.
            WebSocketState state = manager.getState();

            // If the current state is neither CLOSING nor CLOSED.
            if (state != WebSocketState.CLOSING && state != WebSocketState.CLOSED) {
                // Change the state to CLOSING.
                manager.changeToClosing(StateManager.CloseInitiator.SERVER);

                // This web socket has not sent a close frame yet,
                // so schedule sending a close frame.

                // RFC 6455, 5.5.1. Close
                //
                //   When sending a Close frame in response, the endpoint
                //   typically echos the status code it received.
                //

                // Simply reuse the frame.
                mWebSocket.sendFrame(frame);

                stateChanged = true;
            }
        }

        if (stateChanged)
        {
            // Notify the listeners of the state change.
            mWebSocket.getListenerManager().callOnStateChanged(WebSocketState.CLOSING);
        }

        // Notify the listeners that a close frame was received.
        callOnCloseFrame(frame);

        // Stop reading.
        return false;
    }


    private boolean handlePingFrame(WebSocketFrame frame) {
        // Notify the listeners that a ping frame was received.
        callOnPingFrame(frame);

        // RFC 6455, 5.5.3. Pong
        //
        //   A Pong frame sent in response to a Ping frame must
        //   have identical "Application data" as found in the
        //   message body of the Ping frame being replied to.

        // Create a pong frame which has the same payload as
        // the ping frame.
        WebSocketFrame pong = WebSocketFrame
                .createPongFrame(frame.getPayload());

        // Send the pong frame to the server.
        mWebSocket.sendFrame(pong);

        // Keep reading.
        return true;
    }


    private boolean handlePongFrame(WebSocketFrame frame) {
        // Notify the listeners that a pong frame was received.
        callOnPongFrame(frame);

        // Keep reading.
        return true;
    }

    private void callOnTextFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnTextFrame(frame);
    }

    private void callOnBinaryFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnBinaryFrame(frame);
    }

    private byte[] getMessage(WebSocketFrame frame) {
        // The raw payload of the frame.
        byte[] payload = frame.getPayload();

        // If a per-message compression extension is enabled and
        // the Per-Message Compressed bit of the frame is set.
        if (mPMCE != null && frame.getRsv1()) {
            // Decompress the payload.
            payload = decompress(payload);
        }

        return payload;
    }

    private void callOnCloseFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnCloseFrame(frame);
    }

    private void callOnPingFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnPingFrame(frame);
    }

    private void callOnPongFrame(WebSocketFrame frame) {
        mWebSocket.getListenerManager().callOnPongFrame(frame);
    }

    private void notifyFinished() {
        mWebSocket.onReadingThreadFinished(mCloseFrame);
    }

    private void waitForCloseFrame() {
        if (mNotWaitForCloseFrame) {
            return;
        }

        // If a close frame has already been received.
        if (mCloseFrame != null) {
            return;
        }

        WebSocketFrame frame = null;

        // Schedule a task which calls Socket.close() to prevent
        // the code below from looping forever.
        scheduleClose();

        while (true) {
            try {
                // Read a frame from the server.
                frame = mWebSocket.getInput().readFrame();
            } catch (Throwable t) {
                // Give up receiving a close frame.
                break;
            }

            // If it is a close frame.
            if (frame.isCloseFrame()) {
                // Received a close frame. Finished.
                mCloseFrame = frame;
                break;
            }

            if (isInterrupted()) {
                break;
            }
        }
    }

    private void scheduleClose() {
        synchronized (mCloseLock) {
            cancelCloseTask();
            scheduleCloseTask();
        }
    }

    private void cancelCloseTask() {
        if (mCloseTimer != null) {
            mCloseTimer.cancel();
            mCloseTimer = null;
        }

        if (mCloseTask != null) {
            mCloseTask.cancel();
            mCloseTask = null;
        }
    }

    private void scheduleCloseTask() {
        mCloseTask  = new CloseTask();
        mCloseTimer = new Timer("ReadingThreadCloseTimer");
        mCloseTimer.schedule(mCloseTask, mCloseDelay);
    }

    private void cancelClose() {
        synchronized (mCloseLock) {
            cancelCloseTask();
        }
    }

    void requestStop(long closeDelay) {
        synchronized (this) {
            if (mStopRequested) {
                return;
            }

            mStopRequested = true;
        }

        // interrupt() may not interrupt a blocking socket read(), so calling
        // interrupt() here may not work. interrupt() in Java is different
        // from signal-based interruption in C which unblocks a read() system
        // call. Anyway, let's mark this thread as interrupted.
        interrupt();

        // To surely unblock a read() call, Socket.close() needs to be called.
        // Or, shutdownInterrupt() may work, but it is not explicitly stated
        // in the JavaDoc. In either case, interruption should not be executed
        // now because a close frame from the server should be waited for.
        //
        // So, let's schedule a task with some delay which calls Socket.close().
        // However, in normal cases, a close frame will arrive from the server
        // before the task calls Socket.close().
        mCloseDelay = closeDelay;
        scheduleClose();
    }

    private class CloseTask extends TimerTask {
        @Override
        public void run() {
            try {
                Socket socket = mWebSocket.getSocket();
                socket.close();
            } catch (Throwable t) {
                // Ignore.
            }
        }
    }

}
