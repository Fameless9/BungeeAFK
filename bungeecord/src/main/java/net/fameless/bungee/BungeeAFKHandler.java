package net.fameless.bungee;

import net.fameless.core.BungeeAFK;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.messaging.RequestType;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BungeeAFKHandler extends AFKHandler implements Listener {

    @Override
    public void onInit() {
        BungeePlatform.proxyServer().registerChannel("bungee:bungeeafk");
        BungeePlatform.proxyServer().getPluginManager().registerListener(BungeePlatform.get(), this);
    }

    @EventHandler
    public void onPostLogin(@NotNull PostLoginEvent event) {
        BungeePlayer bungeePlayer = BungeePlayer.adapt(event.getPlayer());
        awaitConnectionAndHandleJoin(bungeePlayer, 0);
    }

    private void awaitConnectionAndHandleJoin(BungeePlayer bungeePlayer, int attempt) {
        final int maxAttempts = 50;
        BungeePlatform.get().getProxy().getScheduler().schedule(BungeePlatform.get(), () -> {
            Optional<ProxiedPlayer> playerOpt = bungeePlayer.getPlatformPlayer();
            if (playerOpt.isPresent() && playerOpt.get().getServer() != null) {
                bungeePlayer.setActive();
            } else if (attempt < maxAttempts) {
                awaitConnectionAndHandleJoin(bungeePlayer, attempt + 1);
            } else {
                LOGGER.error("Timeout while waiting for player {} to have a valid server connection. Previous states cannot be reverted.", bungeePlayer.getUniqueId());
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    @EventHandler
    public void onPluginMessage(@NotNull PluginMessageEvent event) {
        if (!event.getTag().equals("bungee:bungeeafk")) return;
        String[] parts = new String(event.getData()).split(";");
        RequestType type = RequestType.fromString(parts[0]);

        try {
            switch (type) {
                case ACTION_CAUGHT -> {
                    if (parts.length != 2) return;
                    BungeePlayer.adapt(UUID.fromString(parts[1])).ifPresent(BAFKPlayer::setActive);
                }
                case GAMEMODE_CHANGE -> handleGameModeChange(parts);
                case LOCATION_CHANGE -> handleLocationChange(parts);
                case CLICK -> {
                    if (parts.length != 2) return;
                    BungeePlayer.adapt(UUID.fromString(parts[1])).ifPresent(bungeePlayer -> {
                        bungeePlayer.setActive();
                        BungeeAFK.getAutoClickerDetector().registerClick(bungeePlayer);
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("Invalid data received: {} stacktrace: {}", Arrays.toString(parts), e.getMessage());
        }
    }

    private void handleGameModeChange(String @NotNull [] parts) {
        if (parts.length < 3) return;
        BungeePlayer.adapt(UUID.fromString(parts[1])).ifPresent(bungeePlayer -> {
            try {
                GameMode gameMode = GameMode.valueOf(parts[2].toUpperCase(Locale.ROOT));
                bungeePlayer.setGameMode(gameMode);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid game mode enum: {}", parts[2], e);
            }
        });
    }

    private void handleLocationChange(String @NotNull [] parts) {
        if (parts.length < 8) return;
        BungeePlayer.adapt(UUID.fromString(parts[1])).ifPresent(bungeePlayer -> {
            String worldName = parts[2];
            try {
                double x = Double.parseDouble(parts[3]);
                double y = Double.parseDouble(parts[4]);
                double z = Double.parseDouble(parts[5]);
                float yaw = Float.parseFloat(parts[6]);
                float pitch = Float.parseFloat(parts[7]);
                bungeePlayer.setLocation(new net.fameless.core.location.Location(worldName, x, y, z, pitch, yaw));
            } catch (NumberFormatException e) {
                LOGGER.warn("Plugin message contained invalid coordinates", e);
            }
        });
    }
}
