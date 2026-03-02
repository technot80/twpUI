package com.servercontroller.plugin.metrics;

import java.time.Duration;
import java.time.Instant;

public class CpuUsageTracker {
    private Instant lastSampleTime;
    private Duration lastCpuTime;

    public double samplePercent() {
        Instant now = Instant.now();
        Duration cpuTime = ProcessHandle.current().info().totalCpuDuration().orElse(Duration.ZERO);
        if (lastSampleTime == null || lastCpuTime == null) {
            lastSampleTime = now;
            lastCpuTime = cpuTime;
            return 0.0;
        }
        long elapsedNanos = Duration.between(lastSampleTime, now).toNanos();
        long cpuNanos = cpuTime.minus(lastCpuTime).toNanos();
        lastSampleTime = now;
        lastCpuTime = cpuTime;
        if (elapsedNanos <= 0) {
            return 0.0;
        }
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        double usage = (cpuNanos / (double) elapsedNanos) / cores * 100.0;
        return Math.max(0.0, Math.min(100.0, usage));
    }
}
