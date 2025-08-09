package net.fameless.core.util;

import net.fameless.core.handling.AFKState;
import net.fameless.core.player.BAFKPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PlayerFilters {

    @Contract(pure = true)
    public static @NotNull PlayerFilter isOnline() {
        return p -> !p.isOffline();
    }

    public static @NotNull PlayerFilter notMatching(final @NotNull BAFKPlayer<?> player) {
        return p -> !p.equals(player);
    }

    public static @NotNull PlayerFilter onServer(final String serverName) {
        return p -> p.getCurrentServerName().equalsIgnoreCase(serverName);
    }

    public static @NotNull PlayerFilter hasPermission(final String permission) {
        return p -> p.hasPermission(permission);
    }

    public static @NotNull PlayerFilter withAfkState(final AFKState state) {
        return p -> p.getAfkState() == state;
    }

}
