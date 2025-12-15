package net.fameless.core.network;

import io.netty.channel.Channel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelRegistry {

    private final Map<Channel, Integer> connections = new ConcurrentHashMap<>();

    public void register(Channel channel, int minecraftServerPort) {
        connections.put(channel, minecraftServerPort);
    }

    public int unregister(Channel channel) {
        return connections.remove(channel);
    }

    public int getPort(Channel channel) {
        return connections.get(channel);
    }

    public Set<Channel> channels() {
        return new HashSet<>(connections.keySet());
    }

}
