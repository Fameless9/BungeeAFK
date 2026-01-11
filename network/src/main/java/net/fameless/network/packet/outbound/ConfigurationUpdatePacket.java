package net.fameless.network.packet.outbound;


public class ConfigurationUpdatePacket {

    public boolean reduceSimulationDistance;

    public ConfigurationUpdatePacket(boolean reduceSimulationDistance) {
        this.reduceSimulationDistance = reduceSimulationDistance;
    }

}
