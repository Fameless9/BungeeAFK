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
            # Manually adding regions here is possible, but not recommended, unless you know what you're doing
            %s
            """.formatted(
                Caption.getCurrentLanguage().getIdentifier(),
                PluginConfig.get().getInt("warning-delay", 60),
                PluginConfig.get().getInt("afk-delay", 600),
                PluginConfig.get().getInt("action-delay", 630),
                PluginConfig.get().getString("action", "kick"),
                PluginConfig.get().getString("afk-server-name", ""),
                PluginConfig.get().getSection("afk-location").get("world"),
                PluginConfig.get().getSection("afk-location").get("x"),
                PluginConfig.get().getSection("afk-location").get("y"),
                PluginConfig.get().getSection("afk-location").get("z"),
                PluginConfig.get().getBoolean("allow-bypass", true),
                PluginConfig.get().getStringList("disabled-servers"),
                PluginConfig.YAML.dumpAsMap(Map.of("bypass-regions", PluginConfig.get().getSection("bypass-regions")))
        );
    }

}
