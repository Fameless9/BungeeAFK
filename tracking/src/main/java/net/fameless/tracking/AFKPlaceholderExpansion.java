package net.fameless.tracking;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class AFKPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "bafk";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Fameless9";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return switch (params) {
            case "user_afk" -> {
                if (player == null) yield null;
                yield String.valueOf(TrackingPlugin.getInstance().isAfk(player));
            }
            case "afk_users" -> {
                Set<String> players = Bukkit.getOnlinePlayers().stream()
                        .filter(TrackingPlugin.getInstance()::isAfk)
                        .map(Entity::getName)
                        .collect(Collectors.toSet());
                yield String.join(", ", players).trim();
            }
            case "active_users" -> {
                Set<String> players = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !TrackingPlugin.getInstance().isAfk(p))
                        .map(Entity::getName)
                        .collect(Collectors.toSet());
                yield String.join(", ", players).trim();
            }
            default -> null;
        };
    }
}
