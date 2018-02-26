package com.imagine.story.common.websocket;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 代理握手管理器
 */
class ProxyHandshaker {
    // \r -- 回车
    // \n -- 换行
    private static final String RN = "\r\n";
    private final Socket mSocket;
    private final String mHost;
    private final int mPort;
    private final ProxySettings mSettings;

    public ProxyHandshaker(Socket socket, String host, int port, ProxySettings settings) {
        mSocket   = socket;
        mHost     = host;
        mPort     = port;
        mSettings = settings;
    }

    public void perform() throws IOException {
        // Send a CONNECT request to the proxy server.
        sendRequest();
        // Receive a response.
        receiveResponse();
    }

    private void sendRequest() throws IOException {
        // Build a CONNECT request.
        String request = buildRequest();

        // Convert the request to a byte array.
        byte[] requestBytes = Misc.getBytesUTF8(request);

        // Get the stream to send data to the proxy server.
        OutputStream output = mSocket.getOutputStream();

        // Send the request to the proxy server.
        output.write(requestBytes);
        output.flush();
    }

    private String buildRequest() {
        String host = String.format(Locale.US,"%s:%d", mHost, mPort);

        // CONNECT
        StringBuilder builder = new StringBuilder()
                .append("CONNECT ")
                .append(host)
                .append(" HTTP/1.1")
                .append(RN)
                .append("Host: ")
                .append(host)
                .append(RN);

        // Additional headers
        addHeaders(builder);

        // Proxy-Authorization
        addProxyAuthorization(builder);

        // The entire request.
        return builder.append(RN).toString();
    }

    private void addHeaders(StringBuilder builder) {
        // For each additional header.
        for (Map.Entry<String, List<String>> header : mSettings.getHeaders().entrySet()) {
            // Header name.
            String name = header.getKey();

            // For each header value.
            for (String value : header.getValue()) {
                if (value == null) {
                    value = "";
                }

                builder.append(name).append(": ").append(value).append(RN);
            }
        }
    }

    private void addProxyAuthorization(StringBuilder builder) {
        String id = mSettings.getId();

        if (id == null || id.length() == 0) {
            return;
        }

        String password = mSettings.getPassword();

        if (password == null) {
            password = "";
        }

        // {id}:{password}
        String credentials = String.format("%s:%s", id, password);

        // The current implementation always uses Basic Authentication.
        builder.append("Proxy-Authorization: Basic ")
                .append(Base64.encode(credentials))
                .append(RN);
    }

    private void receiveResponse() throws IOException {
        // Get the stream to read data from the proxy server.
        InputStream input = mSocket.getInputStream();

        // Read the status line.
        readStatusLine(input);

        // Skip HTTP headers, including an empty line (= the separator
        // between the header part and the body part).
        skipHeaders(input);
    }

    private void readStatusLine(InputStream input) throws IOException {
        // Read the status line.
        String statusLine = Misc.readLine(input, "UTF-8");

        // If the response from the proxy server does not contain a status line.
        if (statusLine == null || statusLine.length() == 0) {
            throw new IOException("The response from the proxy server does not contain a status line.");
        }

        // Expect "HTTP/1.1 200 Connection established"
        String[] elements = statusLine.split(" +", 3);

        if (elements.length < 2) {
            throw new IOException(
                    "The status line in the response from the proxy server is badly formatted. " +
                            "The status line is: " + statusLine);
        }

        // If the status code is not "200".
        if (!"200".equals(elements[1])) {
            throw new IOException(
                    "The status code in the response from the proxy server is not '200 Connection established'. " +
                            "The status line is: " + statusLine);
        }

        // OK. A connection was established.
    }

    private void skipHeaders(InputStream input) throws IOException {
        // The number of normal letters in a line.
        int count = 0;

        while (true) {
            // Read a byte from the stream.
            int ch = input.read();

            // If the end of the stream was reached.
            if (ch == -1) {
                // Unexpected EOF.
                throw new EOFException("The end of the stream from the proxy server was reached unexpectedly.");
            }

            // If the end of the line was reached.
            if (ch == '\n') {
                // If there is no normal byte in the line.
                if (count == 0) {
                    // An empty line (the separator) was found.
                    return;
                }

                // Reset the counter and go to the next line.
                count = 0;
                continue;
            }

            // If the read byte is not a carriage return.
            if (ch != '\r') {
                // Increment the number of normal bytes on the line.
                ++count;
                continue;
            }

            // Read the next byte.
            ch = input.read();

            // If the end of the stream was reached.
            if (ch == -1) {
                // Unexpected EOF.
                throw new EOFException("The end of the stream from the proxy server was reached unexpectedly after a carriage return.");
            }

            if (ch != '\n') {
                // Regard the last '\r' as a normal byte as well as the current 'ch'.
                count += 2;
                continue;
            }

            // '\r\n' was detected.

            // If there is no normal byte in the line.
            if (count == 0) {
                // An empty line (the separator) was found.
                return;
            }

            // Reset the counter and go to the next line.
            count = 0;
        }
    }

    String getProxiedHostname() {
        return mHost;
    }

}
