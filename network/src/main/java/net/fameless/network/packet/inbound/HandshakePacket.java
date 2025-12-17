package net.fameless.network.packet.inbound;

import net.fameless.network.ServerSoftware;

public class HandshakePacket {

    public ServerSoftware serverSoftware;
    public int minecraftServerPort;

    public HandshakePacket(ServerSoftware serverSoftware, int minecraftServerPort) {
        this.serverSoftware = serverSoftware;
        this.minecraftServerPort = minecraftServerPort;
    }
}
