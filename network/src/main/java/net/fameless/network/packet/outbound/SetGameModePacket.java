package net.fameless.network.packet.outbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class SetGameModePacket extends AbstractPacket {

    public UUID uuid;
    public String gameMode;

    public SetGameModePacket(UUID uuid, String gameMode) {
        super(MessageType.SET_GAMEMODE);
        this.uuid = uuid;
        this.gameMode = gameMode;
    }
}
