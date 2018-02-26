package com.imagine.story.activity;

import android.view.Window;
import android.view.WindowManager;

import com.imagine.story.R;
import com.imagine.story.common.base.BaseActivity;
import com.imagine.story.common.websocket.HostnameUnverifiedException;
import com.imagine.story.common.websocket.OpeningHandshakeException;
import com.imagine.story.common.websocket.StatusLine;
import com.imagine.story.common.websocket.WebSocket;
import com.imagine.story.common.websocket.WebSocketAdapter;
import com.imagine.story.common.websocket.WebSocketException;
import com.imagine.story.common.websocket.WebSocketFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by conquer on 2018/2/9.
 *
 */

public class GameActivity extends BaseActivity {
    public static final int WEB_SOCKET_TIME_OUT = 5000;

    private WebSocketFactory webSocketFactory;
    private WebSocket webSocket;

    @Override
    protected void initViews() {
        // 去标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 去状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        initWebSocket();
    }

    private void initWebSocket() {
        webSocketFactory = new WebSocketFactory();
        webSocketFactory.setConnectionTimeout(WEB_SOCKET_TIME_OUT);
        try {
            webSocket = webSocketFactory.createSocket("wss://game.iluoy.com/ws/" +
                    "matching?token=71fb292068518665dc9b2131a55fee98&gameType=wuziqi&matchType=recreation");
        } catch (IOException e) {
            e.printStackTrace();
        }

        webSocket.addListener(new WebSocketAdapter() {

        });

        try {
            // Connect to the server and perform an opening handshake
            // This method blocks until the opening handshake is finished
            webSocket.connect();
        } catch (OpeningHandshakeException e) {
            // A violation against the WebSocket protocol was detected during the opening handshake

            // Status line.
            StatusLine sl = e.getStatusLine();
            System.out.println("=== Status Line ===");
            System.out.format("HTTP Version  = %s\n", sl.getHttpVersion());
            System.out.format("Status Code   = %d\n", sl.getStatusCode());
            System.out.format("Reason Phrase = %s\n", sl.getReasonPhrase());

            // HTTP headers.
            Map<String, List<String>> headers = e.getHeaders();
            System.out.println("=== HTTP Headers ===");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                // Header name.
                String name = entry.getKey();

                // Values of the header.
                List<String> values = entry.getValue();

                if (values == null || values.size() == 0) {
                    // Print the name only.
                    System.out.println(name);
                    continue;
                }

                for (String value : values) {
                    // Print the name and the value.
                    System.out.format("%s: %s\n", name, value);
                }
            }
        } catch (HostnameUnverifiedException e) {
            // The certificate of the peer does not match the expected hostname
        } catch (WebSocketException e) {
            // Failed to establish a WebSocket connection
            // 建立连接失败，可以新建一个WebSocket重试
            reconnect();
        }
    }

    private void reconnect() {
        try {
            webSocket.recreate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
