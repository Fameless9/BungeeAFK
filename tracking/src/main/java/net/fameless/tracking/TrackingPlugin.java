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
import net.fameless.network.ServerSoftware;
import net.fameless.network.packet.inbound.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrackingPlugin extends JavaPlugin implements Listener {

    private static TrackingPlugin instance;

    public static TrackingPlugin getInstance() {
        return instance;
    }

    private final EventLoopGroup group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    private final Object connectionAttemptLock = new Object();
    private final Set<UUID> joinProcessed = new HashSet<>();
    private final Set<UUID> afkPlayers = new HashSet<>();

    private volatile Channel channel;
    private volatile boolean connecting = false;

    private Bootstrap bootstrap;

    private boolean debugLogging = false;
    private boolean paperAvailable;
    private boolean reduceSimulationDistance = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        this.debugLogging = getConfig().getBoolean("debug-logging", false);
        getLogger().info("Starting with debug-logging " + (debugLogging ? "enabled" : "disabled") + ".");

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

        String host = getConfig().getString("netty-host", "localhost");
        int port = getConfig().getInt("netty-port", 9000);
        getLogger().info("Attempting to establish connection to proxy plugin instance on: " + host + ":" + port);
        establishConnection(host, port);

        try {
            Class.forName("io.papermc.paper.InternalAPIBridge");
            this.paperAvailable = true;
            getLogger().info("Paper instance detected");
        } catch (ClassNotFoundException e) {
            getLogger().info("No paper instance detected. Some features are unavailable");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI instance detected. Registering placeholder...");
            new AFKPlaceholderExpansion().register();
        }
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

                                    channel.closeFuture().addListener((ChannelFutureListener) -> {
                                        synchronized (connectionAttemptLock) {
                                            this.channel = null;
                                            Bukkit.getScheduler().runTaskLater(this, () -> establishConnection(host, port), 20);
                                        }
                                    });

                                    getLogger().info("Connection to proxy plugin instance established successfully");
                                } else {
                                    Bukkit.getScheduler().runTaskLater(this, () -> establishConnection(host, port), 20);
                                }
                            } finally {
                                connecting = false;
                            }
                        }
                    });
        }
    }

    public boolean isAfk(@NotNull Player player) {
        return afkPlayers.contains(player.getUniqueId());
    }

    private void sendHello() {
        if (channel == null) {
            getLogger().warning("Cannot send 'handshake' packet as channel is null");
            return;
        }

        if (debugLogging) getLogger().info("Sending a handshake packet to the proxy plugin");
        HandshakePacket packet = new HandshakePacket(ServerSoftware.SPIGOT, getServer().getPort());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.HANDSHAKE, packet));
    }

    private void sendActionCaught(@NotNull Player player) {
        if (channel == null) {
            getLogger().warning("Cannot send 'action caught' packet for " + player.getName() + " as channel is null");
            return;
        }
        if (!joinProcessed.contains(player.getUniqueId())) {
            if (debugLogging) getLogger().info("Cancelling 'action caught' packet for {} as player is not initialized");
            return;
        }

        if (debugLogging) getLogger().info("Sending an 'action caught' packet for " + player.getName());
        ActionCaughtPacket packet = new ActionCaughtPacket(player.getUniqueId());
        channel.writeAndFlush(NetworkUtil.msg(MessageType.ACTION_CAUGHT, packet));
    }

    private void sendLocationChanged(@NotNull Player player, @NotNull Location to) {
        if (channel == null) {
            getLogger().warning("Cannot send 'location changed' packet for " + player.getName() + " as channel is null");
            return;
        }

        if (debugLogging)
            getLogger().info("Sending a 'location changed' packet for " + player.getName() + "Location=" + to);
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
        if (channel == null) {
            getLogger().warning("Cannot send 'game mode changed' packet for " + player.getName() + " as channel is null");
            return;
        }

        if (debugLogging)
            getLogger().info("Sending a 'game mode changed' packet for player " + player.getName() + " GameMode=" + gameMode.name());
        new GameModeChangedPacket(player.getUniqueId(), gameMode.name()).send(channel);
    }

    private void sendClickDetected(@NotNull Player player) {
        if (channel == null) {
            getLogger().warning("Cannot send 'click detected' packet for " + player.getName() + " as channel is null");
            return;
        }

        if (debugLogging) getLogger().info("Sending a 'click detected' packet for " + player.getName());
        new ClickDetectedPacket(player.getUniqueId()).send(channel);
    }

    public void onPlayerAfk(@NotNull Player player) {
        afkPlayers.add(player.getUniqueId());
        if (paperAvailable && reduceSimulationDistance) {
            player.setViewDistance(2);
            player.setSimulationDistance(2);
        }
    }

    public void onPlayerReturn(@NotNull Player player) {
        afkPlayers.remove(player.getUniqueId());
        if (paperAvailable) {
            player.setViewDistance(getServer().getViewDistance());
            player.setSimulationDistance(getServer().getSimulationDistance());
        }
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        if (debugLogging) getLogger().info("Caught a chat action from player " + event.getPlayer().getName());
        sendActionCaught(event.getPlayer());
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (!event.getFrom().equals(event.getTo())) {
            if (debugLogging) getLogger().info("Caught a move action from player " + event.getPlayer().getName());
            sendActionCaught(event.getPlayer());
            sendLocationChanged(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (debugLogging) getLogger().info("Caught an interaction from player " + event.getPlayer().getName());
        sendActionCaught(event.getPlayer());
        if (event.getAction().equals(Action.PHYSICAL)) return;
        sendClickDetected(event.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(@NotNull PlayerGameModeChangeEvent event) {
        if (debugLogging) getLogger().info("Caught a game mode change from player " + event.getPlayer().getName());
        sendGameModeChanged(event.getPlayer(), event.getNewGameMode());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sendGameModeChanged(player, player.getGameMode());
        sendLocationChanged(player, player.getLocation());
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                joinProcessed.add(player.getUniqueId());
            } catch (Throwable ignore) {
            }
        }, 4L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        joinProcessed.remove(event.getPlayer().getUniqueId());
    }

    public void setReduceSimulationDistance(boolean reduceSimulationDistance) {
        if (reduceSimulationDistance && !paperAvailable) {
            getLogger().warning("reduceSimulationDistance=true, but no Paper instance has been detected. This feature is only available on Paper servers!");
        }
        this.reduceSimulationDistance = reduceSimulationDistance;
    }
}
