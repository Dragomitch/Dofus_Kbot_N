package com.dofus.network.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles bidirectional traffic forwarding between client and server.
 *
 * Client → [ProxyHandler] → Server
 * Client ← [ProxyHandler] ← Server
 */
@Slf4j
public class ProxyHandler extends ChannelInboundHandlerAdapter {

    private final String targetHost;
    private final int targetPort;

    private Channel outboundChannel; // Connection to Dofus server

    public ProxyHandler(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected: {}", ctx.channel().remoteAddress());

        // When client connects, establish connection to Dofus server
        final Channel inboundChannel = ctx.channel();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // Handler for server → client traffic
                        ch.pipeline().addLast(new ServerToClientHandler(inboundChannel));
                    }
                })
                .option(ChannelOption.AUTO_READ, false);

        ChannelFuture future = bootstrap.connect(targetHost, targetPort);
        outboundChannel = future.channel();

        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("Connected to Dofus server: {}:{}", targetHost, targetPort);
                inboundChannel.read(); // Start reading from client
            } else {
                log.error("Failed to connect to Dofus server", channelFuture.cause());
                inboundChannel.close();
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Client → Server (OUTBOUND from client perspective)
        if (outboundChannel != null && outboundChannel.isActive()) {
            ByteBuf data = (ByteBuf) msg;

            // Log packet
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            log.debug("Client → Server: {} bytes", bytes.length);

            // Forward to server
            outboundChannel.writeAndFlush(data).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read(); // Continue reading from client
                } else {
                    future.channel().close();
                }
            });
        } else {
            log.warn("Outbound channel not available, dropping packet");
            ((ByteBuf) msg).release();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected: {}", ctx.channel().remoteAddress());

        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Proxy error", cause);
        closeOnFlush(ctx.channel());
    }

    private static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Handler for server → client traffic.
     */
    private static class ServerToClientHandler extends ChannelInboundHandlerAdapter {

        private final Channel inboundChannel; // Client connection

        public ServerToClientHandler(Channel inboundChannel) {
            this.inboundChannel = inboundChannel;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // Server → Client (INBOUND to client)
            ByteBuf data = (ByteBuf) msg;

            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            log.debug("Server → Client: {} bytes", bytes.length);

            // Forward to client
            inboundChannel.writeAndFlush(data).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read(); // Continue reading from server
                } else {
                    future.channel().close();
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            closeOnFlush(inboundChannel);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("Server relay error", cause);
            closeOnFlush(ctx.channel());
        }
    }
}
