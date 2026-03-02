package com.servercontroller.plugin.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public record PluginConfig(Connection connection, Logging logging, Metrics metrics, Updates updates, Control control,
                           Discovery discovery) {
    public static PluginConfig from(FileConfiguration config) {
        Connection connection = new Connection(
                config.getString("connection.host", "0.0.0.0"),
                config.getInt("connection.port", 8765),
                config.getString("connection.api-key", "change-me"),
                new Tls(
                        config.getBoolean("connection.tls.enabled", true),
                        config.getString("connection.tls.keystore-path", "keystore.p12"),
                        config.getString("connection.tls.keystore-password", "change-me"),
                        config.getString("connection.tls.key-alias", "servercontroller")
                )
        );
        Logging logging = new Logging(
                config.getString("logging.level", "INFO"),
                config.getInt("logging.buffer-size", 1000)
        );
        Metrics metrics = new Metrics(
                config.getInt("metrics.interval-seconds", 5)
        );
        Updates updates = new Updates(
                config.getInt("updates.cache-minutes", 60),
                readSpigotResources(config)
        );
        Control control = new Control(
                config.getString("control.file", ".server-control"),
                config.getString("control.heartbeat-file", ".server-heartbeat"),
                config.getString("control.status-file", ".server-status")
        );
        Discovery discovery = new Discovery(
                config.getBoolean("discovery.enabled", true),
                config.getInt("discovery.port", 8766),
                config.getInt("discovery.interval-seconds", 10)
        );
        return new PluginConfig(connection, logging, metrics, updates, control, discovery);
    }

    private static Map<String, Integer> readSpigotResources(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("updates.spigot-resources");
        Map<String, Integer> map = new HashMap<>();
        if (section == null) {
            return map;
        }
        for (String key : section.getKeys(false)) {
            map.put(key, section.getInt(key));
        }
        return map;
    }

    public record Connection(String host, int port, String apiKey, Tls tls) {
    }

    public record Tls(boolean enabled, String keystorePath, String keystorePassword, String keyAlias) {
    }

    public record Logging(String level, int bufferSize) {
    }

    public record Metrics(int intervalSeconds) {
    }

    public record Updates(int cacheMinutes, Map<String, Integer> spigotResources) {
    }

    public record Control(String file, String heartbeatFile, String statusFile) {
    }

    public record Discovery(boolean enabled, int port, int intervalSeconds) {
    }
}
