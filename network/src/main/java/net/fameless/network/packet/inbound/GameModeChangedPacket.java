package net.fameless.network.packet.inbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class GameModeChangedPacket extends AbstractPacket {

    public UUID uuid;
    public String gameMode;

    public GameModeChangedPacket(UUID uuid, String gameMode) {
        super(MessageType.GAMEMODE_CHANGED);
        this.uuid = uuid;
        this.gameMode = gameMode;
    }
}
