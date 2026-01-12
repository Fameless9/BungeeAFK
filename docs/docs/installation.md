# Installation

You can install BungeeAFK either on a server network like BungeeCord or Velocity, or on a single server.
Before installing, check [Compatibility](compatibility.md) to ensure your server version is supported.

## Prerequisites

- Java 21
- A setup server network or a single server that is compatible with BungeeAFK (see [Compatibility](compatibility.md))

## Installation Steps

1. **Download BungeeAFK**: Get the latest version of BungeeAFK from
   the [latest releases page](https://github.com/Fameless9/BungeeAFK/releases/latest).
2. **Download BungeeAFK-Tracking**: In order for BungeeAFK to work, you need to download the BungeeAFK-Tracking plugin
   from the [latest releases page](https://github.com/Fameless9/BungeeAFK/releases/latest) as well.
3. **Place the Plugins**:
    - Place the tracking plugin in the `plugins` folder of **every** subserver in your network
    - Place the BungeeAFK plugin in the main `plugins` folder of your proxy server.
4. **Configure the Plugins** (optional):
    - Open the `config.yml` file in the BungeeAFK plugin folder.
    - Adjust the settings according to your preferences. For detailed configuration options, refer to
      the [Configuration](configuration.md) documentation.
5. **Restart Your Server**: Restart your proxy server to load the plugins.

After installing, update BungeeAFK regularly to benefit from new features and improvements.
See [Updating BungeeAFK](updating.md) for more information on how to update the plugin.