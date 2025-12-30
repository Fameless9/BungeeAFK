package net.fameless.core.util;

import net.fameless.core.player.BAFKPlayer;
import net.kyori.adventure.text.Component;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class MessageBroadcaster {

    @SafeVarargs
    public static void broadcastMessageToFiltered(Component message, Predicate<BAFKPlayer<?>>... filters) {
        Predicate<BAFKPlayer<?>> combinedFilter = Stream.of(filters)
                .reduce(Predicate::and)
                .orElse(player -> true);

        BAFKPlayer.PLAYERS.stream()
                .filter(PlayerFilters.isOnline())
                .filter(combinedFilter)
                .forEach(player -> player.sendMessage(message));
    }

}
