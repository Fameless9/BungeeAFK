package net.fameless.core.util;

import net.fameless.core.caption.Caption;
import net.fameless.core.config.Config;
import net.fameless.core.handling.BroadcastStrategy;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.Locale;
import java.util.Map;

public class YamlUtil {

    public static Yaml YAML = new Yaml();

    public static @NotNull String generateConfig() {
        return String.format(Locale.US, """
                        
                        #  ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗███████╗ █████╗ ███████╗██╗  ██╗
                        #  ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔════╝██╔══██╗██╔════╝██║ ██╔╝
                        #  ██████╔╝██║   ██║██╔██╗ ██║██║  ███╗█████╗  █████╗  ███████║█████╗  █████╔╝
                        #  ██╔══██╗██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██╔══╝  ██╔══██║██╔══╝  ██╔═██╗
                        #  ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗███████╗██║  ██║██║     ██║  ██╗
                        #  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝  ╚═╝
                        #  BungeeAFK - AFK detection plugin for BungeeCord, Velocity and Spigot by Fameless9
                        
                        # The plugin detects whether the config.yml file has been edited during runtime
                        # If false, the plugin will not save cached config values to the config.yml file on shutdown, if
                        # a change has been detected
                        # Note: Setting this to false may lead to loss of configuration changes made via in-game commands or APIs
                        # if you edit the config.yml file manually while the server is running. To preserve manual changes, set this to false.
                        overwrite-file-changes: %b
                        
                        # Language used for messages and notifications
                        # Available languages: en, de
                        lang: %s
                        
                        # Netty port used for communication between the proxy and Spigot servers
                        # Note: Changing this value requires updating the Spigot server configuration as well\s
                        netty-port: %d
                        
                        # Delay after which the warning message is sent to the player (seconds) | Lang entry: "notification.afk_warning"
                        # e.g., if set to 60, the player will receive a warning message after 1 minute of inactivity
                        warning-delay: %d
                        
                        # Delay after which a player is marked as AFK (seconds)
                        # e.g., if set to 600, the player will be marked as AFK after 10 minutes of inactivity
                        afk-delay: %d
                        
                        # Delay after which the action is executed (seconds)
                        # e.g., if set to 630, the player will be kicked or connected after 10 minutes and 30 seconds of inactivity
                        action-delay: %d
                        
                        # Action to be performed after action delay is reached. Possible values: "kick", "connect", "nothing".
                        # "kick" - player is kicked from the server
                        # "connect" - player is connected to the server specified in the "afk-server-name" option
                        # "nothing" - nothing happens
                        action: "%s"
                        
                        # Name of the server to which the player will be connected if the action is set to "connect"
                        # If the server does not exist, the action will default to "kick"
                        # !!! Only available for BungeeCord and Velocity !!!
                        afk-server-name: %s
                        
                        # Whether to reduce the simulation distance for AFK players
                        # This feature can reduce server load by limiting the number of chunks and entities sent to AFK players
                        # Note: This feature only works with tracking servers running on Paper
                        reduce-simulation-distance: %b
                        
                        # Whether to send action bar messages while handling AFK states
                        # Note: Actionbar messages might overlap with other plugins using the action bar
                        # Affected language keys: 'actionbar.afk', 'actionbar.afk_moved'
                        actionbar: %b
                        
                        # AFK zone configuration
                        # If the action is set to "teleport", the player will be teleported to this location
                        afk-location:
                          world: %s
                          x: %s
                          y: %s
                          z: %s
                        
                        # Whether to allow bypass of AFK detection for players with the "afk.bypass" permission (global)
                        allow-bypass: %b
                        
                        # List of servers where AFK detection is disabled
                        # Players on these servers will not be marked as AFK, and no actions will be performed
                        # Example: [lobby, hub]
                        disabled-servers:
                          %s
                        
                        # Strategy used to broadcast AFK notifications to other players
                        # Affected message keys: 'notification.afk_broadcast', 'notification.return_broadcast', 'notification.afk_kick_broadcast', 'notification.afk_disconnect_broadcast'
                        # 'PASS_ALL' - The message is sent to every online player on the proxy
                        # 'GLOBAL' - The message is sent to every online player on the proxy, except the player who is AFK
                        # 'PER_SERVER' - The message is sent to every player on the same server as the player who is AFK
                        # 'DISABLE' - No broadcast message is sent
                        broadcast-strategy: %s
                        
                        # List of messages to suppress
                        # Possible values are all message keys, as defined in the lang files
                        # The messages defined here will not be sent to any player
                        suppressed-messages:
                          %s
                        
                        # Cooldown time between AFK toggle commands to prevent spamming
                        # Time unit is in seconds
                        afk-command-cooldown: %d
                        
                        # Map of regions where AFK detection can be toggled on or off independently
                        # Players in regions where AFK detection is false will not be marked as AFK, and no actions will be performed
                        # Regions should be added using the /bafk region add <param> command
                        # Manually adding regions here is possible, but not recommended, unless you know what you're doing. Run /bafk region reload to reload regions from here
                        %s
                        
                        # Auto-Clicker Detection Settings
                        auto-clicker:
                          enabled: %b
                        
                          # Whether to allow bypass of auto-clicker detection for players with the "bungeeafk.autoclicker.bypass" permission
                          allow-bypass: %b
                        
                          # Players with this permission will receive a notification that an autoclicker has been detected
                          notify-permission: %s
                        
                          # Whether to notify the player when an auto clicker is detected for them
                          notify-player: %b
                        
                          # Action to be performed when auto clicker has been detected
                          # "kick" - player is kicked from the server (default value)
                          # "open-inv" - open an empty inventory to prevent clicks from impacting anything
                          # "nothing" - nothing will happen
                          action: %s
                        
                          # List of servers where auto clicker detection is disabled
                          disabled-servers:
                            %s
                        
                          # These values are fine-tuned to balance false positives and detection accuracy
                          # sample-size: 150 - Number of clicks analyzed in a rolling window
                          # consecutive-detections: 3 - Number of consecutive suspicious windows required to trigger detection
                          # stddev-threshold: 50 - Standard deviation threshold (in milliseconds) for click interval timing consistency;
                          #    lower stddev indicates more machine-like consistent clicking
                          # min-click-interval: 50 - Minimum interval between clicks (in milliseconds) to be considered valid;
                          #    50ms = 20 clicks per second, 1000ms = 1 click per second
                          # With these settings, a player must click about 450 times in a row with very consistent intervals
                          # (stddev of inter-click timings below 50 ms) or with 20cps+ to be detected as an auto clicker, which is very unlikely
                          sample-size: %d
                          consecutive-detections: %d
                          stddev-threshold: %d
                          min-click-interval: %d
                        
                        # Movement Pattern Detection Settings
                        movement-pattern:
                          enabled: %b
                        
                          # Whether to allow bypass of movement pattern detection for players with the "bungeeafk.movement-pattern.bypass" permission
                          allow-bypass: %b
                        
                          # Players with this permission will receive a notification that a movement pattern has been detected
                          notify-permission: %s
                        
                          # Whether to notify the player when a movement pattern is detected for them
                          notify-player: %b
                        
                          # Action to be performed when movement pattern has been detected
                          # "kick" - player is kicked from the server (default value)
                          # "connect" - player is connected to the server specified in the "afk-server-name" option
                          # "teleport" - player is teleported to the afk-location as configured above
                          # "nothing" - nothing will happen
                          action: %s
                        
                          # List of servers where movement pattern detection is disabled
                          disabled-servers:
                            %s
                        
                          # Time after which an individual movement is cleared and therefore cannot be used for analyzing patterns
                          # This is to prevent high memory usage and to ensure that only recent movements are considered
                          # If set to 600, movements older than 10 minutes will not be considered for pattern detection
                          # If set to 0, movements will never be cleared and will be kept in memory indefinitely
                          # Note: Setting this to 0 may lead to high memory usage if many players are online
                          #       and they move frequently, so it is recommended to set this to a reasonable value
                          #       depending on your server's activity and player base size
                          #       A value of 600 (10 minutes) is a good balance for most servers
                          clear-after: %d
                        
                          certainty-threshold: %f  # Minimum certainty required to trigger detection (0.0 - 1.0)
                          sample-size: %d          # Number of movement samples on the same location to analyze in a rolling window
                        """,
                Config.getInstance().getBoolean("overwrite-file-changes", true),
                Caption.getCurrentLanguage().toLowerCase(Locale.US),
                Config.getInstance().getInt("netty-port", 9000),
                Config.getInstance().getInt("warning-delay", 60),
                Config.getInstance().getInt("afk-delay", 600),
                Config.getInstance().getInt("action-delay", 630),
                Config.getInstance().getString("action", "kick"),
                Config.getInstance().getString("afk-server-name", ""),
                Config.getInstance().getBoolean("reduce-simulation-distance", false),
                Config.getInstance().getBoolean("actionbar", true),
                Config.getInstance().getSection("afk-location").get("world"),
                Config.getInstance().getSection("afk-location").get("x"),
                Config.getInstance().getSection("afk-location").get("y"),
                Config.getInstance().getSection("afk-location").get("z"),
                Config.getInstance().getBoolean("allow-bypass", true),
                Config.getInstance().getStringList("disabled-servers"),
                Config.getInstance().getString("broadcast-strategy", BroadcastStrategy.PER_SERVER.name()),
                Config.getInstance().getStringList("suppressed-messages"),
                Config.getInstance().getInt("afk-command-cooldown", 10),
                YAML.dumpAsMap(Map.of("bypass-regions", Config.getInstance().getSection("bypass-regions"))),
                Config.getInstance().getBoolean("auto-clicker.enabled", true),
                Config.getInstance().getBoolean("auto-clicker.allow-bypass", true),
                Config.getInstance().getString("auto-clicker.notify-permission", "bungeeafk.auto-clicker.notify"),
                Config.getInstance().getBoolean("auto-clicker.notify-player", true),
                Config.getInstance().getString("auto-clicker.action", "open-inv"),
                Config.getInstance().getStringList("auto-clicker.disabled-servers"),
                Config.getInstance().getInt("auto-clicker.sample-size", 200),
                Config.getInstance().getInt("auto-clicker.consecutive-detections", 3),
                Config.getInstance().getInt("auto-clicker.stddev-threshold", 10),
                Config.getInstance().getInt("auto-clicker.min-click-interval", 30),
                Config.getInstance().getBoolean("movement-pattern.enabled", true),
                Config.getInstance().getBoolean("movement-pattern.allow-bypass", true),
                Config.getInstance().getString("movement-pattern.notify-permission", "bungeeafk.movement-pattern.notify"),
                Config.getInstance().getBoolean("movement-pattern.notify-player", true),
                Config.getInstance().getString("movement-pattern.action", "kick"),
                Config.getInstance().getStringList("movement-pattern.disabled-servers"),
                Config.getInstance().getInt("movement-pattern.clear-after", 600),
                Config.getInstance().getDouble("movement-pattern.certainty-threshold", 0.9),
                Config.getInstance().getInt("movement-pattern.sample-size", 5)
        );
    }

}
