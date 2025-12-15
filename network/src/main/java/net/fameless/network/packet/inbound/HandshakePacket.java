package net.fameless.network.packet.inbound;

public class HandshakePacket {

    public int minecraftServerPort;

    public HandshakePacket(int minecraftServerPort) {
        this.minecraftServerPort = minecraftServerPort;
    }
}
