package net.fameless.network.packet.outbound;

import java.util.UUID;

public class PlayerAfkPacket {

    public UUID uuid;

    public PlayerAfkPacket(UUID uuid) {
        this.uuid = uuid;
    }
}
