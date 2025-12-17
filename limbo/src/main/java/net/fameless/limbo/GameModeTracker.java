package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.entity.Entity;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class GameModeTracker {

    private final BiConsumer<@NotNull UUID, @NotNull GameMode> callback;
    private final Map<UUID, GameMode> gameModeMap = new HashMap<>();

    public GameModeTracker(BiConsumer<UUID, GameMode> callback) {
        this.callback = callback;
        Limbo.getInstance().getScheduler().runTaskTimerAsync(LimboTracking.getInstance(), this::run, 5, 5);
    }

    private void run() {
        Set<UUID> online = Limbo.getInstance().getPlayers()
                .stream()
                .map(Entity::getUniqueId)
                .collect(Collectors.toSet());

        gameModeMap.keySet().removeIf(uuid -> !online.contains(uuid));

        for (Player player : Limbo.getInstance().getPlayers()) {
            UUID id = player.getUniqueId();
            GameMode current = player.getGamemode();
            if (current == null) continue;

            GameMode existing = gameModeMap.putIfAbsent(id, current);
            if (existing == null) {
                callback.accept(id, current);
                continue;
            }

            if (existing != current) {
                gameModeMap.put(id, current);
                callback.accept(id, current);
            }
        }
    }
}
