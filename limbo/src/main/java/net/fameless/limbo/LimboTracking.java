package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.*;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LimboTracking extends LimboPlugin implements Listener {

    private static LimboTracking instance;

    public static LimboTracking getInstance() {
        return instance;
    }

    private final EventLoopGroup group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    private final Object connectionAttemptLock = new Object();
    private final Set<UUID> initializedPlayers = new HashSet<>();

    private volatile Channel channel;
    private volatile boolean connecting = false;

    private Bootstrap bootstrap;
    private Config config;
    private boolean debugLogging = false;

    @Override
    public void onEnable() {
        instance = this;

        Limbo.getInstance().getEventsManager().registerEvents(this, this);
        config = new Config();

        this.debugLogging = config.getBoolean("debug-logging", false);
        Logger.info("Starting with debug-logging {}.", debugLogging ? "enabled" : "disabled");

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
        Logger.info("Attempting to establish connection to proxy plugin instance on: {}:{}", host, port);
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
                                    sendHandshake();

                                    this.channel.closeFuture().addListener(cf -> {
                                        synchronized (connectionAttemptLock) {
                                            this.channel = null;
                                            Limbo.getInstance().getScheduler().runTaskLater(this, () -> establishConnection(host, port), 20);
                                        }
                                    });

                                    Logger.info("Connection to proxy plugin instance established successfully");
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

    private void sendHandshake() {
        if (channel == null) {
            Logger.info("Cannot send 'handshake' packet as channel is null");
            return;
        }

        if (debugLogging) Logger.info("Sending a handshake packet to the proxy plugin");
        HandshakePacket packet = new HandshakePacket(ServerSoftware.LIMBO, getServer().getServerConnection().getServerSocket().getLocalPort());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.HANDSHAKE, packet));
    }

    private void sendActionCaught(@NotNull Player player) {
        if (channel == null) {
            Logger.info("Cannot send 'action caught' packet for {} as channel is null", player.getName());
            return;
        }
        if (!initializedPlayers.contains(player.getUniqueId())) {
            if (debugLogging)
                Logger.info("Cancelling 'action caught' packet for {} as player is not initialized", player.getName());
            return;
        }

        ActionCaughtPacket packet = new ActionCaughtPacket(player.getUniqueId());
        if (debugLogging) Logger.info("Sending an 'action caught' packet for {}", player.getName());
        packet.send(channel);
    }

    private void sendLocationChanged(@NotNull Player player, @NotNull Location to) {
        if (channel == null) {
            Logger.info("Cannot send 'location changed' packet for {} as channel is null", player.getName());
            return;
        }

        LocationChangedPacket packet = new LocationChangedPacket(
                player.getUniqueId(),
                to.getWorld().getName(),
                to.getX(),
                to.getY(),
                to.getZ(),
                to.getPitch(),
                to.getYaw()
        );
        if (debugLogging) Logger.info("Sending a 'location changed' packet for {} | Location={}", player.getName(), to);
        packet.send(channel);
    }

    private void sendGameModeChanged(@NotNull Player player, @NotNull GameMode gameMode) {
        if (channel == null) {
            Logger.info("Cannot send 'game mode changed' packet for {} as channel is null", player.getName());
            return;
        }

        GameModeChangedPacket packet = new GameModeChangedPacket(player.getUniqueId(), gameMode.name());
        if (debugLogging)
            Logger.info("Sending a 'game mode changed' packet for {} | GameMode={}", player.getName(), gameMode.name());
        packet.send(channel);
    }

    private void sendClickDetected(@NotNull Player player) {
        if (channel == null) {
            Logger.info("Cannot send 'click detected' packet for {} as channel is null", player.getName());
            return;
        }

        ClickDetectedPacket packet = new ClickDetectedPacket(player.getUniqueId());
        if (debugLogging) Logger.info("Sending a 'click detected' packet for {}", player.getName());
        packet.send(channel);
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (!event.getFrom().equals(event.getTo())) {
            if (debugLogging) Logger.info("Caught a move action from player {}", event.getPlayer().getName());
            sendActionCaught(event.getPlayer());
            sendLocationChanged(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler
    public void onChat(@NotNull PlayerChatEvent event) {
        if (debugLogging) Logger.info("Caught a chat action from player {}", event.getPlayer().getName());
        sendActionCaught(event.getPlayer());
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (debugLogging) Logger.info("Caught an interaction from player {}", event.getPlayer().getName());
        sendActionCaught(event.getPlayer());
        if (event.getAction().equals(PlayerInteractEvent.Action.PHYSICAL)) return;
        sendClickDetected(event.getPlayer());
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (config.getBoolean("spectator-by-default", true)) {
            if (debugLogging)
                Logger.info("Setting the game mode of {} to spectator as 'specator-by-default' is true", event.getPlayer().getName());
            player.setGamemode(GameMode.SPECTATOR);
            player.teleport(player.getLocation());
        }
        sendGameModeChanged(player, player.getGamemode());
        sendLocationChanged(player, player.getLocation());

        // A short delay is required before sending action caught packets, as a PlayerMoveEvent is sometimes thrown if a player joins
        // the server for the first time even though they didn't move
        Limbo.getInstance().getScheduler().runTaskLater(this, () -> {
            try {
                initializedPlayers.add(player.getUniqueId());
            } catch (Throwable ignore) {
            }
        }, 4L);
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        initializedPlayers.remove(event.getPlayer().getUniqueId());
    }
}
