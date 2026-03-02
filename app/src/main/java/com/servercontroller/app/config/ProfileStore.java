package com.servercontroller.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ProfileStore {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String FILE_NAME = "profiles.json";

    public List<ServerProfile> load(Path baseDir) {
        Path file = baseDir.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(Files.readString(file), new TypeReference<>() {
            });
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void save(Path baseDir, List<ServerProfile> profiles) {
        Path file = baseDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(baseDir);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), profiles);
        } catch (IOException ignored) {
        }
    }
}
