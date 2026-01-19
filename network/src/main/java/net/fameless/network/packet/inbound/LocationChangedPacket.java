package net.fameless.network.packet.inbound;

import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

import java.util.UUID;

public class LocationChangedPacket extends AbstractPacket {

    public UUID uuid;
    public String world;
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    public LocationChangedPacket(UUID uuid, String world, double x, double y, double z, float pitch, float yaw) {
        super(MessageType.LOCATION_CHANGED);
        this.uuid = uuid;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
}
