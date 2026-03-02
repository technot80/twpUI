package com.servercontroller.plugin.metrics;

import com.servercontroller.common.metrics.PluginMetricSnapshot;
import com.servercontroller.common.protocol.messages.MetricsMessage;
import com.servercontroller.plugin.ServerControllerPlugin;
import com.servercontroller.plugin.plugins.PluginMetricsRegistry;
import org.bukkit.Server;
import org.bukkit.World;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsService {
    private final ServerControllerPlugin plugin;
    private final ScheduledExecutorService scheduler;
    private final PluginMetricsRegistry pluginMetricsRegistry;
    private final CpuUsageTracker cpuUsageTracker;
    private volatile MetricsMessage latest;

    public MetricsService(ServerControllerPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.pluginMetricsRegistry = new PluginMetricsRegistry();
        this.cpuUsageTracker = new CpuUsageTracker();
    }

    public void start(int intervalSeconds) {
        scheduler.scheduleAtFixedRate(this::collect, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public MetricsMessage latest() {
        return latest;
    }

    private void collect() {
        try {
            Server server = plugin.getServer();
            double tps = server.getTPS()[0];
            double mspt = server.getAverageTickTime();
            int playerCount = server.getOnlinePlayers().size();
            int entityCount = 0;
            int chunkCount = 0;
            for (World world : server.getWorlds()) {
                entityCount += world.getEntities().size();
                chunkCount += world.getLoadedChunks().length;
            }

            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memoryBean.getHeapMemoryUsage();
            long usedMb = heap.getUsed() / 1024 / 1024;
            long maxMb = heap.getMax() / 1024 / 1024;

            double cpuUsage = cpuUsageTracker.samplePercent();

            Map<String, Double> foliaRegionMspt = new HashMap<>();
            List<PluginMetricSnapshot> pluginMetrics = pluginMetricsRegistry.collectAll();

            latest = new MetricsMessage(
                    tps,
                    mspt,
                    cpuUsage,
                    usedMb,
                    maxMb,
                    playerCount,
                    entityCount,
                    chunkCount,
                    foliaRegionMspt,
                    pluginMetrics
            );
        } catch (Exception ignored) {
        }
    }
}
