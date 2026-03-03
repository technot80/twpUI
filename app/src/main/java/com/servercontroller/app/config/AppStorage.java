package com.servercontroller.app.config;

import java.nio.file.Path;

public final class AppStorage {
    private static final String APP_DIR = "ServerController";

    private AppStorage() {
    }

    public static Path baseDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, APP_DIR);
        }
        return Path.of(System.getProperty("user.home"), ".servercontroller");
    }
}
