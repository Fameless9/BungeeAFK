package net.fameless.tracking;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (params.equalsIgnoreCase("user_afk")) {
            return String.valueOf(TrackingPlugin.getInstance().isAfk(player));
        }
        return "Available Placeholder: %bafk_user_afk%";
    }
}
