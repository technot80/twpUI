package com.servercontroller.plugin.net;

import com.servercontroller.common.protocol.Message;
import com.servercontroller.common.protocol.Protocol;
import com.servercontroller.common.protocol.messages.AuthMessage;
import com.servercontroller.common.protocol.messages.AuthResultMessage;
import com.servercontroller.common.protocol.messages.BroadcastMessage;
import com.servercontroller.common.protocol.messages.CheckUpdatesMessage;
import com.servercontroller.common.protocol.messages.ControlMessage;
import com.servercontroller.common.protocol.messages.DownloadUpdateMessage;
import com.servercontroller.common.protocol.messages.ErrorMessage;
import com.servercontroller.common.protocol.messages.GetPluginsMessage;
import com.servercontroller.common.protocol.messages.LogMessage;
import com.servercontroller.common.protocol.messages.MetricsMessage;
import com.servercontroller.common.protocol.messages.PingMessage;
import com.servercontroller.common.protocol.messages.PluginListMessage;
import com.servercontroller.common.protocol.messages.PongMessage;
import com.servercontroller.common.protocol.messages.SubscribeMessage;
import com.servercontroller.common.protocol.messages.UpdateDownloadedMessage;
import com.servercontroller.common.protocol.messages.UpdateInfoMessage;
import com.servercontroller.plugin.ServerControllerPlugin;
import com.servercontroller.plugin.config.PluginConfig;
import com.servercontroller.plugin.control.ControlAction;
import com.servercontroller.plugin.control.ControlFileService;
import com.servercontroller.plugin.logs.LogBuffer;
import com.servercontroller.plugin.plugins.UpdateChecker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketSessionHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Set<ChannelHandlerContext> sessions = ConcurrentHashMap.newKeySet();
    private static final AttributeKey<SessionPreferences> PREFS_KEY = AttributeKey.valueOf("sc_prefs");
    private static final Set<String> DEFAULT_FILTERS = Set.of("LOG", "METRICS");
    private static final Set<String> DEFAULT_LOG_LEVELS = Set.of("INFO", "WARN", "ERROR");

    private final ServerControllerPlugin plugin;
    private final PluginConfig config;
    private final LogBuffer logBuffer;
    private final com.servercontroller.plugin.metrics.MetricsService metricsService;
    private final ControlFileService controlFileService;
    private final UpdateChecker updateChecker;
    private boolean authenticated;

    public WebSocketSessionHandler(ServerControllerPlugin plugin, PluginConfig config, LogBuffer logBuffer,
                                   com.servercontroller.plugin.metrics.MetricsService metricsService,
                                   ControlFileService controlFileService, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.config = config;
        this.logBuffer = logBuffer;
        this.metricsService = metricsService;
        this.controlFileService = controlFileService;
        this.updateChecker = updateChecker;
    }

    public static void broadcast(Message message) {
        String payload;
        try {
            payload = Protocol.encode(message);
        } catch (Exception e) {
            return;
        }
        for (ChannelHandlerContext ctx : sessions) {
            if (message instanceof LogMessage log && !prefs(ctx).allowsLogLevel(log.level())) {
                continue;
            }
            if (message instanceof MetricsMessage && !prefs(ctx).allowsFilter("METRICS")) {
                continue;
            }
            if (message instanceof LogMessage && !prefs(ctx).allowsFilter("LOG")) {
                continue;
            }
            ctx.writeAndFlush(new TextWebSocketFrame(payload));
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        sessions.add(ctx);
        ctx.channel().attr(PREFS_KEY).set(new SessionPreferences(DEFAULT_FILTERS, DEFAULT_LOG_LEVELS));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        sessions.remove(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        sessions.remove(ctx);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            Message message = Protocol.decode(msg.text());
            if (message instanceof AuthMessage auth) {
                handleAuth(ctx, auth);
                return;
            }
            if (!authenticated) {
                send(ctx, new ErrorMessage("AUTH_REQUIRED", "Authenticate first"));
                return;
            }
            if (message instanceof SubscribeMessage subscribe) {
                handleSubscribe(ctx, subscribe);
            } else if (message instanceof PingMessage ping) {
                send(ctx, new PongMessage(ping.timestamp()));
            } else if (message instanceof BroadcastMessage broadcast) {
                Bukkit.broadcastMessage(broadcast.message());
            } else if (message instanceof ControlMessage control) {
                ControlAction action = ControlAction.fromString(control.action());
                if (action != null) {
                    controlFileService.writeControl(action.wireValue());
                    if (action == ControlAction.RESTART) {
                        Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
                    } else if (action == ControlAction.STOP) {
                        Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
                    }
                }
            } else if (message instanceof CheckUpdatesMessage checkUpdates) {
                updateChecker.checkUpdates(checkUpdates.plugins(), info -> send(ctx, info));
            } else if (message instanceof DownloadUpdateMessage downloadUpdate) {
                UpdateDownloadedMessage downloaded = updateChecker.downloadUpdate(downloadUpdate.pluginName(), downloadUpdate.downloadUrl());
                send(ctx, downloaded);
            } else if (message instanceof GetPluginsMessage) {
                send(ctx, buildPluginList());
            }
        } catch (Exception e) {
            send(ctx, new ErrorMessage("BAD_MESSAGE", e.getMessage()));
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, AuthMessage auth) {
        if (auth.apiKey() != null && auth.apiKey().equals(config.connection().apiKey())) {
            authenticated = true;
            send(ctx, new AuthResultMessage(true, "Connected", plugin.getDescription().getVersion(),
                    Bukkit.getServer().getName()));
            send(ctx, buildPluginList());
        } else {
            send(ctx, new AuthResultMessage(false, "Invalid API key", plugin.getDescription().getVersion(),
                    Bukkit.getServer().getName()));
        }
    }

    private void handleSubscribe(ChannelHandlerContext ctx, SubscribeMessage subscribe) {
        prefs(ctx).update(subscribe.filters(), subscribe.logLevels());
        SessionPreferences preferences = prefs(ctx);
        logBuffer.snapshot().forEach(entry -> {
            if (preferences.allowsFilter("LOG") && preferences.allowsLogLevel(entry.level())) {
                send(ctx, new LogMessage(entry.level(), entry.message(), entry.timestamp()));
            }
        });
        MetricsMessage metrics = metricsService.latest();
        if (metrics != null && preferences.allowsFilter("METRICS")) {
            send(ctx, metrics);
        }
    }

    private PluginListMessage buildPluginList() {
        List<PluginListMessage.PluginSummary> plugins = List.of(Bukkit.getPluginManager().getPlugins()).stream()
                .map(Plugin::getDescription)
                .map(desc -> new PluginListMessage.PluginSummary(
                        desc.getName(),
                        desc.getVersion(),
                        desc.getAuthors().isEmpty() ? "" : desc.getAuthors().get(0),
                        Bukkit.getPluginManager().isPluginEnabled(desc.getName())
                ))
                .toList();
        return new PluginListMessage(plugins);
    }

    private void send(ChannelHandlerContext ctx, Message message) {
        try {
            ctx.writeAndFlush(new TextWebSocketFrame(Protocol.encode(message)));
        } catch (Exception ignored) {
        }
    }

    private static SessionPreferences prefs(ChannelHandlerContext ctx) {
        SessionPreferences prefs = ctx.channel().attr(PREFS_KEY).get();
        if (prefs == null) {
            prefs = new SessionPreferences(DEFAULT_FILTERS, DEFAULT_LOG_LEVELS);
            ctx.channel().attr(PREFS_KEY).set(prefs);
        }
        return prefs;
    }

    private static class SessionPreferences {
        private final Set<String> filters;
        private final Set<String> logLevels;

        private SessionPreferences(Set<String> filters, Set<String> logLevels) {
            this.filters = new HashSet<>(filters);
            this.logLevels = new HashSet<>(logLevels);
        }

        private void update(List<String> newFilters, List<String> newLevels) {
            filters.clear();
            logLevels.clear();
            if (newFilters == null || newFilters.isEmpty()) {
                filters.addAll(DEFAULT_FILTERS);
            } else {
                for (String value : newFilters) {
                    if (value != null) {
                        filters.add(value.trim().toUpperCase(Locale.ROOT));
                    }
                }
            }
            if (newLevels == null || newLevels.isEmpty()) {
                logLevels.addAll(DEFAULT_LOG_LEVELS);
            } else {
                for (String value : newLevels) {
                    if (value != null) {
                        logLevels.add(value.trim().toUpperCase(Locale.ROOT));
                    }
                }
            }
        }

        private boolean allowsFilter(String filter) {
            return filters.contains(filter.toUpperCase(Locale.ROOT));
        }

        private boolean allowsLogLevel(String level) {
            return logLevels.contains(level.toUpperCase(Locale.ROOT));
        }
    }
}
