package com.servercontroller.plugin.logs;

import java.util.Locale;
import java.util.logging.Level;

public enum LogLevel {
    ERROR(4),
    WARN(3),
    INFO(2),
    DEBUG(1);

    private final int severity;

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int severity() {
        return severity;
    }

    public static LogLevel fromString(String value) {
        if (value == null) {
            return INFO;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ERROR" -> ERROR;
            case "WARN", "WARNING" -> WARN;
            case "DEBUG", "TRACE" -> DEBUG;
            default -> INFO;
        };
    }

    public static LogLevel fromJul(Level level) {
        if (level == null) {
            return INFO;
        }
        if (level.intValue() >= Level.SEVERE.intValue()) {
            return ERROR;
        }
        if (level.intValue() >= Level.WARNING.intValue()) {
            return WARN;
        }
        if (level.intValue() >= Level.INFO.intValue()) {
            return INFO;
        }
        return DEBUG;
    }
}
