package net.fameless.limbo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.GameMode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkMessage;
import net.fameless.network.packet.outbound.OpenEmptyInventoryPacket;
import net.fameless.network.packet.outbound.SetGameModePacket;
import net.fameless.network.packet.outbound.TeleportPlayerPacket;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class InboundChannelHandler extends SimpleChannelInboundHandler<String> {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, @NotNull Throwable cause) {
        if (cause.getMessage() != null && cause.getMessage().contains("Connection reset")) {
            return;
        }
        ctx.close();
        Limbo.getInstance().getConsole().sendMessage("[BungeeAFK] [Severe] Netty error in BungeeAFK-Tracking: " + cause.getMessage());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        NetworkMessage message = gson.fromJson(msg, NetworkMessage.class);
        MessageType type = MessageType.valueOf(message.type);

        switch (type) {
            case OPEN_EMPTY_INVENTORY -> {
                OpenEmptyInventoryPacket packet = gson.fromJson(message.payload, OpenEmptyInventoryPacket.class);
                Player target = Limbo.getInstance().getPlayer(packet.uuid);
                if (target == null) return;
                Limbo.getInstance().getScheduler().runTask(LimboTracking.getInstance(),
                        () -> target.openInventory(Limbo.getInstance().createInventory(Component.empty(), 27, null)));
            }
            case TELEPORT_PLAYER -> {
                TeleportPlayerPacket packet = gson.fromJson(message.payload, TeleportPlayerPacket.class);
                Location location = new Location(Limbo.getInstance().getWorld(packet.world), packet.x, packet.y, packet.z, packet.pitch, packet.yaw);
                Player target = Limbo.getInstance().getPlayer(packet.uuid);
                if (target == null) return;
                Limbo.getInstance().getScheduler().runTask(LimboTracking.getInstance(), () -> target.teleport(location));
            }
            case SET_GAMEMODE -> {
                SetGameModePacket packet = gson.fromJson(message.payload, SetGameModePacket.class);
                GameMode gameMode = GameMode.valueOf(packet.gameMode.toUpperCase(Locale.US));
                Player target = Limbo.getInstance().getPlayer(packet.uuid);
                if (target == null) return;
                Limbo.getInstance().getScheduler().runTask(LimboTracking.getInstance(), () -> target.setGamemode(gameMode));
            }
        }
    }
}
