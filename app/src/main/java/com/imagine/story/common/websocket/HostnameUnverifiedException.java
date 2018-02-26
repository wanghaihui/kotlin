package com.imagine.story.common.websocket;

import javax.net.ssl.SSLSocket;

/**
 * The certificate(证书) of the peer does not match the expected hostname
 *
 * {@link #getError()} of this class returns {@link WebSocketError#HOSTNAME_UNVERIFIED}
 *
 * See <a href='https://github.com/TakahikoKawasaki/nv-websocket-client/pull/107'
 * >Verify that certificate is valid for server hostname (#107)</a>
 *
 */
public class HostnameUnverifiedException extends WebSocketException {
    private static final long serialVersionUID = 1L;

    private final SSLSocket mSSLSocket;
    private final String mHostname;

    public HostnameUnverifiedException(SSLSocket socket, String hostname) {
        super(WebSocketError.HOSTNAME_UNVERIFIED,
                String.format("The certificate of the peer %s does not match the expected hostname (%s)", stringifyPrincipal(socket), hostname));
        mSSLSocket = socket;
        mHostname  = hostname;
    }

    private static String stringifyPrincipal(SSLSocket socket) {
        try {
            return String.format("(%s)", socket.getSession().getPeerPrincipal().toString());
        } catch (Exception e) {
            // Principal information is not available.
            return "";
        }
    }

    public SSLSocket getSSLSocket() {
        return mSSLSocket;
    }

    public String getHostname() {
        return mHostname;
    }
}
