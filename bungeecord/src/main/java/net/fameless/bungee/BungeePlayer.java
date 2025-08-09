package net.fameless.bungee;

import net.fameless.core.command.framework.CallerType;
import net.fameless.core.event.EventDispatcher;
import net.fameless.core.event.PlayerKickEvent;
import net.fameless.core.location.Location;
import net.fameless.core.messaging.RequestType;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BungeePlayer extends BAFKPlayer<ProxiedPlayer> {

    public static final List<BungeePlayer> BUNGEE_PLAYERS = new ArrayList<>();

    public BungeePlayer(@NotNull ProxiedPlayer player) {
        super(player.getUniqueId());
        this.name = player.getName();
        BUNGEE_PLAYERS.add(this);
    }

    public static @NotNull Optional<BungeePlayer> adapt(@NotNull String name) {
        return BUNGEE_PLAYERS.stream()
                .filter(bp -> bp.getName().equals(name))
                .findFirst();
    }

    public static @NotNull Optional<BungeePlayer> adapt(@NotNull UUID uuid) {
        return BUNGEE_PLAYERS.stream()
                .filter(bp -> bp.getUniqueId().equals(uuid))
                .findFirst();
    }

    public static @NotNull BungeePlayer adapt(@NotNull ProxiedPlayer player) {
        return BUNGEE_PLAYERS.stream()
                .filter(bp -> bp.getPlatformPlayer()
                        .map(p -> p.getUniqueId().equals(player.getUniqueId()))
                        .orElse(false))
                .findFirst()
                .orElseGet(() -> new BungeePlayer(player));
    }

    public static @NotNull Optional<BungeePlayer> adapt(@NotNull BAFKPlayer<?> player) {
        return player.getPlatformPlayer()
                .filter(ProxiedPlayer.class::isInstance)
                .map(ProxiedPlayer.class::cast)
                .map(BungeePlayer::adapt);
    }

    @Override
    public @NotNull CallerType callerType() {
        return CallerType.PLAYER;
    }

    @Override
    public @NotNull String getName() {
        if (this.name != null) return this.name;
        return getPlatformPlayer().map(ProxiedPlayer::getName).orElse("N/A");
    }

    @Override
    public @NotNull Audience getAudience() {
        return getPlatformPlayer()
                .map(BungeeUtil.BUNGEE_AUDIENCES::player)
                .orElseGet(Audience::empty);
    }

    @Override
    public Optional<ProxiedPlayer> getPlatformPlayer() {
        return Optional.ofNullable(BungeePlatform.get().getProxy().getPlayer(getUniqueId()));
    }

    @Override
    public boolean isOffline() {
        return getPlatformPlayer().map(p -> !p.isConnected()).orElse(true);
    }

    @Override
    public void connect(String serverName) {
        getPlatformPlayer().ifPresent(player ->
                player.connect(BungeePlatform.get().getProxy().getServerInfo(serverName)));
    }

    @Override
    public void kick(Component reason) {
        ProxiedPlayer player = getPlatformPlayer().orElse(null);
        if (player == null) return;

        PlayerKickEvent event = new PlayerKickEvent(this, reason);
        EventDispatcher.post(event);

        if (event.isCancelled()) {
            LOGGER.info("PlayerKickEvent was cancelled for player: {}", getName());
            return;
        }
        player.disconnect(BungeeComponentSerializer.get().serialize(event.getReason()));
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlatformPlayer().map(p -> p.hasPermission(permission)).orElse(false);
    }

    @Override
    public String getCurrentServerName() {
        ProxiedPlayer player = getPlatformPlayer().orElse(null);
        if (player == null) return "N/A";
        Server server = player.getServer();
        return server != null ? server.getInfo().getName() : "N/A";
    }

    @Override
    public void updateGameMode(@NotNull GameMode gameMode) {
        ProxiedPlayer player = getPlatformPlayer().orElse(null);
        if (player == null) {
            LOGGER.info("player is null, cannot set gamemode.");
            return;
        }
        byte[] data = (RequestType.GAMEMODE_CHANGE.getName() + ";" +
                this.getUniqueId() + ";" +
                gameMode.name()).getBytes();
        player.getServer().getInfo().sendData("bungee:bungeeafk", data);
    }

    @Override
    public void teleport(@NotNull Location location) {
        ProxiedPlayer player = getPlatformPlayer().orElse(null);
        if (player == null) {
            LOGGER.info("player is null, cannot teleport.");
            return;
        }
        byte[] data = (RequestType.TELEPORT_PLAYER.getName() + ";" +
                this.getUniqueId() + ";" +
                location.getWorldName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getYaw() + ";" +
                location.getPitch()
        ).getBytes();
        player.getServer().getInfo().sendData("bungee:bungeeafk", data);
    }

    @Override
    public void openEmptyInventory() {
        ProxiedPlayer player = getPlatformPlayer().orElse(null);
        if (player == null) {
            LOGGER.info("player is null, cannot set gamemode.");
            return;
        }
        byte[] data = (RequestType.OPEN_EMPTY_INVENTORY.getName() + ";" +
                this.getUniqueId() + ";").getBytes();

        player.getServer().getInfo().sendData("bungee:bungeeafk", data);
    }
}
