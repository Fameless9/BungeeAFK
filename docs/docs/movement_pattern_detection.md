BungeeAFK supports Movement Pattern Detection to identify players who use AFK-Machines or similar tools to bypass AFK
detection.
This feature is useful for maintaining fair gameplay and ensuring that players are genuinely active.

### Limitations

Movement Pattern Detection only uses the player's movement data of the last 10 minutes to detect patterns. This
value is configurable.
See [Configuring Movement Pattern Detection](configuration.md#movement-pattern-detection-settings) for more information.

### Configuring Movement Pattern Detection

See [Configuring Movement Pattern Detection](configuration.md#movement-pattern-detection-settings) for more information
on how to configure the detection.
See also [Movement Pattern Commands](commands.md#movement-pattern-detection-commands) for commands to interact with
Movement Pattern Detection.

### How it works

BungeeAFK analyzes player movement after visiting the same block `5` times in a row.
From there, it calculates the certainty of the player using an AFK-Machine or similar tool, by calculating the standard
deviation of the intervals between visiting the same block.
If the standard deviation is below a certain threshold, it is assumed that the player is using an AFK-Machine or similar
tool.
This threshold can be configured in the `config.yml` file under the `movement-pattern` section.
The default threshold is `0.9` (90% certainty), which means that the standard deviation must be below `100ms` to be
considered suspicious.
Lower threshold = false positives are less likely, but it is more difficult to detect actual AFK-Machines.
If a player is detected as using an AFK-Machine or similar tool, the action configured in the `config.yml` file under
the `movement-pattern` section will be executed.
See [Configuring Movement Pattern Detection](configuration.md#movement-pattern-detection-settings) for more information
on how to configure the detection.