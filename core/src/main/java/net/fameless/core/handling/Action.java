package net.fameless.core.handling;

import net.fameless.core.BungeeAFK;
import net.fameless.core.config.PluginConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum Action {

    KICK,
    CONNECT,
    TELEPORT,
    NOTHING;

    private static final List<Action> excludedActions = new ArrayList<>();

    private final String IDENTIFIER = name().toLowerCase();
    private final String MESSAGE_KEY = "notification.action." + IDENTIFIER;

    public String getIdentifier() {
        return IDENTIFIER;
    }

    public String getMessageKey() {
        return MESSAGE_KEY;
    }

    public boolean isAvailable() {
        return !excludedActions.contains(this);
    }

    public static boolean isAfkServerConfigured() {
        if (!PluginConfig.getInstance().getConfig().contains("afk-server-name")) {
            return false;
        }

        String serverName = PluginConfig.getInstance().getConfig().getString("afk-server-name", null);
        if (serverName == null) return false;
        return BungeeAFK.getPlatform().doesServerExist(serverName);
    }

    public static @NotNull List<Action> getAvailableActions() {
        List<Action> availableActions = new ArrayList<>();
        for (Action action : values()) {
            if (action.isAvailable()) {
                availableActions.add(action);
            }
        }
        return availableActions;
    }

    public static @NotNull Action fromIdentifier(String identifier) throws IllegalArgumentException {
        for (Action action : values()) {
            if (action.getIdentifier().equalsIgnoreCase(identifier)) {
                return action;
            }
        }
        throw new IllegalArgumentException("No action found for identifier: " + identifier);
    }

    public static void setActionExcluded(Action action, boolean excluded) {
        if (excluded) {
            excludedActions.add(action);
        } else {
            excludedActions.remove(action);
        }
    }
}
