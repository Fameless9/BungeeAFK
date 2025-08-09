 package net.fameless.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.fameless.core.BungeeAFK;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.AFKState;
import net.fameless.core.messaging.RequestType;
import net.fameless.core.player.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VelocityAFKHandler extends AFKHandler {

    @Override
    public void init() {
        var proxy = VelocityPlatform.getProxy();
        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.create("bungee", "bungeeafk"));
        proxy.getEventManager().register(VelocityPlatform.get(), this);
    }

    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent event) {
        if (event.getPreviousServer() == null) {
            handleJoin(VelocityPlayer.adapt(event.getPlayer()));
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
                    handleActionCaught(parts[1]);
                }
                case GAMEMODE_CHANGE -> {
                    if (parts.length < 3) return;
                    handleGameModeChange(parts[1], parts[2]);
                }
                case LOCATION_CHANGE -> {
                    if (parts.length < 8) return;
                    handleLocationChange(parts);
                }
                case CLICK -> {
                    if (parts.length < 2) return;
                    handleClick(parts[1]);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Invalid data received: {} stacktrace: {}", Arrays.toString(parts), e.getMessage());
        }
    }

    private void handleClick(String uuidStr) {
        VelocityPlayer velocityPlayer = VelocityPlayer.adapt(UUID.fromString(uuidStr)).orElse(null);
        if (velocityPlayer == null) return;
        velocityPlayer.setTimeSinceLastAction(0);
        velocityPlayer.setAfkState(AFKState.ACTIVE);
        BungeeAFK.getAutoClickerDetector().registerClick(velocityPlayer);
    }

    private void handleActionCaught(String uuidStr) {
        VelocityPlayer velocityPlayer = VelocityPlayer.adapt(UUID.fromString(uuidStr)).orElse(null);
        if (velocityPlayer == null) return;
        velocityPlayer.setTimeSinceLastAction(0);
        velocityPlayer.setAfkState(AFKState.ACTIVE);
        BungeeAFK.getAFKHandler().handleAction(velocityPlayer);
    }

    private void handleGameModeChange(String uuidStr, String modeStr) {
        VelocityPlayer velocityPlayer = VelocityPlayer.adapt(UUID.fromString(uuidStr)).orElse(null);
        if (velocityPlayer == null) return;
        GameMode gameMode = GameMode.valueOf(modeStr.toUpperCase(Locale.ROOT));
        velocityPlayer.setGameMode(gameMode);
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
