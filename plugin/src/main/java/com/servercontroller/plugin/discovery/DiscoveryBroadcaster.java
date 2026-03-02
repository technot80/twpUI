package com.servercontroller.plugin.discovery;

import com.servercontroller.common.protocol.Protocol;
import com.servercontroller.common.protocol.messages.DiscoveryMessage;
import com.servercontroller.plugin.config.PluginConfig;
import org.bukkit.Bukkit;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscoveryBroadcaster {
    private final PluginConfig.Discovery config;
    private final PluginConfig.Connection connection;
    private final ScheduledExecutorService scheduler;

    public DiscoveryBroadcaster(PluginConfig config) {
        this.config = config.discovery();
        this.connection = config.connection();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (!config.enabled()) {
            return;
        }
        scheduler.scheduleAtFixedRate(this::broadcast, 1, config.intervalSeconds(), TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void broadcast() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            String payload = Protocol.encode(new DiscoveryMessage(
                    Bukkit.getServer().getName(),
                    connection.host(),
                    connection.port(),
                    Bukkit.getServer().getVersion()
            ));
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    InetAddress.getByName("255.255.255.255"), config.port());
            socket.send(packet);
        } catch (Exception ignored) {
        }
    }
}
