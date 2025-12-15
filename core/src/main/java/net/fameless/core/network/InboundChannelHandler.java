package net.fameless.core.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.fameless.core.BungeeAFK;
import net.fameless.core.location.Location;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkMessage;
import net.fameless.network.packet.inbound.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class InboundChannelHandler extends SimpleChannelInboundHandler<String> {

    private final Logger logger = LoggerFactory.getLogger("BungeeAFK/Network");
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Override
    public void channelInactive(@NonNull ChannelHandlerContext ctx) {
        int port = OutboundPacketSender.getInstance().getRegistry().unregister(ctx.channel());
        logger.info("Netty channel inactive (Port={})", port);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String msg = cause.getMessage();
        if (msg != null && msg.contains("Connection reset")) return;

        logger.error("Unexpected Netty exception", cause);
        ctx.close();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        NetworkMessage message = gson.fromJson(msg, NetworkMessage.class);
        MessageType type = MessageType.valueOf(message.type);

        switch (type) {
            case HANDSHAKE -> {
                HandshakePacket packet = gson.fromJson(message.payload, HandshakePacket.class);
                int port = packet.minecraftServerPort;
                OutboundPacketSender.getInstance().getRegistry().register(ctx.channel(), port);
                logger.info("Netty channel: proxy ↔ tracking plugin established (Port={})", port);
            }
            case ACTION_CAUGHT -> {
                ActionCaughtPacket packet = gson.fromJson(message.payload, ActionCaughtPacket.class);
                BAFKPlayer.of(packet.uuid).ifPresentOrElse(BAFKPlayer::setActive,
                        () -> logger.warn("Received action caught packet but player does not exist"));
            }
            case LOCATION_CHANGED -> {
                LocationChangedPacket packet = gson.fromJson(message.payload, LocationChangedPacket.class);
                Location location = new Location(packet.world, packet.x, packet.y, packet.z, packet.pitch, packet.yaw);
                BAFKPlayer.of(packet.uuid).ifPresentOrElse(player -> player.setLocation(location),
                        () -> logger.warn("Received location change packet but player does not exist"));
            }
            case CLICK_DETECTED -> {
                ClickDetectedPacket packet = gson.fromJson(message.payload, ClickDetectedPacket.class);
                BAFKPlayer.of(packet.uuid).ifPresentOrElse(player -> {
                    player.setActive();
                    BungeeAFK.getAutoClickerDetector().registerClick(player);
                }, () -> logger.warn("Received click packet but player does not exist"));
            }
            case GAMEMODE_CHANGED -> {
                GameModeChangedPacket packet = gson.fromJson(message.payload, GameModeChangedPacket.class);
                BAFKPlayer.of(packet.uuid).ifPresentOrElse(player -> player.setGameMode(GameMode.valueOf(packet.gameMode.toUpperCase(Locale.US))), () -> logger.warn("Received game mode change packet but player does not exist"));
            }
        }
    }
}
