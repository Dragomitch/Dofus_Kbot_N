package com.dofus.network.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a raw network packet captured from the proxy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawPacket {

    /**
     * Raw packet data (bytes).
     */
    private byte[] data;

    /**
     * Direction of the packet.
     */
    private Direction direction;

    /**
     * Session ID (channel ID).
     */
    private String sessionId;

    /**
     * Timestamp when packet was captured.
     */
    private LocalDateTime timestamp;
}
