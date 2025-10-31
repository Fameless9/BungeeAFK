 package net.fameless.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.fameless.core.BungeeAFK;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.messaging.RequestType;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VelocityAFKHandler extends AFKHandler {

    @Override
    public void onInit() {
        var proxy = VelocityPlatform.getProxy();
        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.create("bungee", "bungeeafk"));
        proxy.getEventManager().register(VelocityPlatform.get(), this);
    }

    @Subscribe
    public void onCommandExecute(@NotNull CommandExecuteEvent event) {
        if (event.getCommandSource() instanceof Player p) {
            VelocityPlayer player = VelocityPlayer.adapt(p);
            player.setActive();
        }
    }

    @Subscribe
    public void onPlayerChat(@NotNull PlayerChatEvent event) {
        VelocityPlayer player = VelocityPlayer.adapt(event.getPlayer());
        player.setActive();
    }

    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent event) {
        VelocityPlayer player = VelocityPlayer.adapt(event.getPlayer());
        if (event.getPreviousServer() == null) {
            player.setActive();
        }
    }

    @Subscribe
    public void onPluginMessage(@NotNull PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equals("bungee:bungeeafk")) return;

        String[] parts = new String(event.getData()).split(";");
        RequestType type = RequestType.fromString(parts[0]);

        try {
            switch (type) {
                case ACTION_CAUGHT -> {
                    if (parts.length < 2) return;
                    VelocityPlayer.adapt(UUID.fromString(parts[1])).ifPresent(BAFKPlayer::setActive);
                }
                case GAMEMODE_CHANGE -> {
                    if (parts.length < 3) return;
                    VelocityPlayer.adapt(UUID.fromString(parts[1])).ifPresent(velocityPlayer -> {
                        try {
                            GameMode gameMode = GameMode.valueOf(parts[2].toUpperCase(Locale.ROOT));
                            velocityPlayer.setGameMode(gameMode);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("Invalid game mode enum: {}", parts[2]);
                        }
                    });
                }
                case LOCATION_CHANGE -> {
                    if (parts.length < 8) return;
                    handleLocationChange(parts);
                }
                case CLICK -> {
                    if (parts.length < 2) return;
                    VelocityPlayer.adapt(UUID.fromString(parts[1])).ifPresent(velocityPlayer -> {
                        velocityPlayer.setActive();
                        BungeeAFK.getAutoClickerDetector().registerClick(velocityPlayer);
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("Invalid data received: {} stacktrace: {}", Arrays.toString(parts), e.getMessage());
        }
    }

    private void handleLocationChange(String @NotNull [] parts) {
        VelocityPlayer velocityPlayer = VelocityPlayer.adapt(UUID.fromString(parts[1])).orElse(null);
        if (velocityPlayer == null) return;
        String worldName = parts[2];
        double x = Double.parseDouble(parts[3]);
        double y = Double.parseDouble(parts[4]);
        double z = Double.parseDouble(parts[5]);
        float yaw = Float.parseFloat(parts[6]);
        float pitch = Float.parseFloat(parts[7]);
        velocityPlayer.setLocation(new net.fameless.core.location.Location(worldName, x, y, z, pitch, yaw));
    }
}
