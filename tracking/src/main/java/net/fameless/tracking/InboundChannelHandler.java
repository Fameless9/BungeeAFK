package net.fameless.tracking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkMessage;
import net.fameless.network.packet.outbound.OpenEmptyInventoryPacket;
import net.fameless.network.packet.outbound.SetGameModePacket;
import net.fameless.network.packet.outbound.TeleportPlayerPacket;
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
    public void channelInactive(ChannelHandlerContext ctx) {
        BungeeAFKTracking.getInstance().setChannel(null);
        BungeeAFKTracking.getInstance().establishConnection();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        BungeeAFKTracking.getInstance().getLogger().info("Connection to proxy plugin instance established");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause.getMessage() != null && cause.getMessage().contains("Connection reset")) {
            return;
        }

        BungeeAFKTracking.getInstance().getLogger().warning("Netty error in BungeeAFK-Tracking:");
        cause.printStackTrace();
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
                Bukkit.getScheduler().runTask(BungeeAFKTracking.getInstance(), () -> target.openInventory(Bukkit.createInventory(null, 27, "")));
            }
            case TELEPORT_PLAYER -> {
                TeleportPlayerPacket packet = gson.fromJson(message.payload, TeleportPlayerPacket.class);
                Location location = new Location(Bukkit.getWorld(packet.world), packet.x, packet.y, packet.z, packet.pitch, packet.yaw);
                Player target = Bukkit.getPlayer(packet.uuid);
                if (target == null) return;
                Bukkit.getScheduler().runTask(BungeeAFKTracking.getInstance(), () -> target.teleport(location));
            }
            case SET_GAMEMODE -> {
                SetGameModePacket packet = gson.fromJson(message.payload, SetGameModePacket.class);
                GameMode gameMode = GameMode.valueOf(packet.gameMode.toUpperCase(Locale.US));
                Player target = Bukkit.getPlayer(packet.uuid);
                if (target == null) return;
                Bukkit.getScheduler().runTask(BungeeAFKTracking.getInstance(), () -> target.setGameMode(gameMode));
            }
        }
    }
}
