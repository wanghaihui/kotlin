package com.imagine.story.common.websocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WebSocket frame -- 数据帧
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5"
 *      >RFC 6455, 5. Data Framing</a>
 */

public class WebSocketFrame {
    private boolean mFin;
    private boolean mRsv1;
    private boolean mRsv2;
    private boolean mRsv3;
    private int mOpcode;
    private boolean mMask;
    private byte[] mPayload;

    public boolean getFin() {
        return mFin;
    }
    public WebSocketFrame setFin(boolean fin) {
        mFin = fin;
        return this;
    }

    public boolean getRsv1() {
        return mRsv1;
    }
    public WebSocketFrame setRsv1(boolean rsv1) {
        mRsv1 = rsv1;
        return this;
    }

    public boolean getRsv2() {
        return mRsv2;
    }
    public WebSocketFrame setRsv2(boolean rsv2) {
        mRsv2 = rsv2;
        return this;
    }

    public boolean getRsv3() {
        return mRsv3;
    }
    public WebSocketFrame setRsv3(boolean rsv3) {
        mRsv3 = rsv3;
        return this;
    }

    public int getOpcode() {
        return mOpcode;
    }
    public WebSocketFrame setOpcode(int opcode) {
        mOpcode = opcode;
        return this;
    }

    public boolean isContinuationFrame() {
        return (mOpcode == WebSocketOpcode.CONTINUATION);
    }

    public boolean isTextFrame() {
        return (mOpcode == WebSocketOpcode.TEXT);
    }

    public boolean isBinaryFrame() {
        return (mOpcode == WebSocketOpcode.BINARY);
    }

    public boolean isCloseFrame() {
        return (mOpcode == WebSocketOpcode.CLOSE);
    }

    public boolean isPingFrame() {
        return (mOpcode == WebSocketOpcode.PING);
    }

    public boolean isPongFrame() {
        return (mOpcode == WebSocketOpcode.PONG);
    }

    public boolean isDataFrame() {
        return (0x1 <= mOpcode && mOpcode <= 0x7);
    }

    public boolean isControlFrame() {
        return (0x8 <= mOpcode && mOpcode <= 0xF);
    }

    boolean getMask() {
        return mMask;
    }
    WebSocketFrame setMask(boolean mask) {
        mMask = mask;
        return this;
    }

    public boolean hasPayload() {
        return mPayload != null;
    }

    public int getPayloadLength() {
        if (mPayload == null) {
            return 0;
        }
        return mPayload.length;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public String getPayloadText() {
        if (mPayload == null) {
            return null;
        }
        return Misc.toStringUTF8(mPayload);
    }

    public WebSocketFrame setPayload(byte[] payload) {
        if (payload != null && payload.length == 0) {
            payload = null;
        }
        mPayload = payload;
        return this;
    }

    public WebSocketFrame setPayload(String payload) {
        if (payload == null || payload.length() == 0) {
            return setPayload((byte[]) null);
        }
        return setPayload(Misc.getBytesUTF8(payload));
    }

    public WebSocketFrame setCloseFramePayload(int closeCode, String reason) {
        // Convert the close code to a 2-byte unsigned integer
        // in network byte order.
        byte[] encodedCloseCode = new byte[] {
                (byte)((closeCode >> 8) & 0xFF),
                (byte)((closeCode     ) & 0xFF)
        };

        // If a reason string is not given.
        if (reason == null || reason.length() == 0) {
            // Use the close code only.
            return setPayload(encodedCloseCode);
        }

        // Convert the reason into a byte array.
        byte[] encodedReason = Misc.getBytesUTF8(reason);

        // Concatenate the close code and the reason.
        byte[] payload = new byte[2 + encodedReason.length];
        System.arraycopy(encodedCloseCode, 0, payload, 0, 2);
        System.arraycopy(encodedReason, 0, payload, 2, encodedReason.length);

        // Use the concatenated string.
        return setPayload(payload);
    }

    public int getCloseCode() {
        if (mPayload == null || mPayload.length < 2) {
            return WebSocketCloseCode.NONE;
        }

        // A close code is encoded in network byte order.
        int closeCode = (((mPayload[0] & 0xFF) << 8) | (mPayload[1] & 0xFF));

        return closeCode;
    }

    public String getCloseReason() {
        if (mPayload == null || mPayload.length < 3) {
            return null;
        }

        return Misc.toStringUTF8(mPayload, 2, mPayload.length - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("WebSocketFrame(FIN=").append(mFin ? "1" : "0")
                .append(",RSV1=").append(mRsv1 ? "1" : "0")
                .append(",RSV2=").append(mRsv2 ? "1" : "0")
                .append(",RSV3=").append(mRsv3 ? "1" : "0")
                .append(",Opcode=").append(Misc.toOpcodeName(mOpcode))
                .append(",Length=").append(getPayloadLength());

        switch (mOpcode) {
            case WebSocketOpcode.TEXT:
                appendPayloadText(builder);
                break;

            case WebSocketOpcode.BINARY:
                appendPayloadBinary(builder);
                break;

            case WebSocketOpcode.CLOSE:
                appendPayloadClose(builder);
                break;
        }

        return builder.append(")").toString();
    }

    private boolean appendPayloadCommon(StringBuilder builder) {
        builder.append(",Payload=");

        if (mPayload == null) {
            builder.append("null");

            // Nothing more to append.
            return true;
        }

        if (mRsv1) {
            // In the current implementation, mRsv1=true is allowed
            // only when Per-Message Compression is applied.
            builder.append("compressed");

            // Nothing more to append.
            return true;
        }

        // Continue.
        return false;
    }


    private void appendPayloadText(StringBuilder builder) {
        if (appendPayloadCommon(builder)) {
            // Nothing more to append.
            return;
        }

        builder.append("\"");
        builder.append(getPayloadText());
        builder.append("\"");
    }

    private void appendPayloadClose(StringBuilder builder) {
        builder.append(",CloseCode=").append(getCloseCode())
                .append(",Reason=");

        String reason = getCloseReason();

        if (reason == null) {
            builder.append("null");
        } else {
            builder.append("\"").append(reason).append("\"");
        }
    }

    private void appendPayloadBinary(StringBuilder builder) {
        if (appendPayloadCommon(builder))
        {
            // Nothing more to append.
            return;
        }

        for (int i = 0; i < mPayload.length; ++i) {
            builder.append(String.format("%02X ", (0xFF & mPayload[i])));
        }

        if (mPayload.length != 0) {
            // Remove the last space.
            builder.setLength(builder.length() - 1);
        }
    }

    public static WebSocketFrame createContinuationFrame() {
        return new WebSocketFrame()
                .setOpcode(WebSocketOpcode.CONTINUATION);
    }

    public static WebSocketFrame createContinuationFrame(byte[] payload) {
        return createContinuationFrame().setPayload(payload);
    }

    public static WebSocketFrame createContinuationFrame(String payload) {
        return createContinuationFrame().setPayload(payload);
    }

    public static WebSocketFrame createTextFrame(String payload) {
        return new WebSocketFrame()
                .setFin(true)
                .setOpcode(WebSocketOpcode.TEXT)
                .setPayload(payload);
    }

    public static WebSocketFrame createBinaryFrame(byte[] payload) {
        return new WebSocketFrame()
                .setFin(true)
                .setOpcode(WebSocketOpcode.BINARY)
                .setPayload(payload);
    }

    public static WebSocketFrame createCloseFrame() {
        return new WebSocketFrame()
                .setFin(true)
                .setOpcode(WebSocketOpcode.CLOSE);
    }

    public static WebSocketFrame createCloseFrame(int closeCode) {
        return createCloseFrame().setCloseFramePayload(closeCode, null);
    }

    public static WebSocketFrame createCloseFrame(int closeCode, String reason) {
        return createCloseFrame().setCloseFramePayload(closeCode, reason);
    }

    public static WebSocketFrame createPingFrame() {
        return new WebSocketFrame()
                .setFin(true)
                .setOpcode(WebSocketOpcode.PING);
    }

    public static WebSocketFrame createPingFrame(byte[] payload) {
        return createPingFrame().setPayload(payload);
    }

    public static WebSocketFrame createPingFrame(String payload) {
        return createPingFrame().setPayload(payload);
    }

    public static WebSocketFrame createPongFrame() {
        return new WebSocketFrame()
                .setFin(true)
                .setOpcode(WebSocketOpcode.PONG);
    }

    public static WebSocketFrame createPongFrame(byte[] payload) {
        return createPongFrame().setPayload(payload);
    }

    public static WebSocketFrame createPongFrame(String payload) {
        return createPongFrame().setPayload(payload);
    }

    static byte[] mask(byte[] maskingKey, byte[] payload) {
        if (maskingKey == null || maskingKey.length < 4 || payload == null) {
            return payload;
        }

        for (int i = 0; i < payload.length; ++i) {
            payload[i] ^= maskingKey[i % 4];
        }

        return payload;
    }

    static WebSocketFrame compressFrame(WebSocketFrame frame, PerMessageCompressionExtension pmce) {
        // If Per-Message Compression is not enabled.
        if (pmce == null) {
            // No compression.
            return frame;
        }

        // If the frame is neither a TEXT frame nor a BINARY frame.
        if (!frame.isTextFrame() && !frame.isBinaryFrame()) {
            // No compression.
            return frame;
        }

        // If the frame is not the final frame.
        if (!frame.getFin()) {
            // The compression must be applied to this frame and
            // all the subsequent continuation frames, but the
            // current implementation does not support the behavior.
            return frame;
        }

        // If the RSV1 bit is set.
        if (frame.getRsv1()) {
            // In the current implementation, RSV1=true is allowed
            // only as Per-Message Compressed Bit (See RFC 7692,
            // 6. Framing). Therefore, RSV1=true here is regarded
            // as "already compressed".
            return frame;
        }

        // The plain payload before compression.
        byte[] payload = frame.getPayload();

        // If the payload is empty.
        if (payload == null || payload.length == 0) {
            // No compression.
            return frame;
        }

        // Compress the payload.
        byte[] compressed = compress(payload, pmce);

        // If the length of the compressed data is not less than
        // that of the original plain payload.
        if (payload.length <= compressed.length) {
            // It's better not to compress the payload.
            return frame;
        }

        // Replace the plain payload with the compressed data.
        frame.setPayload(compressed);

        // Set Per-Message Compressed Bit (See RFC 7692, 6. Framing).
        frame.setRsv1(true);

        return frame;
    }


    private static byte[] compress(byte[] data, PerMessageCompressionExtension pmce) {
        try {
            // Compress the data.
            return pmce.compress(data);
        } catch (WebSocketException e) {
            // Failed to compress the data. Ignore this error and use
            // the plain original data. The current implementation
            // does not call any listener callback method for this error.
            return data;
        }
    }


    static List<WebSocketFrame> splitIfNecessary(WebSocketFrame frame, int maxPayloadSize, PerMessageCompressionExtension pmce) {
        // If the maximum payload size is not specified.
        if (maxPayloadSize == 0) {
            // Not split.
            return null;
        }

        // If the total length of the payload is equal to or
        // less than the maximum payload size.
        if (frame.getPayloadLength() <= maxPayloadSize) {
            // Not split.
            return null;
        }

        // If the frame is a binary frame or a text frame.
        if (frame.isBinaryFrame() || frame.isTextFrame()) {
            // Try to compress the frame. In the current implementation, binary
            // frames and text frames with the FIN bit true can be compressed.
            // The compressFrame() method may change the payload and the RSV1
            // bit of the given frame.
            frame = compressFrame(frame, pmce);

            // If the payload length of the frame has become equal to or less
            // than the maximum payload size as a result of the compression.
            if (frame.getPayloadLength() <= maxPayloadSize)
            {
                // Not split. (Note that the frame has been compressed)
                return null;
            }
        } else if (!frame.isContinuationFrame()) {
            // Control frames (Close/Ping/Pong) are not split.
            return null;
        }

        // Split the frame.
        return split(frame, maxPayloadSize);
    }


    private static List<WebSocketFrame> split(WebSocketFrame frame, int maxPayloadSize) {
        // The original payload and the original FIN bit.
        byte[] originalPayload = frame.getPayload();
        boolean originalFin    = frame.getFin();

        List<WebSocketFrame> frames = new ArrayList<>();

        // Generate the first frame using the existing WebSocketFrame instance.
        // Note that the reserved bit 1 and the opcode are untouched.
        byte[] payload = Arrays.copyOf(originalPayload, maxPayloadSize);
        frame.setFin(false).setPayload(payload);
        frames.add(frame);

        for (int from = maxPayloadSize; from < originalPayload.length; from += maxPayloadSize) {
            // Prepare the payload of the next continuation frame.
            int to  = Math.min(from + maxPayloadSize, originalPayload.length);
            payload = Arrays.copyOfRange(originalPayload, from, to);

            // Create a continuation frame.
            WebSocketFrame cont = WebSocketFrame.createContinuationFrame(payload);
            frames.add(cont);
        }

        if (originalFin) {
            // Set the FIN bit of the last frame.
            frames.get(frames.size() - 1).setFin(true);
        }

        return frames;
    }
}
