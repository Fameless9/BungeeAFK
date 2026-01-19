package net.fameless.network.packet.inbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class ActionCaughtPacket extends AbstractPacket {

    public UUID uuid;

    public ActionCaughtPacket(UUID uuid) {
        super(MessageType.ACTION_CAUGHT);
        this.uuid = uuid;
    }
}
