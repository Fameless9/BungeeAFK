BungeeAFK comes with a lot of customization options, allowing you to tailor the plugin to your server's needs.  
Below is a guide on how to configure BungeeAFK effectively.

#### Custom Messages
To customize the messages displayed by BungeeAFK, see [Custom Messages](custom_messages.md).

#### Configuration File
The main configuration file is located at `plugins/BungeeAFK/config.yml`. Open this file to view and edit the settings.

## Configuration Options
---
### General Settings

#### Language (`lang`)
The language setting allows you to choose the language for the plugin's messages. You can set it to `en` for English or `de` for German.  
The plugin will use the language file: `lang_xx.json` where `xx` is the language code you set.  

Default is `en`.

#### AFK-Broadcasts (`afk-broadcast`)
If true, a broadcast message will be sent to all players (except the AFK player) when a player goes AFK, returns from AFK or
an AFK action is performed. This option does not affect broadcasts sent to players with specific permissions.
Affected language keys are:
- `notification.afk_broadcast`
- `notification.return_broadcast`
- `notification.afk_kick_broadcast`
- `notification.afk_disconnect_broadcast`
Default is `true`.

#### Warning Delay (`warning-delay`)
This is the delay in seconds after which a warning message is sent to the player after their last activity. The warning message  
can be customized in the language file under the key `notification.afk_warning`.  

Default is `90` seconds.

#### AFK Delay (`afk-delay`)
This is the delay in seconds after which a player is considered AFK if they have not interacted with the server.  
The AFK message can be customized in the language file under the key `notification.afk.<action>`.  

Default is `180` seconds.

#### Action Delay (`action-delay`)
This is the delay between a player's last interaction and the Action as configured in the `action` entry.  

Default is `420` seconds.

#### Action (`action`)
This setting allows you to specify the action that will be taken when Action Delay is reached.  
You can choose from the following options:
- `kick`: The player will be kicked from the server.
- `connect`: The player will be connected to a specified afk-server.
- `teleport`: The player will be teleported to a specified afk-location.
- `nothing`: No action will be taken.

Default is `kick`.

#### AFK Server (`afk-server-name`)
This is the name of the server to which players will be connected when the action is set to `connect`.  
Make sure this server is enabled and present in your proxy's config.

#### Action Bar Messages (`actionbar`)
Controls whether players receive action bar notifications while AFK handling runs.
Affected language keys are `actionbar.afk` and `actionbar.afk_moved`.
Default is `true`.

#### AFK Location (`afk-location`)
This is the location to which players will be teleported when the action is set to `teleport`.

#### Allow Bypass (`allow-bypass`)
If true, players with the permission `afk.bypass` bypass the AFK system.  

Default is `true`.

#### Disabled Servers (`disabled-servers`)
This is a list of servers where the AFK system is disabled. Players on these servers will not be marked as AFK.  
You can specify multiple servers by separating them with commas.

Example:
```yaml  
disabled-servers: [lobby, survival]  
```  

#### Broadcast Strategy (`broadcast-strategy`)
This setting determines how AFK notifications are broadcasted to other players.  

You can choose from the following options:
- `ALL`: Notifications are sent to all players on the proxy and all servers.
- `GLOBAL`: Notifications are sent to all players on the proxy, except for the player who has gone AFK.
- `PER_SERVER`: Notifications are sent only to players on the same server as the AFK player, except for the player who has gone AFK.
- `DISABLE`: No notifications are sent.

This option affects the following language keys: `notification.afk_broadcast`, `notification.return_broadcast`, `notification.afk_kick_broadcast`, `notification.afk_disconnect_broadcast`.

Default is `PER_SERVER`.

#### AFK Command cooldown (`afk-command-cooldown`)
This is the cooldown in seconds for the `/afk` command to prevent spamming.  
Default is `10` seconds.

#### Bypass Regions (`bypass-regions`)
This is a list of regions where players will not be marked as AFK.  
You can, but should not specify regions manually here, but rather use the `/afk region add` command to add regions.

### Auto Clicker Detection Settings
#### Enable Auto Clicker Detection (`auto-clicker.enabled`)
If true, auto clicker detection is enabled.  

Default is `false`.

#### Allow Auto Clicker Bypass (`auto-clicker.allow-bypass`)
If true, players with the permission `bungeeafk.auto-clicker.bypass` bypass the auto clicker detection.  

Default is `true`.

#### Notify Permission (`auto-clicker.notify-permission`)
This is the permission required to receive notifications about auto clicker detections.  
Default is `bungeeafk.auto-clicker.notify`.

#### Notify detected Player (`auto-clicker.notify-player`)
If true, players will receive a notification when they are detected as using an auto clicker.  

Default is `true`.

#### Action on Detection (`auto-clicker.action`)
This setting allows you to specify the action that will be taken when an auto clicker is detected.  
You can choose from the following options:
- `kick`: The player will be kicked from the server.
- `open-inv`: An empty inventory will be opened for the player to prevent clicks from impacting the world.
- `nothing`: No action will be taken.

#### Disabled Servers for Auto Clicker Detection (`auto-clicker.disabled-servers`)
This is a list of servers where auto clicker detection is disabled.  
Players on these servers will not be checked for auto clicker usage.

Example:
```yaml  
auto-clicker:  
  disabled-servers: [lobby, survival]  
```  

### Movement Pattern Detection Settings
#### Enable Movement Pattern Detection (`movement-pattern.enabled`)
If true, movement pattern detection is enabled.  

Default is `true`.

#### Allow Movement Pattern Bypass (`movement-pattern.allow-bypass`)
If true, players with the permission `bungeeafk.movement-pattern.bypass` bypass the movement pattern detection.  

Default is `true`.

#### Notify Permission (`movement-pattern.notify-permission`)
This is the permission required to receive notifications about movement pattern detections.  

Default is `bungeeafk.movement-pattern.notify`.

#### Notify detected Player (`movement-pattern.notify-player`)
If true, players will receive a notification when they are detected as using a movement pattern.  
Default is `true`.

#### Action on Detection (`movement-pattern.action`)
This setting allows you to specify the action that will be taken when a movement pattern is detected.  
You can choose from the following options:
- `kick`: The player will be kicked from the server.
- `connect`: The player will be connected to a specified afk-server.
- `teleport`: The player will be teleported to a specified afk-location.
- `nothing`: No action will be taken.

#### Disabled Servers for Movement Pattern Detection (`movement-pattern.disabled-servers`)
This is a list of servers where movement pattern detection is disabled.  
Players on these servers will not be checked for movement patterns.  
Example:
```yaml  
movement-pattern:  
  disabled-servers: [lobby, survival]  
```  

#### Clear After (`movement-pattern.clear-after`)
Time after which an individual movement is cleared and therefore cannot be used for analyzing patterns.
This is to prevent high memory usage and to ensure that only recent movements are considered.
If set to 600, movements older than 10 minutes will not be considered for pattern detection.
If set to 0, movements will never be cleared and will be kept in memory indefinitely.

Setting the value to 0 may lead to high memory usage if many players are online.
and they move frequently, so it is recommended to set this to a reasonable value
depending on your server's activity and player base size.
A value of 600 (10 minutes) is a good balance for most servers.

#### Required Certainty (`movement-pattern.certainty-threshold`)
This is the minimum certainty required for a movement pattern to be considered suspicious.  
The value should be between `0` and `1`, where `1` means 100% certainty.  
Certainty is calculated using the standard deviation of intervals between visiting the same block.

The formula used is: `Math.max(0, 1 - (stdDev / 1000.0))`

Meaning that a value of `0.9` is reached when the standard deviation is `100` milliseconds.

Default is `0.9`.

#### Sample Size (`movement-pattern.sample-size`)
This is the number of required samples to detect a movement pattern.  
Example: If set to `5`, the player must visit the same block at least `5` times before the movement pattern can be analyzed.  

Default is `5`.