package net.fameless.core.util;

import net.fameless.core.caption.Caption;
import net.fameless.core.config.PluginConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class YamlUtil {

    public static @NotNull String generateConfig() {
        return """

            #  ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗███████╗ █████╗ ███████╗██╗  ██╗
            #  ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔════╝██╔══██╗██╔════╝██║ ██╔╝
            #  ██████╔╝██║   ██║██╔██╗ ██║██║  ███╗█████╗  █████╗  ███████║█████╗  █████╔╝
            #  ██╔══██╗██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██╔══╝  ██╔══██║██╔══╝  ██╔═██╗
            #  ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗███████╗██║  ██║██║     ██║  ██╗
            #  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝  ╚═╝
            #  BungeeAFK - AFK detection plugin for BungeeCord, Velocity and Spigot by Fameless9

            # Language used for messages and notifications
            # Available languages: en, de
            lang: %s

            # Whether to send broadcast messages to all players when someone is marked as AFK, returns from AFK or an AFK action is performed
            # Lang entries: 'notification.afk_broadcast', 'notification.return_broadcast', 'notification.afk_kick_broadcast', 'notification.afk_disconnect_broadcast'
            # This option does not affect auto-clicker or movement pattern detection broadcasts to players with the notify-permission (see below)
            afk-broadcast: %b
            
            # Whether to send broadcast messages about AFK actions only to players on the same server as the affected player
            # Requires afk-broadcast to be enabled
            afk-broadcast-only-current-server: %b
            
            # Cooldown time between AFK toggle commands to prevent spamming
            # Time unit is in seconds
            afk-command-cooldown: %d

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

            # Used for proxy platforms (BungeeCord/Velocity) to disable AFK mode, when they
            # return from the AFK server
            # NOTE: At the moment, this only works for Velocity proxies
            afk-update-state-when-returning: %b

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
            """.formatted(
                Caption.getCurrentLanguage().getIdentifier(),
                PluginConfig.get().getBoolean("afk-broadcast", true),
                PluginConfig.get().getBoolean("afk-broadcast-only-current-server", true),
                PluginConfig.get().getInt("afk-command-cooldown", 30),
                PluginConfig.get().getInt("warning-delay", 60),
                PluginConfig.get().getInt("afk-delay", 600),
                PluginConfig.get().getInt("action-delay", 630),
                PluginConfig.get().getString("action", "kick"),
                PluginConfig.get().getString("afk-server-name", ""),
                PluginConfig.get().getBoolean("afk-update-state-when-returning", false),
                PluginConfig.get().getSection("afk-location").get("world"),
                PluginConfig.get().getSection("afk-location").get("x"),
                PluginConfig.get().getSection("afk-location").get("y"),
                PluginConfig.get().getSection("afk-location").get("z"),
                PluginConfig.get().getBoolean("allow-bypass", true),
                PluginConfig.get().getStringList("disabled-servers"),
                PluginConfig.YAML.dumpAsMap(Map.of("bypass-regions", PluginConfig.get().getSection("bypass-regions"))),
                PluginConfig.get().getBoolean("auto-clicker.enabled", true),
                PluginConfig.get().getBoolean("auto-clicker.allow-bypass", true),
                PluginConfig.get().getString("auto-clicker.notify-permission", "bungeeafk.auto-clicker.notify"),
                PluginConfig.get().getBoolean("auto-clicker.notify-player", true),
                PluginConfig.get().getString("auto-clicker.action", "open-inv"),
                PluginConfig.get().getStringList("auto-clicker.disabled-servers"),
                PluginConfig.get().getInt("auto-clicker.sample-size", 200),
                PluginConfig.get().getInt("auto-clicker.consecutive-detections", 3),
                PluginConfig.get().getInt("auto-clicker.stddev-threshold", 10),
                PluginConfig.get().getInt("auto-clicker.min-click-interval", 30),
                PluginConfig.get().getBoolean("movement-pattern.enabled", true),
                PluginConfig.get().getBoolean("movement-pattern.allow-bypass", true),
                PluginConfig.get().getString("movement-pattern.notify-permission", "bungeeafk.movement-pattern.notify"),
                PluginConfig.get().getBoolean("movement-pattern.notify-player", true),
                PluginConfig.get().getString("movement-pattern.action", "kick"),
                PluginConfig.get().getStringList("movement-pattern.disabled-servers"),
                PluginConfig.get().getInt("movement-pattern.clear-after", 600),
                PluginConfig.get().getDouble("movement-pattern.certainty-threshold", 0.9),
                PluginConfig.get().getInt("movement-pattern.sample-size", 5)
        );
    }

}
