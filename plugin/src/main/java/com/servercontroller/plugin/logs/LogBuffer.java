package com.servercontroller.plugin.logs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;

public class LogBuffer implements Listener {
    private static final Pattern BRACKET_TIME_LEVEL = Pattern.compile("^\\[[0-9:]+\\s+([A-Z]+)]\\s*:?\\s*(.*)$");
    private static final Pattern THREAD_LEVEL = Pattern.compile("^\\[[^\\]/]+/([A-Z]+)]\\s*:?\\s*(.*)$");
    private static final Pattern SIMPLE_LEVEL = Pattern.compile("^\\[([A-Z]+)]\\s*:?\\s*(.*)$");
    private final Deque<LogEntry> buffer;
    private final int maxSize;
    private final LogLevel minimumLevel;
    private Consumer<LogEntry> onEntry;

    public LogBuffer(int maxSize) {
        this(maxSize, LogLevel.INFO);
    }

    public LogBuffer(int maxSize, LogLevel minimumLevel) {
        this.buffer = new ArrayDeque<>(maxSize);
        this.maxSize = maxSize;
        this.minimumLevel = minimumLevel;
    }

    public synchronized void add(String level, String message) {
        LogLevel logLevel = LogLevel.fromString(level);
        if (logLevel.severity() < minimumLevel.severity()) {
            return;
        }
        if (buffer.size() >= maxSize) {
            buffer.removeFirst();
        }
        LogEntry entry = new LogEntry(logLevel.name(), message, Instant.now().toEpochMilli());
        buffer.addLast(entry);
        if (onEntry != null) {
            onEntry.accept(entry);
        }
    }

    public synchronized void add(LogEntry entry) {
        if (entry == null) {
            return;
        }
        LogLevel logLevel = LogLevel.fromString(entry.level());
        if (logLevel.severity() < minimumLevel.severity()) {
            return;
        }
        if (buffer.size() >= maxSize) {
            buffer.removeFirst();
        }
        buffer.addLast(entry);
        if (onEntry != null) {
            onEntry.accept(entry);
        }
    }

    public synchronized Deque<LogEntry> snapshot() {
        return new ArrayDeque<>(buffer);
    }

    public void setOnEntry(Consumer<LogEntry> onEntry) {
        this.onEntry = onEntry;
    }

    public static LogEntry parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String level = "INFO";
        String message = line;
        Matcher matcher = BRACKET_TIME_LEVEL.matcher(line);
        if (matcher.matches()) {
            level = matcher.group(1);
            message = matcher.group(2);
        } else {
            matcher = THREAD_LEVEL.matcher(line);
            if (matcher.matches()) {
                level = matcher.group(1);
                message = matcher.group(2);
            } else {
                matcher = SIMPLE_LEVEL.matcher(line);
                if (matcher.matches()) {
                    level = matcher.group(1);
                    message = matcher.group(2);
                }
            }
        }
        LogLevel logLevel = LogLevel.fromString(level);
        String normalized = message == null || message.isBlank() ? line : message.trim();
        return new LogEntry(logLevel.name(), normalized, Instant.now().toEpochMilli());
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        add("INFO", "Command: " + event.getCommand());
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        add("INFO", "Server loaded: " + event.getType().name());
    }

    public record LogEntry(String level, String message, long timestamp) {
    }
}
