package net.fameless.tracking;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkUtil;
import net.fameless.network.packet.inbound.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class BungeeAFKTracking extends JavaPlugin implements Listener {

    private static BungeeAFKTracking instance;

    public static BungeeAFKTracking getInstance() {
        return instance;
    }

    private final EventLoopGroup group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    private Channel channel;
    private Bootstrap bootstrap;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4),
                                new LengthFieldPrepender(4),
                                new StringDecoder(StandardCharsets.UTF_8),
                                new StringEncoder(StandardCharsets.UTF_8),
                                new InboundChannelHandler()
                        );
                    }
                });

        establishConnection();
    }

    @Override
    public void onDisable() {
        group.shutdownGracefully();
    }

    public void establishConnection() {
        if (channel != null) return;
        bootstrap.connect(getConfig().getString("netty-host", "localhost"), getConfig().getInt("netty-port", 9000))
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        this.channel = future.channel();
                        sendHello();
                    } else {
                        Bukkit.getScheduler().runTaskLater(this, this::establishConnection, 20);
                    }
                });
    }

    private void sendHello() {
        HandshakePacket packet = new HandshakePacket(getServer().getPort());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.HANDSHAKE, packet));
    }

    private void sendActionCaught(@NotNull Player player) {
        ActionCaughtPacket packet = new ActionCaughtPacket(player.getUniqueId());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.ACTION_CAUGHT, packet));
    }

    private void sendLocationChanged(@NotNull Player player, @NotNull Location to) {
        LocationChangedPacket packet = new LocationChangedPacket(
                player.getUniqueId(),
                to.getWorld().getName(),
                to.getX(),
                to.getY(),
                to.getZ(),
                to.getPitch(),
                to.getYaw()
        );
        channel.writeAndFlush(NetworkUtil.msg(MessageType.LOCATION_CHANGED, packet));
    }

    private void sendGameModeChanged(@NotNull Player player, @NotNull GameMode gameMode) {
        GameModeChangedPacket packet = new GameModeChangedPacket(player.getUniqueId(), gameMode.name());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.GAMEMODE_CHANGED, packet));
    }

    private void sendClickDetected(@NotNull Player player) {
        ClickDetectedPacket packet = new ClickDetectedPacket(player.getUniqueId());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.CLICK_DETECTED, packet));
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (!event.getFrom().equals(event.getTo())) {
            sendActionCaught(event.getPlayer());
            sendLocationChanged(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        sendActionCaught(event.getPlayer());
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        sendActionCaught(event.getPlayer());
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        sendGameModeChanged(event.getPlayer(), event.getNewGameMode());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) return;
        sendClickDetected(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            sendGameModeChanged(event.getPlayer(), event.getPlayer().getGameMode());
            sendLocationChanged(event.getPlayer(), event.getPlayer().getLocation());
        }, 5L); // Delay to ensure player is fully initialized on proxy platform
    }
}
