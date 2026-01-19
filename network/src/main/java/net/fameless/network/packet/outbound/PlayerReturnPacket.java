package net.fameless.network.packet.outbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class PlayerReturnPacket extends AbstractPacket {

    public UUID uuid;

    public PlayerReturnPacket(UUID uuid) {
        super(MessageType.PLAYER_RETURN);
        this.uuid = uuid;
    }
}
