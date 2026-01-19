package net.fameless.network.packet.outbound;


import net.fameless.network.MessageType;
import net.fameless.network.packet.AbstractPacket;

public class ConfigurationUpdatePacket extends AbstractPacket {

    public boolean reduceSimulationDistance;

    public ConfigurationUpdatePacket(boolean reduceSimulationDistance) {
        super(MessageType.CONFIGURATION_UPDATE);
        this.reduceSimulationDistance = reduceSimulationDistance;
    }

}
