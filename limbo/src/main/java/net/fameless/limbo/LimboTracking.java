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
import net.fameless.network.packet.inbound.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;

public class LimboTracking extends LimboPlugin implements Listener {

    private static LimboTracking instance;

    public static LimboTracking getInstance() {
        return instance;
    }

    private final EventLoopGroup group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    private Channel channel;
    private Bootstrap bootstrap;
    private Config config;

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

        establishConnection();

        new GameModeTracker(this::sendGameModeChanged);
    }

    @Override
    public void onDisable() {
        group.shutdownGracefully();
    }

    public void establishConnection() {
        if (channel != null) return;
        bootstrap.connect(config.getString("netty-host", "localhost"), config.getInt("netty-port", 9000))
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        this.channel = future.channel();
                        sendHello();
                    } else {
                        Limbo.getInstance().getScheduler().runTaskLater(this, this::establishConnection, 20);
                    }
                });
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    private void sendHello() {
        HandshakePacket packet = new HandshakePacket(getServer().getServerConnection().getServerSocket().getLocalPort());
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
    }

    @EventHandler
    public void onPlayerInteract(@NonNull PlayerInteractEvent event) {
        if (event.getAction().equals(PlayerInteractEvent.Action.PHYSICAL)) return;
        sendClickDetected(event.getPlayer());
    }

    @EventHandler
    public void onJoin(@NonNull PlayerJoinEvent event) {
        if (config.getBoolean("spectator-by-default", true)) event.getPlayer().setGamemode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Limbo.getInstance().getScheduler().runTaskLater(this, () -> {
            sendGameModeChanged(event.getPlayer(), event.getPlayer().getGamemode());
            sendLocationChanged(event.getPlayer(), event.getPlayer().getLocation());
        }, 5L); // Delay to ensure player is fully initialized on proxy platform
    }


}
