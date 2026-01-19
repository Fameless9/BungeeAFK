package net.fameless.network.packet.outbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class OpenEmptyInventoryPacket extends AbstractPacket {

    public UUID uuid;

    public OpenEmptyInventoryPacket(UUID uuid) {
        super(MessageType.OPEN_EMPTY_INVENTORY);
        this.uuid = uuid;
    }
}
