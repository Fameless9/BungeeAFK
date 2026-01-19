package net.fameless.network.packet.outbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class PlayerAfkPacket extends AbstractPacket {

    public UUID uuid;

    public PlayerAfkPacket(UUID uuid) {
        super(MessageType.AFK_DETECTED);
        this.uuid = uuid;
    }
}
