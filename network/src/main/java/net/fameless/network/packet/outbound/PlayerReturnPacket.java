package net.fameless.network.packet.outbound;

import java.util.UUID;

public class PlayerReturnPacket {

    public UUID uuid;

    public PlayerReturnPacket(UUID uuid) {
        this.uuid = uuid;
    }
}
