package com.imagine.story.common.websocket;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * version 2.3
 */
public class WebSocketFactory {
    private final SocketFactorySettings mSocketFactorySettings;
    private final ProxySettings mProxySettings;
    private int mConnectionTimeout;
    private boolean mVerifyHostname = true;

    public WebSocketFactory() {
        mSocketFactorySettings = new SocketFactorySettings();
        mProxySettings         = new ProxySettings(this);
    }

    public SocketFactory getSocketFactory() {
        return mSocketFactorySettings.getSocketFactory();
    }

    public WebSocketFactory setSocketFactory(SocketFactory factory) {
        mSocketFactorySettings.setSocketFactory(factory);
        return this;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return mSocketFactorySettings.getSSLSocketFactory();
    }

    public WebSocketFactory setSSLSocketFactory(SSLSocketFactory factory) {
        mSocketFactorySettings.setSSLSocketFactory(factory);
        return this;
    }

    public SSLContext getSSLContext() {
        return mSocketFactorySettings.getSSLContext();
    }

    public WebSocketFactory setSSLContext(SSLContext context) {
        mSocketFactorySettings.setSSLContext(context);
        return this;
    }

    public ProxySettings getProxySettings() {
        return mProxySettings;
    }

    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    public WebSocketFactory setConnectionTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value cannot be negative.");
        }

        mConnectionTimeout = timeout;
        return this;
    }

    public boolean getVerifyHostname() {
        return mVerifyHostname;
    }

    public WebSocketFactory setVerifyHostname(boolean verifyHostname) {
        mVerifyHostname = verifyHostname;
        return this;
    }

    public WebSocket createSocket(String uri) throws IOException {
        return createSocket(uri, getConnectionTimeout());
    }

    public WebSocket createSocket(String uri, int timeout) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The given URI is null.");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("The given timeout value is negative.");
        }

        return createSocket(URI.create(uri), timeout);
    }

    public WebSocket createSocket(URL url) throws IOException {
        return createSocket(url, getConnectionTimeout());
    }

    public WebSocket createSocket(URL url, int timeout) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("The given URL is null.");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("The given timeout value is negative.");
        }

        try {
            return createSocket(url.toURI(), timeout);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to convert the given URL into a URI.");
        }
    }

    public WebSocket createSocket(URI uri) throws IOException {
        return createSocket(uri, getConnectionTimeout());
    }

    public WebSocket createSocket(URI uri, int timeout) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The given URI is null.");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("The given timeout value is negative.");
        }

        // Split the URI.
        String scheme   = uri.getScheme();
        String userInfo = uri.getUserInfo();
        // 提取host
        String host     = Misc.extractHost(uri);
        int port        = uri.getPort();
        String path     = uri.getRawPath();
        String query    = uri.getRawQuery();

        return createSocket(scheme, userInfo, host, port, path, query, timeout);
    }

    private WebSocket createSocket(
            String scheme, String userInfo, String host, int port,
            String path, String query, int timeout) throws IOException {
        // True if 'scheme' is 'wss' or 'https'.
        boolean secure = isSecureConnectionRequired(scheme);

        // Check if 'host' is specified.
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("The host part is empty.");
        }

        // Determine the path.
        path = determinePath(path);

        // Create a Socket instance and a connector to connect to the server.
        // 关键--创建一个socket实例和一个连接器--来连接到服务器
        SocketConnector connector = createRawSocket(host, port, secure, timeout);

        // Create a WebSocket instance.
        return createWebSocket(secure, userInfo, host, port, path, query, connector);
    }

    private static boolean isSecureConnectionRequired(String scheme) {
        if (scheme == null || scheme.length() == 0) {
            throw new IllegalArgumentException("The scheme part is empty.");
        }

        if ("wss".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return true;
        }

        if ("ws".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
            return false;
        }

        throw new IllegalArgumentException("Bad scheme: " + scheme);
    }

    private static String determinePath(String path) {
        if (path == null || path.length() == 0) {
            return "/";
        }

        if (path.startsWith("/")) {
            return path;
        } else {
            return "/" + path;
        }
    }

    private SocketConnector createRawSocket(
            String host, int port, boolean secure, int timeout) throws IOException {
        // Determine the port number. Especially, if 'port' is -1,
        // it is converted to 80 or 443.
        port = determinePort(port, secure);

        // True if a proxy server should be used.
        boolean proxied = (mProxySettings.getHost() != null);

        // See "Figure 2 -- Proxy server traversal decision tree" at
        // http://www.infoq.com/articles/Web-Sockets-Proxy-Servers

        if (proxied) {
            // Create a connector to connect to the proxy server.
            return createProxiedRawSocket(host, port, secure, timeout);
        } else {
            // Create a connector to connect to the WebSocket endpoint directly.
            return createDirectRawSocket(host, port, secure, timeout);
        }
    }

    /**
     * 创建一个代理的原始Socket
     */
    private SocketConnector createProxiedRawSocket(String host, int port, boolean secure, int timeout) throws IOException {
        // Determine the port number of the proxy server.
        // Especially, if getPort() returns -1, the value
        // is converted to 80 or 443.
        int proxyPort = determinePort(mProxySettings.getPort(), mProxySettings.isSecure());

        // Select a socket factory.
        SocketFactory socketFactory = mProxySettings.selectSocketFactory();

        // Let the socket factory create a socket.
        Socket socket = socketFactory.createSocket();

        // The address to connect to.
        Address address = new Address(mProxySettings.getHost(), proxyPort);

        // The delegatee(代理人) for the handshake(握手) with the proxy(代理).
        ProxyHandshaker handshaker = new ProxyHandshaker(socket, host, port, mProxySettings);

        // SSLSocketFactory for SSL handshake with the WebSocket endpoint.
        SSLSocketFactory sslSocketFactory = secure ?
                (SSLSocketFactory) mSocketFactorySettings.selectSocketFactory(true) : null;

        // Create an instance that will execute the task to connect to the server later.
        return new SocketConnector(
                socket, address, timeout, handshaker, sslSocketFactory, host, port)
                .setVerifyHostname(mVerifyHostname);
    }

    private SocketConnector createDirectRawSocket(String host, int port, boolean secure, int timeout) throws IOException {
        // Select a socket factory.
        SocketFactory factory = mSocketFactorySettings.selectSocketFactory(secure);

        // Let the socket factory create a socket.
        Socket socket = factory.createSocket();

        // The address to connect to.
        Address address = new Address(host, port);

        // Create an instance that will execute the task to connect to the server later.
        return new SocketConnector(socket, address, timeout)
                .setVerifyHostname(mVerifyHostname);
    }

    private static int determinePort(int port, boolean secure) {
        if (0 <= port) {
            return port;
        }

        if (secure) {
            return 443;
        } else {
            return 80;
        }
    }


    // 最终步
    private WebSocket createWebSocket(boolean secure, String userInfo, String host, int port,
            String path, String query, SocketConnector connector) {
        // The value for "Host" HTTP header.
        if (0 <= port) {
            host = host + ":" + port;
        }

        // The value for Request-URI of Request-Line.
        if (query != null) {
            path = path + "?" + query;
        }

        return new WebSocket(this, secure, userInfo, host, path, connector);
    }
}
