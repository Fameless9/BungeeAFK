package net.fameless.core.handling;

import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.util.PlayerFilters;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public enum BroadcastStrategy {

    ALL,
    GLOBAL,
    PER_SERVER,
    DISABLE;

    public @NotNull Predicate<BAFKPlayer<?>> broadcastFilter(BAFKPlayer<?> whoSent) {
        return switch (this) {
            case ALL -> PlayerFilters.passAll();
            case GLOBAL -> PlayerFilters.notMatching(whoSent);
            case PER_SERVER -> PlayerFilters.notMatching(whoSent)
                    .and(PlayerFilters.onServer(whoSent.getCurrentServerName()));
            case DISABLE -> PlayerFilters.passNone();
        };
    }
}
