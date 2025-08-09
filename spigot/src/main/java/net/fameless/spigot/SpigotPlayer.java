package net.fameless.spigot;

import net.fameless.core.command.framework.CallerType;
import net.fameless.core.event.EventDispatcher;
import net.fameless.core.event.PlayerKickEvent;
import net.fameless.core.location.Location;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpigotPlayer extends BAFKPlayer<Player> {

    private static final List<SpigotPlayer> SPIGOT_PLAYERS = new ArrayList<>();

    public SpigotPlayer(@NotNull Player player) {
        super(player.getUniqueId());
        this.name = player.getName();
        SPIGOT_PLAYERS.add(this);
    }

    public static @NotNull SpigotPlayer adapt(Player player) {
        for (SpigotPlayer spigotPlayer : SPIGOT_PLAYERS) {
            Optional<Player> platformPlayerOptional = spigotPlayer.getPlatformPlayer();
            if (platformPlayerOptional.isEmpty()) continue;

            if (spigotPlayer.getPlatformPlayer().get().getUniqueId().equals(player.getUniqueId())) {
                return spigotPlayer;
            }
        }
        return new SpigotPlayer(player);
    }

    public static @NotNull Optional<SpigotPlayer> adapt(UUID uuid) {
        for (SpigotPlayer spigotPlayer : SPIGOT_PLAYERS) {
            if (spigotPlayer.getUniqueId().equals(uuid)) {
                return Optional.of(spigotPlayer);
            }
        }
        return Optional.empty();
    }

    public static Optional<SpigotPlayer> adapt(String name) {
        for (SpigotPlayer spigotPlayer : SPIGOT_PLAYERS) {
            if (spigotPlayer.getName().equals(name)) {
                return Optional.of(spigotPlayer);
            }
        }
        return Optional.empty();
    }

    @Override
    public CallerType callerType() {
        return CallerType.PLAYER;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            Optional<Player> platformPlayer = getPlatformPlayer();
            if (platformPlayer.isEmpty()) {
                return "N/A";
            }
            this.name = platformPlayer.get().getName();
        }
        return this.name;
    }

    @Override
    public Audience getAudience() {
        Optional<Player> platformPlayer = getPlatformPlayer();
        return platformPlayer.map(SpigotUtil.BUKKIT_AUDIENCES::player).orElseGet(Audience::empty);
    }

    @Override
    public Optional<Player> getPlatformPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(getUniqueId()));
    }

    @Override
    public boolean isOffline() {
        return getPlatformPlayer().isEmpty();
    }

    @Override
    public void connect(String serverName) {
        // Not needed for SpigotPlatform
    }

    @Override
    public void kick(Component reason) {
        Bukkit.getScheduler().runTask(SpigotPlatform.get(), () -> {
            Player player = getPlatformPlayer().orElse(null);
            if (player == null) return;

            PlayerKickEvent event = new PlayerKickEvent(this, reason);
            EventDispatcher.post(event);

            if (event.isCancelled()) {
                LOGGER.info("PlayerKickEvent was cancelled for player: {}", getName());
                return;
            }

            player.kickPlayer(LegacyComponentSerializer.legacySection().serialize(event.getReason()));
        });
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlatformPlayer().isPresent() && getPlatformPlayer().get().hasPermission(permission);
    }

    @Override
    public String getCurrentServerName() {
        return ""; // Not needed for SpigotPlatform
    }

    @Override
    public @Nullable Location getLocation() {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) {
            return null;
        }
        return SpigotLocationAdapter.adapt(player.getLocation());
    }

    @Override
    public void updateGameMode(GameMode gameMode) {
        Bukkit.getScheduler().runTask(SpigotPlatform.get(), () -> getPlatformPlayer().ifPresent(player -> player.setGameMode(org.bukkit.GameMode.valueOf(gameMode.name()))));
    }

    @Override
    public void teleport(net.fameless.core.location.Location location) {
        Bukkit.getScheduler().runTask(SpigotPlatform.get(), () -> getPlatformPlayer().ifPresent(player -> player.teleport(SpigotLocationAdapter.adapt(location))));

    }

    @Override
    public void openEmptyInventory() {
        getPlatformPlayer().ifPresent(player -> player.openInventory(Bukkit.createInventory(null, 27, "")));
    }

    @Override
    public GameMode getGameMode() {
        Player player = getPlatformPlayer().orElse(null);
        if (player == null) {
            return GameMode.SURVIVAL;
        }
        return GameMode.valueOf(player.getGameMode().name());
    }

}
