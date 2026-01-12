BungeeAFK supports auto-clicker detection to prevent players from using auto-clickers or similar tools to bypass AFK
detection.
This feature is useful for maintaining fair gameplay and ensuring that players are genuinely active.

### Limitations

Auto-clicker detection is **disabled** by default and must be enabled manually, as false positives, while being
unlikely, can still occur.
BungeeAFK cannot distinguish between a player holding down a button and an auto-clicker, as it is limited to monitoring
player
interactions and not actual clicks. Therefore, if a player is holding down a button for long enough, it may trigger the
auto-clicker detection system.
It is recommended to enable this feature only if you are aware of the potential for false positives and are willing to
handle them.

### Configuring Auto-Clicker Detection

See [Configuring Auto-Clicker Detection](configuration.md#auto-clicker-detection-settings) for more information on how
to configure the detection.
See also [Auto-Clicker Commands](commands.md#auto-clicker-detection-commands) for commands to interact with auto-clicker
detection.

### How it works

BungeeAFK uses a system that is very proof against false positives, but can still detect auto-clickers and similar
tools.
It works by monitoring the standard deviation of the time between player interactions over the last 150 interactions in
each window.
If the standard deviation is below a certain threshold, it is assumed that the player is using an auto-clicker or
similar tool.
This threshold can be configured in the `config.yml` file under the `auto-clicker` section.
The default threshold is `10ms`. Lower threshold = false positives are less likely, but it is more difficult to detect
actual auto-clickers.

In order for the player to be flagged as using an auto-clicker, the threshold must be met in 3 windows of 150
interactions in a row.
This means that the player must be clicking very consistently for at least 450 interactions in a row to be flagged.
This is to prevent false positives from players who are just very (very) consistent at clicking.
If a player is flagged as using an auto-clicker, the action configured in the `config.yml` file under the `auto-clicker`
section will be executed.
See [Configuring Auto-Clicker Detection](configuration.md#auto-clicker-detection-settings) for more information on how
to configure the detection.