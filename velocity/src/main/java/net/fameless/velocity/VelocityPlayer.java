package net.fameless.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.fameless.api.event.EventDispatcher;
import net.fameless.api.event.PlayerKickEvent;
import net.fameless.core.adapter.APIAdapter;
import net.fameless.core.command.framework.CallerType;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.util.ServerPinger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityPlayer extends BAFKPlayer<Player> {

    private static final List<VelocityPlayer> VELOCITY_PLAYERS = new ArrayList<>();

    public VelocityPlayer(@NotNull Player player) {
        super(player.getUniqueId());
        this.name = player.getUsername();
        VELOCITY_PLAYERS.add(this);
    }

    public static @NotNull VelocityPlayer adapt(Player player) {
        return VELOCITY_PLAYERS.stream()
                .filter(vp -> vp.getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElseGet(() -> new VelocityPlayer(player));
    }

    public static @NotNull Optional<VelocityPlayer> adapt(UUID uuid) {
        return VELOCITY_PLAYERS.stream()
                .filter(vp -> vp.getUniqueId().equals(uuid))
                .findFirst();
    }

    public static @NotNull Optional<VelocityPlayer> adapt(String name) {
        return VELOCITY_PLAYERS.stream()
                .filter(vp -> vp.getName().equals(name))
                .findFirst();
    }

    @Override
    public CallerType callerType() {
        return CallerType.PLAYER;
    }

    @Override
    public String getName() {
        if (this.name != null) return this.name;
        return getPlatformPlayer().map(Player::getUsername).orElse("N/A");
    }

    @Override
    public Audience getAudience() {
        return getPlatformPlayer().map(Audience::audience).orElseGet(Audience::empty);
    }

    @Override
    public Optional<Player> getPlatformPlayer() {
        return VelocityPlatform.getProxy().getPlayer(getUniqueId());
    }

    @Override
    public boolean isOffline() {
        return getPlatformPlayer().map(p -> !p.isActive()).orElse(true);
    }

    @Override
    public CompletableFuture<Boolean> connect(String serverName) {
        RegisteredServer server = VelocityPlatform.getProxy().getServer(serverName).orElse(null);
        if (server == null) {
            logger.warn("Error connecting player '{}' to Server '{}' | Server is not registered", this.getName(), serverName);
            return CompletableFuture.completedFuture(false);
        }

        return ServerPinger.isOnline(server.getServerInfo().getAddress())
                .thenCompose(isOnline -> {
                    if (!isOnline) {
                        logger.warn("Error connecting player '{}' to Server '{}' | Server is offline", this.getName(), serverName);
                        return CompletableFuture.completedFuture(false);
                    }

                    return getPlatformPlayer().map(player -> {
                        player.createConnectionRequest(server).fireAndForget();
                        return CompletableFuture.completedFuture(true);
                    }).orElseGet(() -> CompletableFuture.completedFuture(false));
                });
    }

    @Override
    public void kick(Component reason) {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) return;

        PlayerKickEvent event = new PlayerKickEvent(APIAdapter.adapt(this), reason);
        EventDispatcher.post(event);

        if (event.isCancelled()) {
            logger.info("PlayerKickEvent was cancelled for player: {}", getName());
            return;
        }
        player.disconnect(event.getReason());
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlatformPlayer().map(p -> p.hasPermission(permission)).orElse(false);
    }

    @Override
    public @NotNull String getCurrentServerName() {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) return "N/A";
        return player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("N/A");
    }
}
