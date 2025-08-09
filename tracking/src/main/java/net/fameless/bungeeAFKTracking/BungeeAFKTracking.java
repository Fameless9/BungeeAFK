package net.fameless.bungeeAFKTracking;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;


public class BungeeAFKTracking extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bungee:bungeeafk");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "bungee:bungeeafk", this::handleIncomingMessage);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (!event.getFrom().equals(event.getTo())) {
            sendBungeeMessage(event.getPlayer());
            sendLocationChangeMessage(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        sendBungeeMessage(event.getPlayer());
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        sendBungeeMessage(event.getPlayer());
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        sendGamemodeChangeMessage(event.getPlayer(), event.getNewGameMode());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) return;
        sendClickDetectedMessage(event.getPlayer());
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            sendGamemodeChangeMessage(event.getPlayer(), event.getPlayer().getGameMode());
            sendLocationChangeMessage(event.getPlayer(), event.getPlayer().getLocation());
        }, 5L); // Delay to ensure player is fully initialized on proxy platform
    }

    private void sendClickDetectedMessage(@NotNull Player player) {
        String message = "click;" + player.getUniqueId();
        byte[] data = message.getBytes();
        player.sendPluginMessage(this, "bungee:bungeeafk", data);
    }

    private void sendLocationChangeMessage(@NotNull Player player, @NotNull Location location) {
        String message = "location_change;" + player.getUniqueId() + ";" + Objects.requireNonNull(location.getWorld()).getName() + ";" +
                location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" +
                location.getYaw() + ";" + location.getPitch();
        byte[] data = message.getBytes();
        player.sendPluginMessage(this, "bungee:bungeeafk", data);
    }

    private void sendGamemodeChangeMessage(@NotNull Player player, @NotNull GameMode gameMode) {
        String message = "gamemode_change;" + player.getUniqueId() + ";" + gameMode.name();
        byte[] data = message.getBytes();
        player.sendPluginMessage(this, "bungee:bungeeafk", data);
    }

    private void sendBungeeMessage(@NotNull Player player) {
        byte[] data = ("action_caught;" + player.getUniqueId()).getBytes();
        player.sendPluginMessage(this, "bungee:bungeeafk", data);
    }

    private void handleIncomingMessage(String channel, Player player, byte[] message) {
        String messageString = new String(message);
        String[] parts = messageString.split(";");
        if (parts.length < 1) return;
        switch (parts[0].toLowerCase()) {
            case "teleport_player" -> {
                try {
                    Player targetPlayer = Bukkit.getPlayer(UUID.fromString(parts[1]));
                    World world = Bukkit.getWorld(parts[2]);
                    double x = Double.parseDouble(parts[3]);
                    double y = Double.parseDouble(parts[4]);
                    double z = Double.parseDouble(parts[5]);
                    float yaw = Float.parseFloat(parts[6]);
                    float pitch = Float.parseFloat(parts[7]);

                    if (targetPlayer == null || world == null) {
                        getLogger().severe("Invalid teleport data received: " + messageString);
                        return;
                    }

                    player.teleport(new Location(world, x, y, z, yaw, pitch));
                } catch (Exception e) {
                    getLogger().severe("Invalid teleport data received: " + e.getMessage());
                }
            }
            case "set_gamemode" -> {
                if (parts.length < 3) {
                    getLogger().severe("Invalid set_gamemode data received: " + messageString);
                    return;
                }
                Player targetPlayer = Bukkit.getPlayer(UUID.fromString(parts[1]));
                if (targetPlayer == null) {
                    getLogger().severe("Invalid player UUID received for gamemode change: " + parts[1]);
                    return;
                }
                try {
                    String gameModeString = parts[2];
                    targetPlayer.setGameMode(GameMode.valueOf(gameModeString));
                } catch (NumberFormatException e) {
                    getLogger().severe("Invalid gamemode value received: " + parts[2]);
                }
            }
            case "open_empty_inventory" -> {
                if (parts.length < 2) {
                    getLogger().severe("Invalid open_empty_inventory data received: " + messageString);
                    return;
                }
                Player targetPlayer = Bukkit.getPlayer(UUID.fromString(parts[1]));
                if (targetPlayer == null) {
                    getLogger().severe("Invalid player UUID received for open_empty_inventory: " + parts[1]);
                    return;
                }

                targetPlayer.openInventory(Bukkit.createInventory(null, 27, ""));
            }
        }
    }
}
