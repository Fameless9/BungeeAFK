package net.fameless.core.network;

import io.netty.channel.Channel;
import net.fameless.core.config.Config;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.fameless.core.util.Location;
import net.fameless.network.packet.outbound.*;
import org.jetbrains.annotations.NotNull;

public class OutboundPacketSender {

    private static class Holder {
        public static OutboundPacketSender instance = new OutboundPacketSender();
    }

    public static OutboundPacketSender getInstance() {
        return Holder.instance;
    }

    private final ChannelRegistry registry = new ChannelRegistry();

    public void sendOpenEmptyInventoryPacket(@NotNull BAFKPlayer<?> player) {
        new OpenEmptyInventoryPacket(player.getUniqueId()).send(registry.channels());
    }

    public void sendTeleportPlayerPacket(@NotNull BAFKPlayer<?> player, @NotNull Location to) {
        new TeleportPlayerPacket(
                player.getUniqueId(),
                to.worldName(),
                to.x(),
                to.y(),
                to.z(),
                to.pitch(),
                to.yaw()
        ).send(registry.channels());
    }

    public void sendSetGameModePacket(@NotNull BAFKPlayer<?> player, @NotNull GameMode gameMode) {
        new SetGameModePacket(player.getUniqueId(), gameMode.name()).send(registry.channels());
    }

    public void sendPlayerAfkDetectedPacket(@NotNull BAFKPlayer<?> player) {
        new PlayerAfkPacket(player.getUniqueId()).send(registry.channels());
    }

    public void sendPlayerReturnPacket(@NotNull BAFKPlayer<?> player) {
        new PlayerReturnPacket(player.getUniqueId()).send(registry.channels());
    }

    public void sendConfigurationPacket(@NotNull Channel channel) {
        new ConfigurationUpdatePacket(Config.getInstance().getBoolean("reduce-simulation-distance", false)).send(channel);
    }

    public void sendConfigurationPacket() {
        new ConfigurationUpdatePacket(Config.getInstance().getBoolean("reduce-simulation-distance", false)).send(registry.channels());
    }

    public ChannelRegistry getRegistry() {
        return registry;
    }

}
