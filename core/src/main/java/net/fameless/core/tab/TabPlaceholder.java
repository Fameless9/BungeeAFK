package net.fameless.core.tab;

import me.neznamy.tab.api.TabAPI;
import net.fameless.core.handling.AFKState;
import net.fameless.core.player.BAFKPlayer;

import java.util.List;

public class TabPlaceholder {

    private TabPlaceholder() {
    }

    public static void register() {
        TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder(
                "%bafk_user_afk%", 50, player -> {
                    BAFKPlayer<?> bafkPlayer = BAFKPlayer.of(player.getUniqueId()).orElse(null);
                    if (bafkPlayer == null) {
                        return "";
                    }
                    return String.valueOf(bafkPlayer.getAfkState().equals(AFKState.AFK) || bafkPlayer.getAfkState().equals(AFKState.ACTION_TAKEN));
                }
        );

        TabAPI.getInstance().getPlaceholderManager().registerServerPlaceholder(
                "%bafk_afk_users%", 50, () -> {
                    List<String> afkPlayers = BAFKPlayer.PLAYERS.stream()
                            .filter(player -> !player.isOffline())
                            .filter(player -> player.getAfkState().equals(AFKState.AFK) || player.getAfkState().equals(AFKState.ACTION_TAKEN))
                            .map(BAFKPlayer::getName)
                            .toList();
                    return String.join(", ", afkPlayers);
                }
        );

        TabAPI.getInstance().getPlaceholderManager().registerServerPlaceholder(
                "%bafk_active_users%", 50, () -> {
                    List<String> activePlayers = BAFKPlayer.PLAYERS.stream()
                            .filter(player -> !player.isOffline())
                            .filter(player -> !(player.getAfkState().equals(AFKState.AFK) || player.getAfkState().equals(AFKState.ACTION_TAKEN)))
                            .map(BAFKPlayer::getName)
                            .toList();
                    return String.join(", ", activePlayers);
                }
        );
    }
}
