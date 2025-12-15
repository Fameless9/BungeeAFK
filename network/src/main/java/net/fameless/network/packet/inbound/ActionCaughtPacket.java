package net.fameless.network.packet.inbound;

import java.util.UUID;

public class ActionCaughtPacket {

    public UUID uuid;

    public ActionCaughtPacket(UUID uuid) {
        this.uuid = uuid;
    }
}
