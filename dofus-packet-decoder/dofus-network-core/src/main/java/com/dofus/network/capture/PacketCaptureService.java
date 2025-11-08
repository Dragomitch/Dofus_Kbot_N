package com.dofus.network.capture;

import com.dofus.network.event.PacketCapturedEvent;
import com.dofus.network.model.Direction;
import com.dofus.network.model.RawPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Service for capturing and processing network packets.
 *
 * Captured packets are queued and processed asynchronously to avoid
 * blocking the network I/O threads.
 */
@Slf4j
@Service
public class PacketCaptureService {

    private final BlockingQueue<RawPacket> captureQueue = new LinkedBlockingQueue<>(10000);
    private final ApplicationEventPublisher eventPublisher;

    public PacketCaptureService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Start async processing of captured packets.
     */
    @PostConstruct
    public void startProcessing() {
        log.info("Starting packet processing thread");

        Thread processingThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    RawPacket packet = captureQueue.take();
                    processPacket(packet);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Packet processing thread interrupted");
                } catch (Exception e) {
                    log.error("Error processing packet", e);
                }
            }
        });

        processingThread.setName("packet-processor");
        processingThread.setDaemon(true);
        processingThread.start();
    }

    /**
     * Capture an inbound packet (Server → Client).
     */
    public void captureInbound(byte[] data, String sessionId) {
        capture(data, Direction.INBOUND, sessionId);
    }

    /**
     * Capture an outbound packet (Client → Server).
     */
    public void captureOutbound(byte[] data, String sessionId) {
        capture(data, Direction.OUTBOUND, sessionId);
    }

    private void capture(byte[] data, Direction direction, String sessionId) {
        RawPacket packet = RawPacket.builder()
                .data(data)
                .direction(direction)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();

        if (!captureQueue.offer(packet)) {
            log.warn("Capture queue full, dropping packet");
        } else {
            log.trace("Captured {} packet: {} bytes", direction, data.length);
        }
    }

    private void processPacket(RawPacket packet) {
        // Publish event for packet decoder
        PacketCapturedEvent event = new PacketCapturedEvent(this, packet);
        eventPublisher.publishEvent(event);

        log.debug("Processed {} packet: {} bytes",
                packet.getDirection(), packet.getData().length);
    }

    /**
     * Get current queue size (for monitoring).
     */
    public int getQueueSize() {
        return captureQueue.size();
    }
}
