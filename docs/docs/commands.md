# Commands

BungeeAFK provides several commands to configure and manage the plugin's features. This document outlines all available
commands and their associated permissions.

## Main Command

The main command is `/bungeeafk` or `/bafk` and requires the `bungeeafk.command` permission to use.

## Language Commands

Commands to change or reload language settings.

| Command                      | Description                  |
|------------------------------|------------------------------|
| `/bungeeafk lang <language>` | Changes the current language |
| `/bungeeafk lang reload`     | Reloads all language files   |

## Configuration Commands

Commands to configure general plugin settings.

| Command                                                | Description                                                                    |
|--------------------------------------------------------|--------------------------------------------------------------------------------|
| `/bungeeafk configure dump`                            | Send a message with the current Configuration                                  |
| `/bungeeafk configure allow-bypass <true/false>`       | Sets whether players with the permission `afk.bypass` can bypass AFK detection |
| `/bungeeafk configure warning-delay <seconds>`         | Sets the warning delay in seconds                                              |
| `/bungeeafk configure afk-delay <seconds>`             | Sets how long until a player is considered AFK after last interaction          |
| `/bungeeafk configure action-delay <seconds>`          | Sets the delay between being considered AFK and action                         |
| `/bungeeafk configure action <action>`                 | Sets the action to take when a player is AFK and action delay is reached       |
| `/bungeeafk configure caption <language> <key> <text>` | Sets a specific language caption (Supports Minimessage)                        |
| `/bungeeafk configure afk-location`                    | Sets the AFK teleport location to your current position                        |
| `/bungeeafk configure reloadconfig`                    | Reloads the plugin configuration (This will override all of the cached config) |
| `/bungeeafk configure saveconfig`                      | Saves the current configuration to the config.yml file                         |
| `/bungeeafk configure broadcast-strategy <strategy>`   | Sets the broadcast strategy for AFK messages                                   |

### Proxy-only Configuration Commands

These commands are only available when running on a proxy server.

| Command                                        | Description                        |
|------------------------------------------------|------------------------------------|
| `/bungeeafk configure disable-server <server>` | Disables AFK detection on a server |
| `/bungeeafk configure enable-server <server>`  | Enables AFK detection on a server  |
| `/bungeeafk configure disabled-servers`        | Lists all disabled servers         |

## Region Commands

Commands to manage AFK bypass regions.

| Command                                                              | Description                                        |
|----------------------------------------------------------------------|----------------------------------------------------|
| `/bungeeafk region reload`                                           | Reloads all bypass regions from config.yml         |
| `/bungeeafk region list`                                             | Lists all bypass regions                           |
| `/bungeeafk region details <region>`                                 | Shows details of a region                          |
| `/bungeeafk region toggle-detection <region>`                        | Toggles AFK detection for a region                 |
| `/bungeeafk region remove <region>`                                  | Removes a bypass region                            |
| `/bungeeafk region add <name> <world> <x1> <y1> <z1> <x2> <y2> <z2>` | Creates a new bypass region with specified corners |

## Auto-Clicker Detection Commands

Commands to configure auto-clicker detection.

| Command                                              | Description                                                                                                   |
|------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `/bungeeafk auto-clicker enable`                     | Enables auto-clicker detection globally                                                                       |
| `/bungeeafk auto-clicker disable`                    | Disables auto-clicker detection globally                                                                      |
| `/bungeeafk auto-clicker toggle-bypass`              | Toggles whether players with the permission `bungeeafk.auto-clicker.bypass` can bypass auto-clicker detection |
| `/bungeeafk auto-clicker action <action>`            | Sets the action on auto-clicker detection                                                                     |
| `/bungeeafk auto-clicker toggle-notify-player`       | Toggles player notification on detection                                                                      |
| `/bungeeafk auto-clicker detection-history <player>` | Shows auto-clicker detection history for a player                                                             |
| `/bungeeafk auto-clicker toggle-on-server <server>`  | Toggles auto-clicker detection on a specific server (proxy only)                                              |

## Movement Pattern Detection Commands

Commands to configure movement pattern detection.

| Command                                                  | Description                                                                                                           |
|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `/bungeeafk movement-pattern enable`                     | Enables movement pattern detection                                                                                    |
| `/bungeeafk movement-pattern disable`                    | Disables movement pattern detection                                                                                   |
| `/bungeeafk movement-pattern toggle-bypass`              | Toggles whether players with the permission `bungeeafk.movement-pattern.bypass` can bypass movement pattern detection |
| `/bungeeafk movement-pattern action <action>`            | Sets the action on movement pattern detection                                                                         |
| `/bungeeafk movement-pattern toggle-notify-player`       | Toggles player notification on detection                                                                              |
| `/bungeeafk movement-pattern detection-history <player>` | Shows movement pattern detection history for a player                                                                 |
| `/bungeeafk movement-pattern toggle-on-server <server>`  | Toggles movement pattern detection on a specific server (proxy only)                                                  |

## Permissions

The main permission for using all commands is `bungeeafk.command`.
