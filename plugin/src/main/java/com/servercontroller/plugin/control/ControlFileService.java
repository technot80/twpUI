package com.servercontroller.plugin.control;

import com.servercontroller.plugin.config.PluginConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ControlFileService {
    private final Path dataFolder;
    private final PluginConfig.Control control;
    private final ScheduledExecutorService heartbeatExecutor;

    public ControlFileService(Path dataFolder, PluginConfig.Control control) {
        this.dataFolder = dataFolder;
        this.control = control;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(this::writeHeartbeat, 0, 5, TimeUnit.SECONDS);
    }

    public void stopHeartbeat() {
        heartbeatExecutor.shutdownNow();
    }

    public void writeControl(String action) {
        ControlAction controlAction = ControlAction.fromString(action);
        if (controlAction == null) {
            return;
        }
        writeAtomic(control.file(), controlAction.wireValue());
    }

    public void writeStatus(String status) {
        writeAtomic(control.statusFile(), status);
    }

    private void writeHeartbeat() {
        writeAtomic(control.heartbeatFile(), Long.toString(Instant.now().toEpochMilli()));
    }

    private void writeAtomic(String fileName, String content) {
        try {
            Files.createDirectories(dataFolder);
            Path target = dataFolder.resolve(fileName);
            Path temp = dataFolder.resolve(fileName + ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
        }
    }
}
