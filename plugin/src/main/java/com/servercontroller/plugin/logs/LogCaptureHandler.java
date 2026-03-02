package com.servercontroller.plugin.logs;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogCaptureHandler extends Handler {
    private final LogBuffer buffer;

    public LogCaptureHandler(LogBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        LogLevel level = LogLevel.fromJul(record.getLevel());
        buffer.add(level.name(), record.getMessage());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
