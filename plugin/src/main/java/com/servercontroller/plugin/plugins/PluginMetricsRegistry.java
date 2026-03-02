package com.servercontroller.plugin.plugins;

import com.servercontroller.common.metrics.PluginMetricSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginMetricsRegistry {
    public List<PluginMetricSnapshot> collectAll() {
        List<PluginMetricSnapshot> snapshots = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof ServerControllerMetrics metricsProvider) {
                Map<String, Double> metrics = metricsProvider.getMetrics();
                snapshots.add(new PluginMetricSnapshot(plugin.getName(), metrics));
            }
        }
        return snapshots;
    }
}
