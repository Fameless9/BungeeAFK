package net.fameless.network.packet.inbound;

import java.util.UUID;

public class ClickDetectedPacket {

    public UUID uuid;

    public ClickDetectedPacket(UUID uuid) {
        this.uuid = uuid;
    }
}
