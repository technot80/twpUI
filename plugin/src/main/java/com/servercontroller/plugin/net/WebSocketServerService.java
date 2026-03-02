package com.servercontroller.plugin.net;

import com.servercontroller.common.protocol.messages.LogMessage;
import com.servercontroller.common.protocol.messages.MetricsMessage;
import com.servercontroller.plugin.ServerControllerPlugin;
import com.servercontroller.plugin.config.PluginConfig;
import com.servercontroller.plugin.control.ControlFileService;
import com.servercontroller.plugin.logs.LogBuffer;
import com.servercontroller.plugin.metrics.MetricsService;
import com.servercontroller.plugin.plugins.UpdateChecker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketServerService {
    private final ServerControllerPlugin plugin;
    private final PluginConfig config;
    private final LogBuffer logBuffer;
    private final MetricsService metricsService;
    private final ControlFileService controlFileService;
    private final UpdateChecker updateChecker;
    private final ScheduledExecutorService scheduler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public WebSocketServerService(ServerControllerPlugin plugin, PluginConfig config, LogBuffer logBuffer,
                                  MetricsService metricsService, ControlFileService controlFileService) {
        this.plugin = plugin;
        this.config = config;
        this.logBuffer = logBuffer;
        this.metricsService = metricsService;
        this.controlFileService = controlFileService;
        this.updateChecker = new UpdateChecker(plugin, config.updates());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            SslContext sslContext = buildSslContext();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true));
                            ch.pipeline().addLast(new WebSocketSessionHandler(plugin, config, logBuffer, metricsService,
                                    controlFileService, updateChecker));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            channel = bootstrap.bind(config.connection().host(), config.connection().port()).sync().channel();
            logBuffer.setOnEntry(entry -> WebSocketSessionHandler.broadcast(
                    new LogMessage(entry.level(), entry.message(), entry.timestamp())));
            scheduler.scheduleAtFixedRate(this::broadcastMetrics, 1, config.metrics().intervalSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start WebSocket server: " + e.getMessage());
        }
    }

    public void stop() {
        scheduler.shutdownNow();
        if (channel != null) {
            channel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }

    private void broadcastMetrics() {
        MetricsMessage metrics = metricsService.latest();
        if (metrics != null) {
            WebSocketSessionHandler.broadcast(metrics);
        }
    }

    private SslContext buildSslContext() throws Exception {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }
}
