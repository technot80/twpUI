package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.metrics.PluginMetricSnapshot;
import com.servercontroller.common.protocol.Message;
import java.util.List;
import java.util.Map;

public record MetricsMessage(double tps, double mspt, double cpuUsage, long usedMemoryMb, long maxMemoryMb,
                             int playerCount, int entityCount, int chunkCount,
                             Map<String, Double> foliaRegionMspt,
                             List<PluginMetricSnapshot> pluginMetrics) implements Message {
}
