package net.fameless.spigot;

import net.fameless.core.BungeeAFK;
import net.fameless.core.handling.AFKHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class SpigotAFKHandler extends AFKHandler implements Listener {

    @Override
    public void onInit() {
        Bukkit.getPluginManager().registerEvents(this, SpigotPlatform.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        SpigotPlayer.adapt(event.getPlayer()).setActive();
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (!event.getFrom().equals(event.getTo())) {
            SpigotPlayer.adapt(event.getPlayer()).setActive();
        }
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        SpigotPlayer.adapt(event.getPlayer()).setActive();
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        SpigotPlayer player = SpigotPlayer.adapt(event.getPlayer());
        player.setActive();
        BungeeAFK.getAutoClickerDetector().registerClick(player);
    }
}
