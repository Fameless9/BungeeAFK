package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GameModeTracker {

    private final BiConsumer<Player, GameMode> callback;
    private final Map<Player, GameMode> gameModeMap = new HashMap<>();

    public GameModeTracker(BiConsumer<Player, GameMode> callback) {
        this.callback = callback;
        Limbo.getInstance().getScheduler().runTaskTimerAsync(LimboTracking.getInstance(), this::run, 5, 5);
    }

    private void run() {
        for (Player player : Limbo.getInstance().getPlayers()) {
            if (!gameModeMap.containsKey(player)) {
                gameModeMap.put(player, player.getGamemode());
                callback.accept(player, player.getGamemode());
                continue;
            }
            GameMode cached = gameModeMap.get(player);
            if (cached != player.getGamemode()) {
                gameModeMap.put(player, player.getGamemode());
                callback.accept(player, player.getGamemode());
            }
        }
    }
}
