package com.imagine.story.common.websocket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

class SocketFactorySettings {
    // 生成Socket的工厂类
    private SocketFactory mSocketFactory;
    // 安全保护
    private SSLSocketFactory mSSLSocketFactory;
    // SSL的上下文
    private SSLContext mSSLContext;

    public SocketFactory getSocketFactory() {
        return mSocketFactory;
    }
    public void setSocketFactory(SocketFactory factory) {
        mSocketFactory = factory;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLSocketFactory;
    }
    public void setSSLSocketFactory(SSLSocketFactory factory) {
        mSSLSocketFactory = factory;
    }

    public SSLContext getSSLContext() {
        return mSSLContext;
    }
    public void setSSLContext(SSLContext context) {
        mSSLContext = context;
    }

    // 选择SocketFactory
    public SocketFactory selectSocketFactory(boolean secure) {
        if (secure) {
            if (mSSLContext != null) {
                return mSSLContext.getSocketFactory();
            }

            if (mSSLSocketFactory != null) {
                return mSSLSocketFactory;
            }

            return SSLSocketFactory.getDefault();
        }

        if (mSocketFactory != null) {
            return mSocketFactory;
        }

        return SocketFactory.getDefault();
    }
}
