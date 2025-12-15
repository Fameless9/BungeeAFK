package net.fameless.network.packet.inbound;

import java.util.UUID;

public class GameModeChangedPacket {

    public UUID uuid;
    public String gameMode;

    public GameModeChangedPacket(UUID uuid, String gameMode) {
        this.uuid = uuid;
        this.gameMode = gameMode;
    }
}
