package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class GameModeTracker {

    private final BiConsumer<@NotNull Player, @NotNull GameMode> callback;
    private final Map<Player, GameMode> gameModeMap = new HashMap<>();

    public GameModeTracker(BiConsumer<Player, GameMode> callback) {
        this.callback = callback;
        Limbo.getInstance().getScheduler().runTaskTimerAsync(LimboTracking.getInstance(), this::run, 5, 5);
    }

    private void run() {
        Set<Player> online = Limbo.getInstance().getPlayers();
        gameModeMap.keySet().removeIf(player -> !online.contains(player));

        for (Player player : Limbo.getInstance().getPlayers()) {
            GameMode current = player.getGamemode();
            if (current == null) continue;

            GameMode existing = gameModeMap.putIfAbsent(player, current);
            if (existing == null) {
                callback.accept(player, current);
                continue;
            }

            if (existing != current) {
                gameModeMap.put(player, current);
                callback.accept(player, current);
            }
        }
    }
}
