The plugin comes with a PlaceholderAPI expansion and TAB integration. Below is a list of available placeholders:

| Placeholder           | Description                                                       |
|-----------------------|-------------------------------------------------------------------|
| `%bafk_user_afk%`     | Displays the afk state of a player `true` if AFK, `false` if not. |
| `%bafk_afk_users%`    | Displays a list of all AFK-players on the current server          |
| `%bafk_active_users%` | Displays a list of all non-AFK players on the current server      |

### Using the Placeholders in PlaceholderAPI

To use these placeholders, ensure that you have PlaceholderAPI installed on your server.
You can then incorporate these placeholders into your chat formats, scoreboard displays, or any other compatible plugins
that support PlaceholderAPI.

To install PlaceholderAPI, have a look at the official [PlaceholderAPI Wiki](https://wiki.placeholderapi.com/)

### Using the Placeholders in TAB

If you are using the TAB plugin, you can also utilize these placeholders in your TAB configurations:

In your TAB config.yml, add:

```yaml
placeholder-output-replacements:
  "%bafk_user_afk%":
    true: "&7[AFK] &r"
    false: ""
```

In this example, if the placeholder returns `true`, it will be replaced with "[AFK]" in gray color, otherwise, it will
be replaced with an empty string.
You can now use the placeholder `%bafk_user_afk%` in your TAB configurations to show the AFK status of players in the
nametags and the tablist.