package com.dofus.network.capture;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty handler that captures all packets flowing through the proxy.
 *
 * This handler sits in the pipeline and observes traffic without
 * modifying it, passing captured data to PacketCaptureService.
 */
@Slf4j
public class PacketCaptureHandler extends ChannelDuplexHandler {

    private final PacketCaptureService captureService;

    public PacketCaptureHandler(PacketCaptureService captureService) {
        this.captureService = captureService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Inbound: Server → Client
        if (msg instanceof ByteBuf && captureService != null) {
            ByteBuf buffer = (ByteBuf) msg;

            // Copy bytes without consuming them
            byte[] data = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), data);

            // Capture packet
            String sessionId = ctx.channel().id().asShortText();
            captureService.captureInbound(data, sessionId);
        }

        // Pass through to next handler
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Outbound: Client → Server
        if (msg instanceof ByteBuf && captureService != null) {
            ByteBuf buffer = (ByteBuf) msg;

            byte[] data = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), data);

            String sessionId = ctx.channel().id().asShortText();
            captureService.captureOutbound(data, sessionId);
        }

        // Pass through to next handler
        super.write(ctx, msg, promise);
    }
}
