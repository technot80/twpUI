package com.servercontroller.plugin;

import com.servercontroller.plugin.config.PluginConfig;
import com.servercontroller.plugin.control.ControlFileService;
import com.servercontroller.plugin.discovery.DiscoveryBroadcaster;
import com.servercontroller.plugin.logs.LogBuffer;
import com.servercontroller.plugin.logs.LogCaptureHandler;
import com.servercontroller.plugin.logs.LogLevel;
import com.servercontroller.plugin.metrics.MetricsService;
import com.servercontroller.plugin.net.WebSocketServerService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class ServerControllerPlugin extends JavaPlugin {
    private PluginConfig config;
    private WebSocketServerService webSocketServerService;
    private MetricsService metricsService;
    private LogBuffer logBuffer;
    private ControlFileService controlFileService;
    private LogCaptureHandler logCaptureHandler;
    private DiscoveryBroadcaster discoveryBroadcaster;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        this.config = PluginConfig.from(getConfig());
        this.logBuffer = new LogBuffer(config.logging().bufferSize(), LogLevel.fromString(config.logging().level()));
        preloadLatestLog(logBuffer, Bukkit.getWorldContainer().toPath().resolve("logs").resolve("latest.log"));
        Path serverRoot = Bukkit.getWorldContainer().toPath();
        this.controlFileService = new ControlFileService(serverRoot, config.control());
        this.metricsService = new MetricsService(this);
        this.webSocketServerService = new WebSocketServerService(this, config, logBuffer, metricsService, controlFileService);
        this.logCaptureHandler = new LogCaptureHandler(logBuffer);
        this.discoveryBroadcaster = new DiscoveryBroadcaster(config);

        Bukkit.getPluginManager().registerEvents(logBuffer, this);
        Logger.getLogger("").addHandler(logCaptureHandler);
        metricsService.start(config.metrics().intervalSeconds());
        controlFileService.startHeartbeat();
        webSocketServerService.start();
        discoveryBroadcaster.start();
    }

    @Override
    public void onDisable() {
        if (webSocketServerService != null) {
            webSocketServerService.stop();
        }
        if (metricsService != null) {
            metricsService.stop();
        }
        if (controlFileService != null) {
            controlFileService.stopHeartbeat();
        }
        if (logCaptureHandler != null) {
            Logger.getLogger("").removeHandler(logCaptureHandler);
        }
        if (discoveryBroadcaster != null) {
            discoveryBroadcaster.stop();
        }
    }

    private void preloadLatestLog(LogBuffer buffer, Path logPath) {
        if (buffer == null || logPath == null) {
            return;
        }
        if (!Files.exists(logPath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(logPath);
            for (String line : lines) {
                buffer.add(LogBuffer.parseLine(line));
            }
        } catch (IOException ignored) {
        }
    }
}
