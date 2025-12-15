package net.fameless.bungee;

import net.fameless.core.handling.AFKHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BungeeAFKHandler extends AFKHandler implements Listener {

    @Override
    public void onInit() {
        BungeePlatform.proxyServer().registerChannel("bungee:bungeeafk");
        BungeePlatform.proxyServer().getPluginManager().registerListener(BungeePlatform.get(), this);
    }

    @EventHandler
    public void onPostLogin(@NotNull PostLoginEvent event) {
        BungeePlayer bungeePlayer = BungeePlayer.adapt(event.getPlayer());
        awaitConnectionAndHandleJoin(bungeePlayer, 0);
    }

    private void awaitConnectionAndHandleJoin(BungeePlayer bungeePlayer, int attempt) {
        final int maxAttempts = 50;
        BungeePlatform.get().getProxy().getScheduler().schedule(BungeePlatform.get(), () -> {
            Optional<ProxiedPlayer> playerOpt = bungeePlayer.getPlatformPlayer();
            if (playerOpt.isPresent() && playerOpt.get().getServer() != null) {
                bungeePlayer.setActive();
            } else if (attempt < maxAttempts) {
                awaitConnectionAndHandleJoin(bungeePlayer, attempt + 1);
            } else {
                LOGGER.error("Timeout while waiting for player {} to have a valid server connection. Previous states cannot be reverted.", bungeePlayer.getUniqueId());
            }
        }, 100, TimeUnit.MILLISECONDS);
    }
}
