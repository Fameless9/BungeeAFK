package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerChatEvent;
import com.loohp.limbo.events.player.PlayerInteractEvent;
import com.loohp.limbo.events.player.PlayerJoinEvent;
import com.loohp.limbo.events.player.PlayerMoveEvent;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.plugins.LimboPlugin;
import com.loohp.limbo.utils.GameMode;
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
import net.fameless.network.ServerSoftware;
import net.fameless.network.packet.inbound.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LimboTracking extends LimboPlugin implements Listener {

    private static LimboTracking instance;

    public static LimboTracking getInstance() {
        return instance;
    }

    private final EventLoopGroup group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    private volatile Channel channel;
    private Bootstrap bootstrap;
    private Config config;
    private final Object connectionAttemptLock = new Object();
    private volatile boolean connecting = false;

    @Override
    public void onEnable() {
        instance = this;

        Limbo.getInstance().getEventsManager().registerEvents(this, this);
        config = new Config();

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

        String host = config.getString("netty-host", "localhost");
        int port = config.getInt("netty-port", 9000);
        Limbo.getInstance().getConsole().sendMessage("[BungeeAFK] Attempting to establish connection to proxy plugin instance on: " + host + ":" + port);
        establishConnection(host, port);

        new GameModeTracker(this::sendGameModeChanged);
    }

    @Override
    public void onDisable() {
        group.shutdownGracefully();
    }

    public void establishConnection(String host, int port) {
        synchronized (connectionAttemptLock) {
            if (channel != null || connecting) return;
            connecting = true;
            bootstrap.connect(host, port)
                    .addListener((ChannelFutureListener) future -> {
                        synchronized (connectionAttemptLock) {
                            try {
                                if (future.isSuccess()) {
                                    this.channel = future.channel();
                                    sendHello();

                                    this.channel.closeFuture().addListener(cf -> {
                                        synchronized (connectionAttemptLock) {
                                            this.channel = null;
                                            Limbo.getInstance().getScheduler().runTaskLater(this, () -> establishConnection(host, port), 20);
                                        }
                                    });

                                    Limbo.getInstance().getConsole().sendMessage("[BungeeAFK] Connection to proxy plugin instance established successfully");
                                } else {
                                    Limbo.getInstance().getScheduler().runTaskLater(this, () -> establishConnection(host, port), 20);
                                }
                            } finally {
                                connecting = false;
                            }
                        }
                    });
        }
    }

    private void sendHello() {
        if (channel == null) return;
        HandshakePacket packet = new HandshakePacket(ServerSoftware.LIMBO, getServer().getServerConnection().getServerSocket().getLocalPort());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.HANDSHAKE, packet));
    }

    private void sendActionCaught(@NotNull Player player) {
        if (channel == null) return;
        ActionCaughtPacket packet = new ActionCaughtPacket(player.getUniqueId());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.ACTION_CAUGHT, packet));
    }

    private void sendLocationChanged(@NotNull Player player, @NotNull Location to) {
        if (channel == null) return;
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

    private void sendGameModeChanged(@NotNull UUID uuid, @NotNull GameMode gameMode) {
        if (channel == null) return;
        GameModeChangedPacket packet = new GameModeChangedPacket(uuid, gameMode.name());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.GAMEMODE_CHANGED, packet));
    }

    private void sendClickDetected(@NotNull Player player) {
        if (channel == null) return;
        ClickDetectedPacket packet = new ClickDetectedPacket(player.getUniqueId());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.CLICK_DETECTED, packet));
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
    public void onChat(@NotNull PlayerChatEvent event) {
        sendActionCaught(event.getPlayer());
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        sendActionCaught(event.getPlayer());
        if (event.getAction().equals(PlayerInteractEvent.Action.PHYSICAL)) return;
        sendClickDetected(event.getPlayer());
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (config.getBoolean("spectator-by-default", true)) event.getPlayer().setGamemode(GameMode.SPECTATOR);
        Limbo.getInstance().getScheduler().runTaskLater(this, () -> {
            sendLocationChanged(event.getPlayer(), event.getPlayer().getLocation());
        }, 5L);
    }
}
