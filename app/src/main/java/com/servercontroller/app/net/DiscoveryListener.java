package com.servercontroller.app.net;

import com.servercontroller.common.protocol.Message;
import com.servercontroller.common.protocol.Protocol;
import com.servercontroller.common.protocol.messages.DiscoveryMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class DiscoveryListener {
    private final int port;

    public DiscoveryListener(int port) {
        this.port = port;
    }

    public void start(Consumer<DiscoveryMessage> consumer) {
        Thread thread = new Thread(() -> listen(consumer));
        thread.setDaemon(true);
        thread.start();
    }

    private void listen(Consumer<DiscoveryMessage> consumer) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[2048];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String payload = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                Message message = Protocol.decode(payload);
                if (message instanceof DiscoveryMessage discovery) {
                    consumer.accept(new DiscoveryMessage(
                            discovery.serverName(),
                            packet.getAddress().getHostAddress(),
                            discovery.port(),
                            discovery.serverType()
                    ));
                }
            }
        } catch (Exception ignored) {
        }
    }
}
