package net.fameless.core.network;

import net.fameless.core.location.Location;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkUtil;
import net.fameless.network.packet.outbound.OpenEmptyInventoryPacket;
import net.fameless.network.packet.outbound.SetGameModePacket;
import net.fameless.network.packet.outbound.TeleportPlayerPacket;
import org.jspecify.annotations.NonNull;

public class OutboundPacketSender {

    private static class Holder {
        public static OutboundPacketSender instance = new OutboundPacketSender();
    }

    public static OutboundPacketSender getInstance() {
        return Holder.instance;
    }

    private final ChannelRegistry registry = new ChannelRegistry();

    public void sendOpenEmptyInventoryPacket(@NonNull BAFKPlayer<?> player) {
        OpenEmptyInventoryPacket packet = new OpenEmptyInventoryPacket(player.getUniqueId());
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.OPEN_EMPTY_INVENTORY, packet)));
    }

    public void sendTeleportPlayerPacket(@NonNull BAFKPlayer<?> player, @NonNull Location to) {
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

    public void sendSetGameModePacket(@NonNull BAFKPlayer<?> player, @NonNull GameMode gameMode) {
        SetGameModePacket packet = new SetGameModePacket(player.getUniqueId(), gameMode.name());
        registry.channels().forEach(channel -> channel.writeAndFlush(NetworkUtil.msg(MessageType.SET_GAMEMODE, packet)));
    }

    public ChannelRegistry getRegistry() {
        return registry;
    }

}
