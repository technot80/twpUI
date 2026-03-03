package com.servercontroller.app.ui.viewmodel;

import com.servercontroller.app.net.ServerConnection;
import com.servercontroller.app.net.ServerMessageListener;
import com.servercontroller.app.ui.model.PluginMetricRow;
import com.servercontroller.common.metrics.PluginMetricSnapshot;
import com.servercontroller.common.protocol.Message;
import com.servercontroller.common.protocol.messages.AuthResultMessage;
import com.servercontroller.common.protocol.messages.LogMessage;
import com.servercontroller.common.protocol.messages.MetricsMessage;
import com.servercontroller.common.protocol.messages.PluginListMessage;
import com.servercontroller.common.protocol.messages.UpdateInfoMessage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerTabViewModel implements ServerMessageListener {
    private static final DecimalFormat METRIC_FORMAT = new DecimalFormat("0.##");

    private final StringProperty status = new SimpleStringProperty("Offline");
    private final StringProperty tps = new SimpleStringProperty("-");
    private final StringProperty mspt = new SimpleStringProperty("-");
    private final StringProperty cpu = new SimpleStringProperty("-");
    private final StringProperty memory = new SimpleStringProperty("-");
    private final StringProperty players = new SimpleStringProperty("-");
    private final StringProperty entities = new SimpleStringProperty("-");

    private final ObservableList<PluginListMessage.PluginSummary> plugins = FXCollections.observableArrayList();
    private final ObservableList<LogMessage> logs = FXCollections.observableArrayList();
    private final ObservableList<PluginMetricRow> pluginMetrics = FXCollections.observableArrayList();

    private final ServerConnection connection;
    private final List<String> pendingUpdatePlugins = new ArrayList<>();
    private BiConsumer<String, UpdateInfoMessage> updateInfoListener;

    public ServerTabViewModel(ServerConnection connection) {
        this.connection = connection;
        this.connection.addListener(this);
    }

    public void connect() {
        connection.connect();
    }

    public void broadcast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        connection.broadcast(message);
    }

    public void restart() {
        connection.restart();
    }

    public void stop() {
        connection.stop();
    }

    public void setLogLevels(java.util.List<String> logLevels) {
        connection.setLogLevels(logLevels);
    }

    public void checkUpdates() {
        pendingUpdatePlugins.clear();
        pendingUpdatePlugins.addAll(plugins.stream().map(PluginListMessage.PluginSummary::name).toList());
        connection.checkUpdates(pendingUpdatePlugins);
    }

    public void downloadUpdate(String pluginName, String downloadUrl) {
        connection.downloadUpdate(pluginName, downloadUrl);
    }

    public void setUpdateInfoListener(BiConsumer<String, UpdateInfoMessage> updateInfoListener) {
        this.updateInfoListener = updateInfoListener;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty tpsProperty() {
        return tps;
    }

    public StringProperty msptProperty() {
        return mspt;
    }

    public StringProperty cpuProperty() {
        return cpu;
    }

    public StringProperty memoryProperty() {
        return memory;
    }

    public StringProperty playersProperty() {
        return players;
    }

    public StringProperty entitiesProperty() {
        return entities;
    }

    public ObservableList<PluginListMessage.PluginSummary> plugins() {
        return plugins;
    }

    public ObservableList<LogMessage> logs() {
        return logs;
    }

    public ObservableList<PluginMetricRow> pluginMetrics() {
        return pluginMetrics;
    }

    @Override
    public void onMessage(Message message) {
        Platform.runLater(() -> {
            if (message instanceof AuthResultMessage auth) {
                status.set(auth.success() ? "Online" : "Auth failed");
            } else if (message instanceof MetricsMessage metrics) {
                tps.set(String.format("%.2f", metrics.tps()));
                mspt.set(String.format("%.2f", metrics.mspt()));
                cpu.set(String.format("%.1f%%", metrics.cpuUsage()));
                memory.set(metrics.usedMemoryMb() + " / " + metrics.maxMemoryMb() + " MB");
                players.set(String.valueOf(metrics.playerCount()));
                entities.set(String.valueOf(metrics.entityCount()));
                updatePluginMetrics(metrics.pluginMetrics());
            } else if (message instanceof PluginListMessage list) {
                plugins.setAll(list.plugins());
                status.set("Online");
            } else if (message instanceof LogMessage log) {
                logs.add(log);
                status.set("Online");
            } else if (message instanceof UpdateInfoMessage updateInfo) {
                if (updateInfoListener != null) {
                    updateInfoListener.accept(updateInfo.pluginName(), updateInfo);
                }
                if (updateInfo.hasUpdate()) {
                    status.set("Update available: " + updateInfo.pluginName());
                }
            }
        });
    }

    @Override
    public void onError(Throwable error) {
        Platform.runLater(() -> status.set("Disconnected"));
    }

    private void updatePluginMetrics(List<PluginMetricSnapshot> snapshots) {
        if (snapshots == null) {
            return;
        }
        List<PluginMetricRow> rows = new ArrayList<>();
        for (PluginMetricSnapshot snapshot : snapshots) {
            if (snapshot == null || snapshot.values() == null) {
                continue;
            }
            List<Map.Entry<String, Double>> entries = new ArrayList<>(snapshot.values().entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getKey));
            for (Map.Entry<String, Double> entry : entries) {
                String value = entry.getValue() == null ? "-" : METRIC_FORMAT.format(entry.getValue());
                rows.add(new PluginMetricRow(snapshot.pluginName(), entry.getKey(), value));
            }
        }
        pluginMetrics.setAll(rows);
    }
}
