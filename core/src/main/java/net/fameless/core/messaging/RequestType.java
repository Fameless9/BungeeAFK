package net.fameless.core.messaging;

import org.jetbrains.annotations.NotNull;

public enum RequestType {

    ACTION_CAUGHT,
    TELEPORT_PLAYER,
    ENABLE_COLLISION,
    DISABLE_COLLISION,
    SET_GAMEMODE,
    GAMEMODE_CHANGE,
    LOCATION_CHANGE,
    CLICK,
    OPEN_EMPTY_INVENTORY;

    public boolean matches(String name) {
        return this.name().equalsIgnoreCase(name);
    }

    public @NotNull String getName() {
        return this.name().toLowerCase();
    }

    public static @NotNull RequestType fromString(@NotNull String name) {
        for (RequestType type : RequestType.values()) {
            if (type.matches(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching RequestType for name: " + name);
    }
}
