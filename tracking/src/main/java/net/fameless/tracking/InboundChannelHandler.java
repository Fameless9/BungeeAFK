package net.fameless.tracking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkMessage;
import net.fameless.network.packet.outbound.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Locale;

public class InboundChannelHandler extends SimpleChannelInboundHandler<String> {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause.getMessage() != null && cause.getMessage().contains("Connection reset")) {
            return;
        }

        TrackingPlugin.getInstance().getLogger().severe("Netty error in BungeeAFK-Tracking: " + cause.getMessage());
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        NetworkMessage message = gson.fromJson(msg, NetworkMessage.class);
        MessageType type = MessageType.valueOf(message.type);

        switch (type) {
            case OPEN_EMPTY_INVENTORY -> {
                OpenEmptyInventoryPacket packet = gson.fromJson(message.payload, OpenEmptyInventoryPacket.class);
                Player target = Bukkit.getPlayer(packet.uuid);
                if (target == null) return;
                Bukkit.getScheduler().runTask(TrackingPlugin.getInstance(), () -> target.openInventory(Bukkit.createInventory(null, 27, "")));
            }
            case TELEPORT_PLAYER -> {
                TeleportPlayerPacket packet = gson.fromJson(message.payload, TeleportPlayerPacket.class);
                Location location = new Location(Bukkit.getWorld(packet.world), packet.x, packet.y, packet.z, packet.pitch, packet.yaw);
                Player target = Bukkit.getPlayer(packet.uuid);
                if (target == null) return;
                Bukkit.getScheduler().runTask(TrackingPlugin.getInstance(), () -> target.teleport(location));
            }
            case SET_GAMEMODE -> {
                SetGameModePacket packet = gson.fromJson(message.payload, SetGameModePacket.class);
                GameMode gameMode = GameMode.valueOf(packet.gameMode.toUpperCase(Locale.US));
                Player target = Bukkit.getPlayer(packet.uuid);
                if (target == null) return;
                Bukkit.getScheduler().runTask(TrackingPlugin.getInstance(), () -> target.setGameMode(gameMode));
            }
            case AFK_DETECTED -> {
                PlayerAfkPacket packet = gson.fromJson(message.payload, PlayerAfkPacket.class);
                Player player = Bukkit.getPlayer(packet.uuid);
                if (player == null) break;
                TrackingPlugin.getInstance().onPlayerAfk(player);
            }
            case PLAYER_RETURN -> {
                PlayerReturnPacket packet = gson.fromJson(message.payload, PlayerReturnPacket.class);
                Player player = Bukkit.getPlayer(packet.uuid);
                if (player == null) break;
                TrackingPlugin.getInstance().onPlayerReturn(player);
            }
            case CONFIGURATION_UPDATE -> {
                ConfigurationUpdatePacket packet = gson.fromJson(message.payload, ConfigurationUpdatePacket.class);
                TrackingPlugin.getInstance().setReduceSimulationDistance(packet.reduceSimulationDistance);
            }
        }
    }
}
