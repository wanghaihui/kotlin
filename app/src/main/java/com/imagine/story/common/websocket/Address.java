package com.imagine.story.common.websocket;

import java.net.InetSocketAddress;
import java.util.Locale;

class Address {
    private final String mHost;
    private final int mPort;
    // transient关键字的作用是需要实现Serializable接口, 将不需要序列化的属性前添加关键字transient
    private transient String mString;

    Address(String host, int port) {
        mHost = host;
        mPort = port;
    }

    InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(mHost, mPort);
    }

    String getHostname() {
        return mHost;
    }

    @Override
    public String toString() {
        if (mString == null) {
            mString = String.format(Locale.getDefault(), "%s:%d", mHost, mPort);
        }

        return mString;
    }
}
