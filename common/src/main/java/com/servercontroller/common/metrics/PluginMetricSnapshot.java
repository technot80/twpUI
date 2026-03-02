package com.servercontroller.common.metrics;

import java.util.Map;

public record PluginMetricSnapshot(String pluginName, Map<String, Double> values) {
}
