package com.dofus.network.proxy;

import com.dofus.network.capture.PacketCaptureHandler;
import com.dofus.network.capture.PacketCaptureService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

/**
 * Initializes the channel pipeline for proxy connections.
 */
@RequiredArgsConstructor
public class ProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final String targetHost;
    private final int targetPort;
    private final PacketCaptureService captureService;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add handlers to the pipeline
        // Order matters: inbound handlers first, outbound handlers last

        // Packet capture handler (observes traffic)
        pipeline.addLast("packetCapture", new PacketCaptureHandler(captureService));

        // Proxy handler (forwards traffic)
        pipeline.addLast("proxyHandler", new ProxyHandler(targetHost, targetPort));
    }
}
