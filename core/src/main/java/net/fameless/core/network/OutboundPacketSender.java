package net.fameless.core.network;

import io.netty.channel.Channel;
import net.fameless.core.config.Config;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.fameless.core.util.Location;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkUtil;
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
        OpenEmptyInventoryPacket packet = new OpenEmptyInventoryPacket(player.getUniqueId());
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.OPEN_EMPTY_INVENTORY, packet)));
    }

    public void sendTeleportPlayerPacket(@NotNull BAFKPlayer<?> player, @NotNull Location to) {
        TeleportPlayerPacket packet = new TeleportPlayerPacket(
                player.getUniqueId(),
                to.worldName(),
                to.x(),
                to.y(),
                to.z(),
                to.pitch(),
                to.yaw()
        );
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.TELEPORT_PLAYER, packet)));
    }

    public void sendSetGameModePacket(@NotNull BAFKPlayer<?> player, @NotNull GameMode gameMode) {
        SetGameModePacket packet = new SetGameModePacket(player.getUniqueId(), gameMode.name());
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.SET_GAMEMODE, packet)));
    }

    public void sendPlayerAfkDetectedPacket(@NotNull BAFKPlayer<?> player) {
        PlayerAfkPacket packet = new PlayerAfkPacket(player.getUniqueId());
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.AFK_DETECTED, packet)));
    }

    public void sendPlayerReturnPacket(@NotNull BAFKPlayer<?> player) {
        PlayerReturnPacket packet = new PlayerReturnPacket(player.getUniqueId());
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.PLAYER_RETURN, packet)));
    }

    public void sendConfigurationPacket(@NotNull Channel channel) {
        var packet = new ConfigurationUpdatePacket(Config.getInstance().getBoolean("reduce-simulation-distance", false));
        channel.writeAndFlush(NetworkUtil.msg(MessageType.CONFIGURATION_UPDATE, packet));
    }

    public void sendConfigurationPacket() {
        var packet = new ConfigurationUpdatePacket(Config.getInstance().getBoolean("reduce-simulation-distance", false));
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.CONFIGURATION_UPDATE, packet)));
    }

    public ChannelRegistry getRegistry() {
        return registry;
    }

}
