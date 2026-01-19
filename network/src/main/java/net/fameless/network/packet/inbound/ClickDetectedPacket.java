package net.fameless.network.packet.inbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class ClickDetectedPacket extends AbstractPacket {

    public UUID uuid;

    public ClickDetectedPacket(UUID uuid) {
        super(MessageType.CLICK_DETECTED);
        this.uuid = uuid;
    }

}
