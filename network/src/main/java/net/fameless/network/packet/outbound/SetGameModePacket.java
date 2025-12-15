package net.fameless.network.packet.outbound;

import java.util.UUID;

public class SetGameModePacket {

    public UUID uuid;
    public String gameMode;

    public SetGameModePacket(UUID uuid, String gameMode) {
        this.uuid = uuid;
        this.gameMode = gameMode;
    }
}
