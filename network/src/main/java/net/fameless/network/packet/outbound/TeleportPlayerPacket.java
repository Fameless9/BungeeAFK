package net.fameless.network.packet.outbound;

import java.util.UUID;

public class TeleportPlayerPacket {

    public UUID uuid;
    public String world;
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    public TeleportPlayerPacket(UUID uuid, String world, double x, double y, double z, float pitch, float yaw) {
        this.uuid = uuid;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
}
