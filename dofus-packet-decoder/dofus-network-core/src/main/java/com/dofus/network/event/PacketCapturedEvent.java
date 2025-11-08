package com.dofus.network.event;

import com.dofus.network.model.RawPacket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a packet is captured.
 *
 * Listeners can subscribe to this event to process packets.
 */
@Getter
public class PacketCapturedEvent extends ApplicationEvent {

    private final RawPacket packet;

    public PacketCapturedEvent(Object source, RawPacket packet) {
        super(source);
        this.packet = packet;
    }
}
