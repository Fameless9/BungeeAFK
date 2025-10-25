[![CodeFactor](https://www.codefactor.io/repository/github/fameless9/bungeeafk/badge)](https://www.codefactor.io/repository/github/fameless9/bungeeafk)
![GitHub Release](https://img.shields.io/github/v/release/fameless9/bungeeafk)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/fameless9/bungeeafk/total)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/fameless9/bungeeafk)
![bStats Servers](https://img.shields.io/bstats/servers/25577)
![Spiget Rating](https://img.shields.io/spiget/rating/124327)
![GitHub License](https://img.shields.io/github/license/fameless9/bungeeafk)

# BungeeAFK

BungeeAFK is a cross-platform plugin for **BungeeCord**, **Velocity**, and **Spigot/Paper** servers that detects inactive (AFK) players. If a player is idle for a configurable amount of time, the plugin can either **kick them**, **teleport them** or **move them to a separate AFK server** within your Bungee network.

### [Official Wiki](https://fameless9.github.io/BungeeAFK/)

## ✨ Features

- ⚙️ Fully configurable timeout and action
- 🔀 Support for **kick**, **teleport** or **move-to-server** actions
- 💤 Built-in Auto-Clicker and AFK-Machine detection
- 🌐 Compatible with BungeeCord, Velocity and Spigot, as well as their forks.
- 📦 Lightweight and easy to install
- 🔗 Detects AFK via movement, chat, interaction, etc.

> Note: The **"move" action is not available on Spigot-only setups** — it requires a Bungee/Velocity network.

---

## 📦 Installation

### 1. Main Plugin

Install `BungeeAFK`:
- put the BungeeAFK-Bungee, Velocity or Spigot version in the plugins folder of the network/main server

### 2. Tracking Plugin (Required on subservers)
- to detect player activity accurately, **every subserver in the network must also have**:

```
BungeeAFK-Tracking
```

- Download `BungeeAFK-Tracking`
- Place it in the `plugins` folder of each Spigot/Paper server
- This allows BungeeAFK to track movement, chat, interaction, etc.

Find detailed instructions on the [Installation Guide](https://fameless9.github.io/BungeeAFK/installation/)

---

## 🛠️ Configuration

BungeeAFK comes with **a lot** of customization options. For a detailed overview, visit the [Configuration Guide](https://fameless9.github.io/BungeeAFK/configuration/).  
It also supports **customizable messages**. For that, visit [Custom Messages](https://fameless9.github.io/BungeeAFK/custom_messages/)

---

## 🚫 Limitations

- The **connect** feature only works on **networks (BungeeCord, etc.)** not on standalone spigot servers.
- You **must install** `BungeeAFK-Tracking` on **all subservers** managed by the BungeeCord/Velovity network for AFK detection to work properly.

---

## 🔗 Compatibility

| Platform     | Supported |
|--------------|-----------|
| BungeeCord   | ✅        |
| Velocity     | ✅        |
| Spigot       | ✅        |
| Paper        | ✅        |
| Purpur       | ✅        |
| Waterfall    | ✅        |

---

## 📣 Commands
You can configure the plugin using the `/bungeeafk` or `/bafk` command.

- `/bafk configure <action | action-delay | afk-delay | allow-bypass | warn-delay | afk-location | caption | reloadconfig> <value>`
- `/bafk lang <en | de | reload>`

---

## 📥 Downloads

Download from the [Latest Release](https://github.com/Fameless9/BungeeAFK/releases/latest).

---

## 🧠 License

Licensed under General Public License v3.0. See [LICENSE](./LICENSE) for details.
