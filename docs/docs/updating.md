# Updating BungeeAFK

BungeeAFK regularly receives updates. The plugin automatically checks for new updates once per day. If an update is
available,
you will receive a notification in the console. To update, follow the same steps and
in [installation steps](installation.md#installation-steps). After updating, some features
may require additional configuration. After starting the server, run `/bafk configuration saveconfig` to save the
current configuration to the `config.yml` file.
This will also save all new configuration options that were added in the new version together with their default values
and comments.
From there, you can adjust the settings as needed and run `/bafk configuration reloadconfig` to apply the changes
without restarting the server
(See [Configuration](configuration.md) for more details).