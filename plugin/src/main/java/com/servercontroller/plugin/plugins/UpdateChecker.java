package com.servercontroller.plugin.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servercontroller.common.protocol.messages.UpdateDownloadedMessage;
import com.servercontroller.common.protocol.messages.UpdateInfoMessage;
import com.servercontroller.plugin.ServerControllerPlugin;
import com.servercontroller.plugin.config.PluginConfig;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class UpdateChecker {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ServerControllerPlugin plugin;
    private final PluginConfig.Updates config;
    private final Map<String, CachedUpdate> cache;

    public UpdateChecker(ServerControllerPlugin plugin, PluginConfig.Updates config) {
        this.plugin = plugin;
        this.config = config;
        this.cache = new ConcurrentHashMap<>();
    }

    public void checkUpdates(List<String> pluginNames, Consumer<UpdateInfoMessage> consumer) {
        if (pluginNames == null) {
            return;
        }
        for (String name : pluginNames) {
            UpdateInfoMessage info = checkSingle(name);
            consumer.accept(info);
        }
    }

    public UpdateDownloadedMessage downloadUpdate(String pluginName, String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            return new UpdateDownloadedMessage(pluginName, "", false);
        }
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            File pluginsFolder = new File(plugin.getDataFolder().getParentFile(), "plugins");
            String safeName = pluginName.replaceAll("[^a-zA-Z0-9._-]", "_");
            File target = new File(pluginsFolder, safeName + "-update.jar");

            try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(target)) {
                in.transferTo(out);
            }
            return new UpdateDownloadedMessage(pluginName, target.getName(), true);
        } catch (Exception e) {
            return new UpdateDownloadedMessage(pluginName, "", false);
        }
    }

    private UpdateInfoMessage checkSingle(String name) {
        CachedUpdate cached = cache.get(name);
        if (cached != null && !cached.expired(config.cacheMinutes())) {
            return cached.info();
        }
        UpdateInfoMessage info = lookupSpigot(name);
        cache.put(name, new CachedUpdate(info));
        return info;
    }

    private UpdateInfoMessage lookupSpigot(String name) {
        Integer resourceId = config.spigotResources().get(name);
        if (resourceId == null) {
            return new UpdateInfoMessage(name, false, "", "", "", "SPIGOT");
        }
        try {
            URL url = new URL("https://api.spiget.org/v2/resources/" + resourceId + "/versions/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            try (InputStream in = connection.getInputStream()) {
                JsonNode node = MAPPER.readTree(in);
                String latestVersion = node.path("name").asText();
                String downloadUrl = "https://api.spiget.org/v2/resources/" + resourceId + "/download";
                Plugin pluginInstance = plugin.getServer().getPluginManager().getPlugin(name);
                String currentVersion = pluginInstance != null ? pluginInstance.getDescription().getVersion() : "";
                boolean hasUpdate = !currentVersion.isBlank() && !currentVersion.equalsIgnoreCase(latestVersion);
                return new UpdateInfoMessage(name, hasUpdate, currentVersion, latestVersion, downloadUrl, "SPIGOT");
            }
        } catch (Exception e) {
            return new UpdateInfoMessage(name, false, "", "", "", "SPIGOT");
        }
    }

    private record CachedUpdate(UpdateInfoMessage info, long timestamp) {
        public CachedUpdate(UpdateInfoMessage info) {
            this(info, Instant.now().toEpochMilli());
        }

        public boolean expired(int cacheMinutes) {
            return Instant.now().toEpochMilli() - timestamp > cacheMinutes * 60_000L;
        }
    }
}
