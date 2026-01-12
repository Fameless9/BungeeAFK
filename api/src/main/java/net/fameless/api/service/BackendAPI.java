package net.fameless.api.service;

import com.google.gson.JsonArray;
import net.fameless.api.exception.PlayerNotFoundException;
import net.fameless.api.model.AFKState;
import net.fameless.api.model.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public abstract class BackendAPI {

    private static BackendAPI implementation;

    public static BackendAPI getImplementation() {
        if (implementation == null) {
            throw new IllegalStateException("No API implementation set. Is BungeeAFK enabled?");
        }

        return implementation;
    }

    public static void setImplementation(BackendAPI implementation) {
        BackendAPI.implementation = implementation;
    }

    public abstract void reloadPluginConfig();

    public abstract void performConnectAction(Player player, Component kickFallbackReason, Component kickFallbackBroadcastMessage, Component connectMessage, Component connectBroadcastMessage) throws PlayerNotFoundException;

    public abstract void performKickAction(Player player, Component reason, Component broadcastMessage) throws PlayerNotFoundException;

    public abstract void performTeleportAction(Player player, Component teleportMessage) throws PlayerNotFoundException;

    public abstract boolean isPlayerAFK(Player player) throws PlayerNotFoundException;

    public abstract AFKState getPlayerAFKState(Player player) throws PlayerNotFoundException;

    public abstract void setPlayerAFKState(Player player, AFKState afkState) throws PlayerNotFoundException;

    public abstract void setPlayerAFK(Player player, boolean afk) throws PlayerNotFoundException;

    public abstract long getTimeSinceLastAction(Player player) throws PlayerNotFoundException;

    public abstract void setTimeSinceLastAction(Player player, long timeSinceLastAction) throws PlayerNotFoundException;

    public abstract void setMovementPatternDetectionEnabled(boolean enabled);

    public abstract boolean isMovementPatternDetectionEnabled();

    public abstract void setAutoClickerDetectionEnabled(boolean enabled);

    public abstract boolean isAutoClickerDetectionEnabled();

    public abstract JsonArray getBypassRegions();

    public abstract void setBypassRegions(JsonArray bypassRegions);

    public abstract void setConfigValue(String key, Object value);

    public abstract @Nullable Object getConfigValue(String key);

}
