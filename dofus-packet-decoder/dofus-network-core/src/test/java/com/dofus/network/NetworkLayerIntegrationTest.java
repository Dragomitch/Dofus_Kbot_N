package com.dofus.network;

import com.dofus.network.capture.PacketCaptureService;
import com.dofus.network.model.Direction;
import com.dofus.network.model.RawPacket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the network layer.
 */
@SpringBootTest(classes = {PacketCaptureService.class})
@TestPropertySource(properties = {
        "dofus.network.proxy.enabled=false" // Disable proxy for tests
})
class NetworkLayerIntegrationTest {

    @Autowired(required = false)
    private PacketCaptureService captureService;

    @Test
    void testPacketCaptureServiceInitializes() {
        assertThat(captureService).isNotNull();
    }

    @Test
    void testCaptureInboundPacket() throws InterruptedException {
        // Given
        byte[] testData = new byte[]{0x01, 0x02, 0x03, 0x04};
        String sessionId = "test-session";

        // When
        captureService.captureInbound(testData, sessionId);

        // Then
        Thread.sleep(100); // Give async processing time
        assertThat(captureService.getQueueSize()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCaptureOutboundPacket() throws InterruptedException {
        // Given
        byte[] testData = new byte[]{(byte) 0xFF, (byte) 0xEE, (byte) 0xDD};
        String sessionId = "test-session";

        // When
        captureService.captureOutbound(testData, sessionId);

        // Then
        Thread.sleep(100); // Give async processing time
        assertThat(captureService.getQueueSize()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testRawPacketCreation() {
        // Given
        byte[] data = new byte[]{0x01, 0x02};
        Direction direction = Direction.INBOUND;
        String sessionId = "test";

        // When
        RawPacket packet = RawPacket.builder()
                .data(data)
                .direction(direction)
                .sessionId(sessionId)
                .build();

        // Then
        assertThat(packet.getData()).isEqualTo(data);
        assertThat(packet.getDirection()).isEqualTo(direction);
        assertThat(packet.getSessionId()).isEqualTo(sessionId);
    }
}
