package com.servercontroller.plugin.plugins;

import java.util.Map;

public interface ServerControllerMetrics {
    Map<String, Double> getMetrics();
}
