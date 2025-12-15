package net.fameless.network.packet.outbound;

import java.util.UUID;

public class OpenEmptyInventoryPacket {

    public UUID uuid;

    public OpenEmptyInventoryPacket(UUID uuid) {
        this.uuid = uuid;
    }
}
