package net.fameless.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.fameless.api.event.EventDispatcher;
import net.fameless.api.event.PlayerKickEvent;
import net.fameless.core.adapter.APIAdapter;
import net.fameless.core.command.framework.CallerType;
import net.fameless.core.location.Location;
import net.fameless.core.messaging.RequestType;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.fameless.core.util.ServerPinger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
            LOGGER.warn("Error connecting player '{}' to Server '{}' | Server is not registered", this.getName(), serverName);
            return CompletableFuture.completedFuture(false);
        }

        return ServerPinger.isOnline(server.getServerInfo().getAddress())
                .thenCompose(isOnline -> {
                    if (!isOnline) {
                        LOGGER.warn("Error connecting player '{}' to Server '{}' | Server is offline", this.getName(), serverName);
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
            LOGGER.info("PlayerKickEvent was cancelled for player: {}", getName());
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

    @Override
    public void updateGameMode(GameMode gameMode) {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) {
            LOGGER.info("player is null, cannot set gamemode.");
            return;
        }
        ServerConnection connection = player.getCurrentServer().orElse(null);
        if (connection == null) {
            LOGGER.info("player is not connected to a server, cannot set gamemode.");
            return;
        }
        ChannelIdentifier identifier = MinecraftChannelIdentifier.create("bungee", "bungeeafk");
        connection.sendPluginMessage(identifier,
                (RequestType.SET_GAMEMODE.getName() + ";" +
                        this.getUniqueId() + ";" +
                        gameMode.name()
                ).getBytes());
    }

    @Override
    public void teleport(Location location) {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) {
            LOGGER.info("player is null, cannot teleport.");
            return;
        }
        ServerConnection connection = player.getCurrentServer().orElse(null);
        if (connection == null) {
            LOGGER.info("player is not connected to a server, cannot teleport.");
            return;
        }
        ChannelIdentifier identifier = MinecraftChannelIdentifier.create("bungee", "bungeeafk");
        connection.sendPluginMessage(identifier,
                (RequestType.TELEPORT_PLAYER.getName() + ";" +
                        this.getUniqueId() + ";" +
                        location.getWorldName() + ";" +
                        location.getX() + ";" +
                        location.getY() + ";" +
                        location.getZ() + ";" +
                        location.getYaw() + ";" +
                        location.getPitch()
                ).getBytes());
    }

    @Override
    public void openEmptyInventory() {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) {
            LOGGER.info("player is null, cannot open inventory.");
            return;
        }
        ServerConnection connection = player.getCurrentServer().orElse(null);
        if (connection == null) {
            LOGGER.info("player is not connected to a server, cannot open inventory.");
            return;
        }
        ChannelIdentifier identifier = MinecraftChannelIdentifier.create("bungee", "bungeeafk");
        connection.sendPluginMessage(identifier, (RequestType.OPEN_EMPTY_INVENTORY.getName() + ";" + this.getUniqueId()).getBytes());
    }
}
