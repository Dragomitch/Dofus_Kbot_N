package com.dofus.network.proxy;

import com.dofus.network.capture.PacketCaptureService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Netty-based MITM proxy server for intercepting Dofus network traffic.
 *
 * This proxy sits between the Dofus client and server, capturing all
 * bidirectional traffic for packet analysis and decoding.
 */
@Slf4j
@Component
public class DofusProxyServer {

    @Value("${dofus.network.proxy.port:5555}")
    private int proxyPort;

    @Value("${dofus.network.proxy.target-host:34.251.172.139}")
    private String targetHost;

    @Value("${dofus.network.proxy.target-port:443}")
    private int targetPort;

    @Value("${dofus.network.proxy.enabled:true}")
    private boolean enabled;

    private final PacketCaptureService captureService;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public DofusProxyServer(PacketCaptureService captureService) {
        this.captureService = captureService;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        if (!enabled) {
            log.info("Dofus proxy is disabled");
            return;
        }

        log.info("Starting Dofus MITM proxy on port {}", proxyPort);
        log.info("Target server: {}:{}", targetHost, targetPort);

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ProxyChannelInitializer(targetHost, targetPort, captureService))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            // Bind and start to accept incoming connections
            ChannelFuture future = bootstrap.bind(proxyPort).sync();
            serverChannel = future.channel();

            log.info("Dofus proxy started successfully on port {}", proxyPort);

            // Don't block here - let Spring Boot continue
            // The server will run in background threads

        } catch (Exception e) {
            log.error("Failed to start proxy server", e);
            shutdown();
            throw new RuntimeException("Failed to start proxy", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Dofus proxy...");

        if (serverChannel != null) {
            serverChannel.close();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        log.info("Dofus proxy stopped");
    }
}
