package com.imagine.story.common.websocket;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class SocketConnector {
    private Socket mSocket;
    private final Address mAddress;
    private final int mConnectionTimeout;
    private final ProxyHandshaker mProxyHandshaker;
    private final SSLSocketFactory mSSLSocketFactory;
    private final String mHost;
    private final int mPort;
    private boolean mVerifyHostname;

    SocketConnector(Socket socket, Address address, int timeout) {
        this(socket, address, timeout, null, null, null, 0);
    }

    SocketConnector(Socket socket, Address address, int timeout,
                    ProxyHandshaker handshaker, SSLSocketFactory sslSocketFactory,
                    String host, int port) {
        mSocket = socket;
        mAddress = address;
        mConnectionTimeout = timeout;
        // 代理握手
        mProxyHandshaker = handshaker;
        mSSLSocketFactory = sslSocketFactory;
        mHost = host;
        mPort = port;
    }

    public Socket getSocket() {
        return mSocket;
    }


    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    SocketConnector setVerifyHostname(boolean verifyHostname) {
        mVerifyHostname = verifyHostname;
        return this;
    }

    /**
     * 执行实际连接
     * @throws WebSocketException
     */
    public void connect() throws WebSocketException {
        try {
            // Connect to the server (either a proxy or a WebSocket endpoint)
            doConnect();

        } catch (WebSocketException e) {
            // Failed to connect the server
            try {
                mSocket.close();
            } catch (IOException ioe) {
                // Ignore any error raised(引起的) by close()
            }

            throw e;
        }
    }

    private void doConnect() throws WebSocketException {
        // True if a proxy server is set.
        boolean proxied = mProxyHandshaker != null;

        try {
            // Connect to the server (either a proxy or a WebSocket endpoint)
            mSocket.connect(mAddress.toInetSocketAddress(), mConnectionTimeout);
            // 安全的socket
            if (mSocket instanceof SSLSocket) {
                // Verify(验证) that the hostname matches the certificate(证书) here since
                // this is not automatically done by the SSLSocket
                verifyHostname((SSLSocket) mSocket, mAddress.getHostname());
            }
        } catch (IOException e) {
            // Failed to connect the server.
            String message = String.format("Failed to connect to %s'%s': %s", (proxied ? "the proxy " : ""), mAddress, e.getMessage());
            // Raise an exception with SOCKET_CONNECT_ERROR.
            throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR, message, e);
        }

        // If a proxy server is set.
        if (proxied) {
            // Perform handshake with the proxy server.
            // SSL handshake is performed as necessary, too.
            handshake();
        }
    }

    private void verifyHostname(SSLSocket socket, String hostname) throws HostnameUnverifiedException {
        if (!mVerifyHostname) {
            // Skip hostname verification
            return;
        }

        // Hostname verifier
        OkHostnameVerifier verifier = OkHostnameVerifier.INSTANCE;

        // The SSL session
        SSLSession session = socket.getSession();

        // Verify the hostname
        if (verifier.verify(hostname, session)) {
            // Verified. No problem.
            return;
        }

        // The certificate of the peer does not match the expected hostname.
        throw new HostnameUnverifiedException(socket, hostname);
    }

    /**
     * Perform proxy handshake and optionally SSL handshake.
     */
    private void handshake() throws WebSocketException {
        try {
            // Perform handshake with the proxy server.
            mProxyHandshaker.perform();
        } catch (IOException e) {
            // Handshake with the proxy server failed.
            String message = String.format("Handshake with the proxy server (%s) failed: %s", mAddress, e.getMessage());

            // Raise an exception with PROXY_HANDSHAKE_ERROR.
            throw new WebSocketException(WebSocketError.PROXY_HANDSHAKE_ERROR, message, e);
        }

        if (mSSLSocketFactory == null) {
            // SSL handshake with the WebSocket endpoint is not needed.
            return;
        }

        try {
            // Overlay the existing socket.
            mSocket = mSSLSocketFactory.createSocket(mSocket, mHost, mPort, true);
        } catch (IOException e) {
            // Failed to overlay an existing socket.
            String message = "Failed to overlay an existing socket: " + e.getMessage();

            // Raise an exception with SOCKET_OVERLAY_ERROR.
            throw new WebSocketException(WebSocketError.SOCKET_OVERLAY_ERROR, message, e);
        }

        try {
            // Start the SSL handshake manually. As for the reason, see
            // http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/samples/sockets/client/SSLSocketClient.java
            ((SSLSocket)mSocket).startHandshake();

            if (mSocket instanceof SSLSocket) {
                // Verify that the proxied hostname matches the certificate here since
                // this is not automatically done by the SSLSocket.
                verifyHostname((SSLSocket)mSocket, mProxyHandshaker.getProxiedHostname());
            }
        } catch (IOException e) {
            // SSL handshake with the WebSocket endpoint failed.
            String message = String.format("SSL handshake with the WebSocket endpoint (%s) failed: %s", mAddress, e.getMessage());

            // Raise an exception with SSL_HANDSHAKE_ERROR.
            throw new WebSocketException(WebSocketError.SSL_HANDSHAKE_ERROR, message, e);
        }
    }

    /**
     * 沉默的关闭
     */
    void closeSilently() {
        try {
            mSocket.close();
        } catch (Throwable t) {
            // Ignored.
        }
    }
}
