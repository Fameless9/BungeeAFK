package net.fameless.core;

import com.google.gson.JsonArray;
import net.fameless.api.exception.PlayerNotFoundException;
import net.fameless.api.model.Player;
import net.fameless.api.service.BackendAPI;
import net.fameless.core.adapter.APIAdapter;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.AFKState;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.region.Region;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class BungeeAFKAPIImpl extends BackendAPI {

    private final AFKHandler afkHandler;

    public BungeeAFKAPIImpl() {
        setImplementation(this);
        afkHandler = BungeeAFK.getAFKHandler();
    }

    @Override
    public void reloadPluginConfig() {
        PluginConfig.reloadAll();
    }

    @Override
    public void performConnectAction(Player player, Component kickFallbackReason, Component kickFallbackBroadcastMessage, Component connectMessage, Component connectBroadcastMessage) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        afkHandler.performConnectAction(bafkPlayer, kickFallbackReason, kickFallbackBroadcastMessage, connectMessage, connectBroadcastMessage);
    }

    @Override
    public void performKickAction(Player player, Component reason, Component broadcastMessage) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        afkHandler.performKickAction(bafkPlayer, reason, broadcastMessage);
    }

    @Override
    public void performTeleportAction(Player player, Component teleportMessage) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        afkHandler.performTeleportAction(bafkPlayer, teleportMessage);
    }

    @Override
    public boolean isPlayerAFK(Player player) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        return bafkPlayer.getAfkState() == AFKState.AFK || bafkPlayer.getAfkState() == AFKState.ACTION_TAKEN;
    }

    @Override
    public net.fameless.api.model.AFKState getPlayerAFKState(Player player) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        return APIAdapter.adapt(bafkPlayer.getAfkState());
    }

    @Override
    public void setPlayerAFKState(Player player, net.fameless.api.model.AFKState afkState) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        AFKState state = APIAdapter.adapt(afkState);
        bafkPlayer.setAfkState(state);
        if (state == AFKState.ACTIVE) {
            bafkPlayer.setActive();
        } else if (state == AFKState.WARNED) {
            bafkPlayer.setTimeSinceLastAction(afkHandler.getAfkDelayMillis());
        }
    }

    @Override
    public void setPlayerAFK(Player player, boolean afk) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        if (afk) {
            bafkPlayer.setAfkState(AFKState.WARNED);
            bafkPlayer.setTimeSinceLastAction(afkHandler.getAfkDelayMillis());
        } else {
            bafkPlayer.setActive();
        }
    }

    @Override
    public long getTimeSinceLastAction(Player player) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        return bafkPlayer.getTimeSinceLastAction();
    }

    @Override
    public void setTimeSinceLastAction(Player player, long timeSinceLastAction) throws PlayerNotFoundException {
        BAFKPlayer<?> bafkPlayer = APIAdapter.adapt(player);
        bafkPlayer.setTimeSinceLastAction(timeSinceLastAction);
    }

    @Override
    public void setMovementPatternDetectionEnabled(boolean enabled) {
        PluginConfig.get().set("movement-pattern.enabled", enabled);
        BungeeAFK.getMovementPatternDetection().reloadConfigValues();
    }

    @Override
    public boolean isMovementPatternDetectionEnabled() {
        return PluginConfig.get().getBoolean("movement-pattern.enabled", true);
    }

    @Override
    public void setAutoClickerDetectionEnabled(boolean enabled) {
        PluginConfig.get().set("auto-clicker.enabled", enabled);
        BungeeAFK.getAutoClickerDetector().reloadConfigValues();
    }

    @Override
    public boolean isAutoClickerDetectionEnabled() {
        return PluginConfig.get().getBoolean("auto-clicker.enabled", true);
    }

    @Override
    public JsonArray getBypassRegions() {
        JsonArray regions = new JsonArray();
        Region.getAllRegions().forEach(region -> regions.add(region.toJson()));
        return regions;
    }

    @Override
    public void setBypassRegions(@NotNull JsonArray bypassRegions) {
        Region.clearRegions();
        for (int i = 0; i < bypassRegions.size(); i++) {
            Region.fromJson(bypassRegions.get(i).getAsJsonObject());
        }
    }

    @Override
    public void setConfigValue(String key, Object value) {
        PluginConfig.get().set(key, value);
    }

    @Override
    public Object getConfigValue(String key) {
        return PluginConfig.get().getValue(key);
    }
}
