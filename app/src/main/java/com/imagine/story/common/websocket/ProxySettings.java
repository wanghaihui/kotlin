package com.imagine.story.common.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class ProxySettings {

    private final WebSocketFactory mWebSocketFactory;
    // Additional HTTP headers passed to the proxy server
    private final Map<String, List<String>> mHeaders;
    private final SocketFactorySettings mSocketFactorySettings;
    // Use TLS to connect to the proxy server or not
    private boolean mSecure;
    // The host name of the proxy server
    private String mHost;
    // The port number of the proxy server
    private int mPort;
    // The ID for authentication at the proxy server
    private String mId;
    // The password for authentication at the proxy server
    private String mPassword;

    ProxySettings(WebSocketFactory factory) {
        mWebSocketFactory = factory;
        // TreeMap--有序的<key,value>集合
        // 按字母表顺序排序--忽略大小写排序
        mHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        mSocketFactorySettings = new SocketFactorySettings();

        reset();
    }

    public WebSocketFactory getWebSocketFactory() {
        return mWebSocketFactory;
    }

    public ProxySettings reset() {
        mSecure   = false;
        mHost     = null;
        mPort     = -1;
        mId       = null;
        mPassword = null;
        mHeaders.clear();

        return this;
    }

    public boolean isSecure() {
        return mSecure;
    }
    public ProxySettings setSecure(boolean secure) {
        mSecure = secure;
        return this;
    }

    public String getHost() {
        return mHost;
    }
    public ProxySettings setHost(String host) {
        mHost = host;
        return this;
    }

    public int getPort() {
        return mPort;
    }
    public ProxySettings setPort(int port) {
        mPort = port;
        return this;
    }

    public String getId() {
        return mId;
    }
    public ProxySettings setId(String id) {
        mId = id;
        return this;
    }

    public String getPassword() {
        return mPassword;
    }
    public ProxySettings setPassword(String password) {
        mPassword = password;
        return this;
    }

    public ProxySettings setCredentials(String id, String password) {
        return setId(id).setPassword(password);
    }

    public ProxySettings setServer(String uri) {
        if (uri == null) {
            return this;
        }

        return setServer(URI.create(uri));
    }

    public ProxySettings setServer(URL url) {
        if (url == null) {
            return this;
        }

        try {
            return setServer(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public ProxySettings setServer(URI uri) {
        if (uri == null) {
            return this;
        }

        String scheme   = uri.getScheme();
        String userInfo = uri.getUserInfo();
        String host     = uri.getHost();
        int port        = uri.getPort();

        return setServer(scheme, userInfo, host, port);
    }

    private ProxySettings setServer(String scheme, String userInfo, String host, int port) {
        setByScheme(scheme);
        setByUserInfo(userInfo);
        mHost = host;
        mPort = port;

        return this;
    }

    private void setByScheme(String scheme) {
        if ("http".equalsIgnoreCase(scheme)) {
            mSecure = false;
        } else if ("https".equalsIgnoreCase(scheme)) {
            mSecure = true;
        }
    }

    private void setByUserInfo(String userInfo) {
        if (userInfo == null) {
            return;
        }

        String[] pair = userInfo.split(":", 2);
        String id;
        String pw;

        switch (pair.length) {
            case 2:
                id = pair[0];
                pw = pair[1];
                break;
            case 1:
                id = pair[0];
                pw = null;
                break;
            default:
                return;
        }

        if (id.length() == 0) {
            return;
        }

        mId       = id;
        mPassword = pw;
    }

    public Map<String, List<String>> getHeaders() {
        return mHeaders;
    }
    public ProxySettings addHeader(String name, String value) {
        if (name == null || name.length() == 0) {
            return this;
        }

        List<String> list = mHeaders.get(name);

        if (list == null) {
            list = new ArrayList<>();
            mHeaders.put(name, list);
        }

        list.add(value);

        return this;
    }

    public SocketFactory getSocketFactory() {
        return mSocketFactorySettings.getSocketFactory();
    }
    public ProxySettings setSocketFactory(SocketFactory factory) {
        mSocketFactorySettings.setSocketFactory(factory);
        return this;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return mSocketFactorySettings.getSSLSocketFactory();
    }
    public ProxySettings setSSLSocketFactory(SSLSocketFactory factory) {
        mSocketFactorySettings.setSSLSocketFactory(factory);
        return this;
    }

    public SSLContext getSSLContext() {
        return mSocketFactorySettings.getSSLContext();
    }
    public ProxySettings setSSLContext(SSLContext context) {
        mSocketFactorySettings.setSSLContext(context);
        return this;
    }

    SocketFactory selectSocketFactory() {
        return mSocketFactorySettings.selectSocketFactory(mSecure);
    }
}
