package net.fameless.network.packet.inbound;

import net.fameless.network.MessageType;
import net.fameless.network.ServerSoftware;
import net.fameless.network.packet.AbstractPacket;

public class HandshakePacket extends AbstractPacket {

    public ServerSoftware serverSoftware;
    public int minecraftServerPort;

    public HandshakePacket(ServerSoftware serverSoftware, int minecraftServerPort) {
        super(MessageType.HANDSHAKE);
        this.serverSoftware = serverSoftware;
        this.minecraftServerPort = minecraftServerPort;
    }
}
