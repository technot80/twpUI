# twpUI

**Work in progress. Do not use or test in production.**

Control multiple Paper/Folia servers from one JavaFX app. This repo contains a Minecraft plugin, a desktop app, and a shared protocol module.

## Features

- Multi-server control with per-server tabs
- Secure, encrypted WebSocket connection (self-signed TLS + API key)
- Real-time logs with filter toggles (ERROR/WARN/INFO/DEBUG)
- Server metrics (TPS, MSPT, CPU, memory, entities, chunks)
- Per-plugin metrics panel (public API for plugins)
- Plugin list + update checking and download (manual install/restart)
- Broadcast messages, stop and restart controls
- LAN discovery via UDP (API key still required)

## Architecture

- `plugin`: Paper/Folia plugin that exposes a WebSocket control API
- `app`: JavaFX desktop app to connect and control servers
- `common`: shared protocol DTOs and message definitions

## Requirements

- Java 21+
- Paper or Folia (1.20.6+)
- Gradle (or use the bundled Gradle install)

## Build

```bash
"C:\Users\torin\Downloads\gradle-9.3.1-all\gradle-9.3.1\bin\gradle" build
```

## Run the App

```bash
"C:\Users\torin\Downloads\gradle-9.3.1-all\gradle-9.3.1\bin\gradle" :app:run
```

## One-Click Windows App (App Image)

Generate a self-contained app image with `jpackage`:

```bash
"C:\Users\torin\Downloads\gradle-9.3.1-all\gradle-9.3.1\bin\gradle" :app:jpackageAppImage
```

Run it from:

```
app\build\jpackage\twpUI\twpUI.exe
```

## Install the Plugin

```bash
"C:\Users\torin\Downloads\gradle-9.3.1-all\gradle-9.3.1\bin\gradle" :plugin:build
```

Copy the jar from `plugin/build/libs/` to your server `plugins/` folder.

## Plugin Configuration

`plugin/src/main/resources/config.yml` (generated at first run):

```yaml
connection:
  host: 0.0.0.0
  port: 8765
  api-key: "change-me"
  tls:
    enabled: true
    keystore-path: "keystore.p12"
    keystore-password: "change-me"
    key-alias: "servercontroller"
logging:
  level: INFO
  buffer-size: 1000
metrics:
  interval-seconds: 5
updates:
  cache-minutes: 60
  spigot-resources:
    # Example:
    # EssentialsX: 9089
control:
  file: ".server-control"
  heartbeat-file: ".server-heartbeat"
  status-file: ".server-status"
discovery:
  enabled: true
  port: 8766
  interval-seconds: 10
```

### Plugin Updates

Update checks use Spiget. Map plugin names to Spigot resource IDs under `updates.spigot-resources`.

The app only downloads updates into the `plugins/` folder. It never auto-restarts or auto-reloads.

## Server Control Scripts

Use these to honor stop/restart requests from the app:

- `scripts/run-server.sh`
- `scripts/run-server.bat`

Place the script in the server root and start the server using it. The plugin writes the `.server-control` flag file to the server root.

## App Usage

- Add a server with host, port, and API key
- Or use LAN discovery, then enter the API key
- Logs stream live with filter toggles
- Use Broadcast/Stop/Restart from the Control panel

## Per-Plugin Metrics API

Other plugins can expose metrics by implementing the public API:

```java
import com.servercontroller.plugin.plugins.ServerControllerMetrics;

public class MyPlugin extends JavaPlugin implements ServerControllerMetrics {
    @Override
    public Map<String, Double> getMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("jobsCompleted", 42.0);
        metrics.put("activeUsers", 7.0);
        return metrics;
    }
}
```

These appear in the app under the **Plugin Metrics** panel.

## Security Notes

- TLS is self-signed and trusted by the app (intended for private networks)
- API keys are required for all connections
- App stores keys in the OS keychain

## License

MIT. See `LICENSE`.
