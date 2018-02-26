package com.imagine.story.common.websocket;

public enum WebSocketError {

    /**
     * The current state of the WebSocket is not CREATED
     */
    NOT_IN_CREATED_STATE,
    /**
     * The certificate of the peer does not match the expected hostname
     */
    HOSTNAME_UNVERIFIED,
    /**
     * Socket connect failed
     */
    SOCKET_CONNECT_ERROR,
    /**
     * Handshake with a proxy server failed
     */
    PROXY_HANDSHAKE_ERROR,
    /**
     * Failed to overlay an existing socket
     */
    SOCKET_OVERLAY_ERROR,
    /**
     * SSL handshake with a WebSocket endpoint failed
     */
    SSL_HANDSHAKE_ERROR,
    /**
     * Failed to get the input stream of the raw socket
     */
    SOCKET_INPUT_STREAM_FAILURE,
    /**
     * Failed to get the output stream of the raw socket
     */
    SOCKET_OUTPUT_STREAM_FAILURE,
    /**
     * Failed to send an opening handshake request to the server
     */
    OPENING_HAHDSHAKE_REQUEST_FAILURE,
    /**
     * Failed to read an opening handshake response from the server
     */
    OPENING_HANDSHAKE_RESPONSE_FAILURE,
    /**
     * The status line of the opening handshake response is empty
     */
    STATUS_LINE_EMPTY,
    /**
     * The status line of the opening handshake response is badly formatted
     */
    STATUS_LINE_BAD_FORMAT,
    /**
     * An error occurred while HTTP header section was being read
     */
    HTTP_HEADER_FAILURE,
    /**
     * The status code of the opening handshake response is not {@code 101 Switching Protocols}
     */
    NOT_SWITCHING_PROTOCOLS,
    /**
     * The end of the stream has been reached unexpectedly
     */
    INSUFFICENT_DATA,
    /**
     * The opening handshake response does not contain {@code Upgrade} header
     */
    NO_UPGRADE_HEADER,
    /**
     * {@code websocket} was not found in {@code Upgrade} header
     */
    NO_WEBSOCKET_IN_UPGRADE_HEADER,
    /**
     * The opening handshake response does not contain {@code Connection} header
     */
    NO_CONNECTION_HEADER,
    /**
     * {@code Upgrade} was not found in {@code Connection} header
     */
    NO_UPGRADE_IN_CONNECTION_HEADER,
    /**
     * The opening handshake response does not contain {@code Sec-WebSocket-Accept} header
     */
    NO_SEC_WEBSOCKET_ACCEPT_HEADER,
    /**
     * The value of {@code Sec-WebSocket-Accept} header is different from the expected one
     */
    UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,
    /**
     * An uncaught throwable was detected in the reading thread (which reads frames from the server)
     */
    UNEXPECTED_ERROR_IN_READING_THREAD,
    /**
     * No more frame can be read because the end of the input stream has been reached
     */
    NO_MORE_FRAME,
    /**
     * The payload length of a frame is invalid
     */
    INVALID_PAYLOAD_LENGTH,
    /**
     * The payload length of a frame exceeds the maximum array size in Java
     */
    TOO_LONG_PAYLOAD,
    /**
     * {@link OutOfMemoryError} occurred during a trial to allocate a memory area for a frame's payload
     */
    INSUFFICIENT_MEMORY_FOR_PAYLOAD,
    /**
     * A reserved bit of a frame has an unexpected value
     */
    UNEXPECTED_RESERVED_BIT,
    /**
     * A frame has an unknown opcode
     */
    UNKNOWN_OPCODE,
    /**
     * A frame from the server is masked
     */
    FRAME_MASKED,
    /**
     * A control frame is fragmented
     */
    FRAGMENTED_CONTROL_FRAME,
    /**
     * A continuation frame was detected although a continuation had not started
     */
    UNEXPECTED_CONTINUATION_FRAME,
    /**
     * A non-control frame was detected although the existing continuation had not been closed
     */
    CONTINUATION_NOT_CLOSED,
    /**
     * The payload size of a control frame exceeds the maximum size (125 bytes)
     */
    TOO_LONG_CONTROL_FRAME_PAYLOAD,
    /**
     * Interruption occurred while a frame was being read from the WebSocket
     */
    INTERRUPTED_IN_READING,
    /**
     * An I/O error occurred while a frame was being read from the WebSocket
     */
    IO_ERROR_IN_READING,
    /**
     * At least one of the reserved bits of a frame is set
     */
    NON_ZERO_RESERVED_BITS,
    /**
     * Failed to concatenate payloads of multiple frames to construct a message
     */
    MESSAGE_CONSTRUCTION_ERROR,
    /**
     * Failed to convert payload data into a string
     */
    TEXT_MESSAGE_CONSTRUCTION_ERROR,
    /**
     * An uncaught throwable was detected in the writing thread (which sends frames to the server)
     */
    UNEXPECTED_ERROR_IN_WRITING_THREAD,
    /**
     * Flushing frames to the server failed
     */
    FLUSH_ERROR,
    /**
     * An I/O error occurred when a frame was tried to be sent
     */
    IO_ERROR_IN_WRITING,
    /**
     * The value in {@code Sec-WebSocket-Extensions} failed to be parsed
     */
    EXTENSION_PARSE_ERROR,
    /**
     * The extension contained in {@code Sec-WebSocket-Extensions} header is not supported
     */
    UNSUPPORTED_EXTENSION,
    /**
     * The combination of the extensions contained in {@code Sec-WebSocket-Extensions} header causes conflicts
     */
    EXTENSIONS_CONFLICT,
    /**
     * The protocol contained in {@code Sec-WebSocket-Protocol} header is not supported
     */
    UNSUPPORTED_PROTOCOL,
    /**
     * {@code permessage-deflate} extension contains an unsupported parameter
     */
    PERMESSAGE_DEFLATE_UNSUPPORTED_PARAMETER,
    /**
     * The value of {@code server_max_window_bits} parameter or {@code
     * client_max_window_bits} parameter of {@code permessage-deflate}
     * extension is invalid
     */
    PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS,
    /**
     * Decompression failed
     */
    DECOMPRESSION_ERROR,
    /**
     * Compression failed
     */
    COMPRESSION_ERROR

}
