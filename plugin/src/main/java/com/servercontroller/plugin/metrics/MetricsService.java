package com.servercontroller.plugin.metrics;

import com.servercontroller.common.metrics.PluginMetricSnapshot;
import com.servercontroller.common.protocol.messages.MetricsMessage;
import com.servercontroller.plugin.ServerControllerPlugin;
import com.servercontroller.plugin.plugins.PluginMetricsRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsService {
    private final ServerControllerPlugin plugin;
    private final PluginMetricsRegistry pluginMetricsRegistry;
    private final CpuUsageTracker cpuUsageTracker;
    private volatile MetricsMessage latest;
    private BukkitTask task;
    private ScheduledExecutorService scheduler;
    private Object foliaScheduler;
    private Method foliaRunMethod;
    private boolean foliaRunUsesConsumer;
    private boolean foliaRunFailed;

    public MetricsService(ServerControllerPlugin plugin) {
        this.plugin = plugin;
        this.pluginMetricsRegistry = new PluginMetricsRegistry();
        this.cpuUsageTracker = new CpuUsageTracker();
    }

    public void start(int intervalSeconds) {
        long intervalTicks = Math.max(1, intervalSeconds) * 20L;
        Object scheduler = resolveGlobalScheduler();
        if (scheduler != null) {
            if (!scheduleFolia(scheduler, intervalSeconds)) {
                plugin.getLogger().warning("Folia scheduler available but metrics task failed; metrics disabled.");
            }
            return;
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::collect, 0L, intervalTicks);
    }

    public void stop() {
        shutdownScheduler();
        if (task != null) {
            task.cancel();
            task = null;
        }
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

    private Object resolveGlobalScheduler() {
        try {
            return Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resolve Folia scheduler: " + e.getClass().getSimpleName());
            return null;
        }
    }

    private boolean scheduleFolia(Object scheduler, int intervalSeconds) {
        foliaScheduler = scheduler;
        foliaRunMethod = resolveFoliaRunMethod(scheduler);
        if (foliaRunMethod == null) {
            plugin.getLogger().warning("Folia scheduler run method not found.");
            return false;
        }
        scheduler = null;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::dispatchFoliaCollect, 0L, Math.max(1, intervalSeconds), TimeUnit.SECONDS);
        return true;
    }

    private Method resolveFoliaRunMethod(Object scheduler) {
        Method fallback = null;
        for (Method method : scheduler.getClass().getMethods()) {
            String name = method.getName();
            if (!name.equals("run") && !name.equals("execute")) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 2) {
                continue;
            }
            if (!Plugin.class.isAssignableFrom(params[0])) {
                continue;
            }
            if (java.util.function.Consumer.class.isAssignableFrom(params[1])) {
                foliaRunUsesConsumer = true;
                return method;
            }
            if (Runnable.class.isAssignableFrom(params[1])) {
                foliaRunUsesConsumer = false;
                fallback = method;
            }
        }
        return fallback;
    }

    private void dispatchFoliaCollect() {
        if (foliaRunFailed || foliaScheduler == null || foliaRunMethod == null) {
            return;
        }
        try {
            if (foliaRunUsesConsumer) {
                java.util.function.Consumer<Object> consumer = ignored -> collect();
                foliaRunMethod.invoke(foliaScheduler, plugin, consumer);
            } else {
                foliaRunMethod.invoke(foliaScheduler, plugin, (Runnable) this::collect);
            }
        } catch (Exception e) {
            foliaRunFailed = true;
            plugin.getLogger().warning("Failed to schedule Folia metrics task: " + e.getClass().getSimpleName());
            shutdownScheduler();
        }
    }

    private void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}
