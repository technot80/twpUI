package com.servercontroller.common.logging;

public record LogEntry(LogLevel level, String message, long timestamp) {
}
