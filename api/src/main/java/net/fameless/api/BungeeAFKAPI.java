package net.fameless.api;

import com.google.gson.JsonArray;
import net.fameless.api.exception.PlayerNotFoundException;
import net.fameless.api.model.AFKState;
import net.fameless.api.model.Player;
import net.fameless.api.service.BackendAPI;
import net.kyori.adventure.text.Component;

/**
 * The BungeeAFKAPI provides a static interface to the BungeeAFK plugin functionality.
 * This class acts as a facade for the actual implementation provided by the BackendAPI.
 */
public class BungeeAFKAPI {

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws UnsupportedOperationException always thrown when attempting to instantiate this class
     */
    private BungeeAFKAPI() {
        throw new UnsupportedOperationException("You cannot instantiate this class BungeeAFKAPI!");
    }

    /**
     * Checks if the BungeeAFK plugin is initialized.
     *
     * @return true if the plugin is initialized, false otherwise
     */
    public static boolean isInitialized() {
        return BackendAPI.getImplementation() != null;
    }

    /**
     * Reloads the plugin configuration.
     */
    public static void reloadPluginConfig() {
        BackendAPI.getImplementation().reloadPluginConfig();
    }

    /**
     * Performs a connect action for a player.
     *
     * @param player                       The player to perform the connect action for
     * @param kickFallbackReason           The reason message if player is kicked as fallback
     * @param kickFallbackBroadcastMessage The broadcast message if player is kicked as fallback
     * @param connectMessage               The message to send to the player on connect
     * @param connectBroadcastMessage      The message to broadcast when player connects
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static void performConnectAction(Player player, Component kickFallbackReason, Component kickFallbackBroadcastMessage, Component connectMessage, Component connectBroadcastMessage) throws PlayerNotFoundException {
        BackendAPI.getImplementation().performConnectAction(player, kickFallbackReason, kickFallbackBroadcastMessage, connectMessage, connectBroadcastMessage);
    }

    /**
     * Kicks a player with a specified reason and broadcast message.
     *
     * @param player           The player to kick
     * @param reason           The reason message shown to the kicked player
     * @param broadcastMessage The message to broadcast to other players
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static void performKickAction(Player player, Component reason, Component broadcastMessage) throws PlayerNotFoundException {
        BackendAPI.getImplementation().performKickAction(player, reason, broadcastMessage);
    }

    /**
     * Teleports a player and sends them a message.
     *
     * @param player          The player to teleport
     * @param teleportMessage The message to send to the player after teleporting
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static void performTeleportAction(Player player, Component teleportMessage) throws PlayerNotFoundException {
        BackendAPI.getImplementation().performTeleportAction(player, teleportMessage);
    }

    /**
     * Checks if a player is currently AFK.
     *
     * @param player The player to check
     *
     * @return true if the player is AFK, false otherwise
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static boolean isPlayerAFK(Player player) throws PlayerNotFoundException {
        return BackendAPI.getImplementation().isPlayerAFK(player);
    }

    /**
     * Gets the AFK state of a player.
     *
     * @param player The player to check
     *
     * @return The AFKState of the player
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static AFKState getPlayerAFKState(Player player) throws PlayerNotFoundException {
        return BackendAPI.getImplementation().getPlayerAFKState(player);
    }

    /**
     * Sets the AFK status of a player.
     *
     * @param player The player to set the AFK state for
     * @param afk    true to set the player as AFK, false to set as not AFK
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static void setPlayerAFK(Player player, boolean afk) throws PlayerNotFoundException {
        BackendAPI.getImplementation().setPlayerAFK(player, afk);
    }

    /**
     * Sets the AFK state of a player.
     *
     * @param player   The player to set the AFK state for
     * @param afkState The AFKState to set for the player
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static void setPlayerAFKState(Player player, AFKState afkState) throws PlayerNotFoundException {
        BackendAPI.getImplementation().setPlayerAFKState(player, afkState);
    }

    /**
     * Gets the time in milliseconds since a player's last action.
     *
     * @param player The player to check
     *
     * @return The time in milliseconds since the player's last action
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static long getTimeSinceLastAction(Player player) throws PlayerNotFoundException {
        return BackendAPI.getImplementation().getTimeSinceLastAction(player);
    }

    /**
     * Sets the time since a player's last action.
     *
     * @param player              The player to set the time for
     * @param timeSinceLastAction The time in milliseconds since the player's last action
     *
     * @throws PlayerNotFoundException if the player is not found
     */
    public static void setTimeSinceLastAction(Player player, int timeSinceLastAction) throws PlayerNotFoundException {
        BackendAPI.getImplementation().setTimeSinceLastAction(player, timeSinceLastAction);
    }

    /**
     * Enables or disables movement pattern detection.
     *
     * @param enabled true to enable movement pattern detection, false to disable
     */
    public static void setMovementPatternDetectionEnabled(boolean enabled) {
        BackendAPI.getImplementation().setMovementPatternDetectionEnabled(enabled);
    }

    /**
     * Checks if movement pattern detection is enabled.
     *
     * @return true if movement pattern detection is enabled, false otherwise
     */
    public static boolean isMovementPatternDetectionEnabled() {
        return BackendAPI.getImplementation().isMovementPatternDetectionEnabled();
    }

    /**
     * Enables or disables auto-clicker detection.
     *
     * @param enabled true to enable auto-clicker detection, false to disable
     */
    public static void setAutoClickerDetectionEnabled(boolean enabled) {
        BackendAPI.getImplementation().setAutoClickerDetectionEnabled(enabled);
    }

    /**
     * Checks if auto-clicker detection is enabled.
     *
     * @return true if auto-clicker detection is enabled, false otherwise
     */
    public static boolean isAutoClickerDetectionEnabled() {
        return BackendAPI.getImplementation().isAutoClickerDetectionEnabled();
    }

    /**
     * Gets the list of regions where AFK detection is bypassed.
     * Format:
     * [
     * {
     * "regionName": "RegionName",
     * "afkDetection": false,
     * "worldName": "WorldName",
     * "corner1": {
     * "x": 100,
     * "y": 64,
     * "z": 100
     * },
     * "corner2": {
     * "x": 200,
     * "y": 64,
     * "z": 200
     * }
     * }
     * ]
     *
     * @return A JsonArray containing the bypass regions
     */
    public static JsonArray getBypassRegions() {
        return BackendAPI.getImplementation().getBypassRegions();
    }

    /**
     * Load bypass regions from a JsonArray.
     * This method will override the existing bypass regions with the new ones provided.
     * Recommended to use getBypassRegions() to retrieve the current regions and modify them before setting.
     * <p>
     * Format:
     * [
     * {
     * "regionName": "RegionName",
     * "afkDetection": false,
     * "worldName": "WorldName",
     * "corner1": {
     * "x": 100,
     * "y": 64,
     * "z": 100
     * },
     * "corner2": {
     * "x": 200,
     * "y": 64,
     * "z": 200
     * }
     * }
     * ]
     * <p>
     * Will throw an exception if the provided JsonArray is malformed or does not match the expected structure.
     *
     * @param bypassRegions A JsonArray containing the new bypass regions
     */
    public static void setBypassRegions(JsonArray bypassRegions) {
        BackendAPI.getImplementation().setBypassRegions(bypassRegions);
    }

    /**
     * Sets a configuration value.
     *
     * @param key   The configuration key
     * @param value The value to set for the key
     */
    public static void setConfigValue(String key, Object value) {
        BackendAPI.getImplementation().setConfigValue(key, value);
    }

    /**
     * Gets a configuration value.
     *
     * @param key The configuration key
     *
     * @return The value associated with the key, or null if not found
     */
    public static Object getConfigValue(String key) {
        return BackendAPI.getImplementation().getConfigValue(key);
    }
}
