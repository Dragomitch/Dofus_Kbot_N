package com.dofus.network.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RawPacket model.
 */
class RawPacketTest {

    @Test
    void testBuilderPattern() {
        // Given
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        Direction direction = Direction.OUTBOUND;
        String sessionId = "session-123";
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        RawPacket packet = RawPacket.builder()
                .data(data)
                .direction(direction)
                .sessionId(sessionId)
                .timestamp(timestamp)
                .build();

        // Then
        assertThat(packet.getData()).isEqualTo(data);
        assertThat(packet.getDirection()).isEqualTo(direction);
        assertThat(packet.getSessionId()).isEqualTo(sessionId);
        assertThat(packet.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testNoArgsConstructor() {
        // When
        RawPacket packet = new RawPacket();

        // Then
        assertThat(packet).isNotNull();
        assertThat(packet.getData()).isNull();
        assertThat(packet.getDirection()).isNull();
        assertThat(packet.getSessionId()).isNull();
        assertThat(packet.getTimestamp()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        byte[] data = new byte[]{(byte) 0xFF};
        Direction direction = Direction.INBOUND;
        String sessionId = "test";
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        RawPacket packet = new RawPacket(data, direction, sessionId, timestamp);

        // Then
        assertThat(packet.getData()).isEqualTo(data);
        assertThat(packet.getDirection()).isEqualTo(direction);
        assertThat(packet.getSessionId()).isEqualTo(sessionId);
        assertThat(packet.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testSettersAndGetters() {
        // Given
        RawPacket packet = new RawPacket();
        byte[] data = new byte[]{0x0A, 0x0B};
        Direction direction = Direction.OUTBOUND;
        String sessionId = "sess-456";
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        packet.setData(data);
        packet.setDirection(direction);
        packet.setSessionId(sessionId);
        packet.setTimestamp(timestamp);

        // Then
        assertThat(packet.getData()).isEqualTo(data);
        assertThat(packet.getDirection()).isEqualTo(direction);
        assertThat(packet.getSessionId()).isEqualTo(sessionId);
        assertThat(packet.getTimestamp()).isEqualTo(timestamp);
    }
}
