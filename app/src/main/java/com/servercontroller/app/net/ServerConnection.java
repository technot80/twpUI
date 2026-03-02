package com.servercontroller.app.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.servercontroller.common.protocol.Message;
import com.servercontroller.common.protocol.Protocol;
import com.servercontroller.common.protocol.messages.AuthMessage;
import com.servercontroller.common.protocol.messages.BroadcastMessage;
import com.servercontroller.common.protocol.messages.CheckUpdatesMessage;
import com.servercontroller.common.protocol.messages.ControlMessage;
import com.servercontroller.common.protocol.messages.DownloadUpdateMessage;
import com.servercontroller.common.protocol.messages.GetPluginsMessage;
import com.servercontroller.common.protocol.messages.SubscribeMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerConnection {
    private final OkHttpClient client;
    private final String url;
    private final String apiKey;
    private WebSocket socket;
    private final List<ServerMessageListener> listeners;

    public ServerConnection(String url, String apiKey) {
        this.client = buildInsecureClient();
        this.url = url;
        this.apiKey = apiKey;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void addListener(ServerMessageListener listener) {
        listeners.add(listener);
    }

    public void connect() {
        Request request = new Request.Builder().url(url).build();
        socket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                send(new AuthMessage(apiKey, "0.1.0"));
                send(new SubscribeMessage(List.of("LOG", "METRICS"), List.of("INFO", "WARN", "ERROR")));
                send(new GetPluginsMessage());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    Message message = Protocol.decode(text);
                    for (ServerMessageListener listener : listeners) {
                        listener.onMessage(message);
                    }
                } catch (Exception e) {
                    for (ServerMessageListener listener : listeners) {
                        listener.onError(e);
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                for (ServerMessageListener listener : listeners) {
                    listener.onError(t);
                }
            }
        });
    }

    public void send(Message message) {
        if (socket == null) {
            return;
        }
        try {
            socket.send(Protocol.encode(message));
        } catch (JsonProcessingException ignored) {
        }
    }

    public void broadcast(String message) {
        send(new BroadcastMessage(message));
    }

    public void restart() {
        send(new ControlMessage("restart"));
    }

    public void stop() {
        send(new ControlMessage("stop"));
    }

    public void requestPlugins() {
        send(new GetPluginsMessage());
    }

    public void checkUpdates(List<String> plugins) {
        send(new CheckUpdatesMessage(plugins));
    }

    public void downloadUpdate(String pluginName, String downloadUrl) {
        send(new DownloadUpdateMessage(pluginName, downloadUrl));
    }

    public void setLogLevels(List<String> logLevels) {
        send(new SubscribeMessage(List.of("LOG", "METRICS"), logLevels));
    }

    public void close() {
        if (socket != null) {
            socket.close(1000, "closed");
        }
    }

    private OkHttpClient buildInsecureClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(hostnameVerifier)
                    .build();
        } catch (Exception e) {
            return new OkHttpClient();
        }
    }
}
