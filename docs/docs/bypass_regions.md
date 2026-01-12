BungeeAFK supports bypass-regions, allowing players to be AFK in specific regions without being detected as AFK by the
plugin.
This feature is useful for admins who want to allow players to be AFK in certain areas, such as event areas, but not in
others.

### Setup Bypass Regions

To add a new bypass region, you can use the `/bungeeafk region add <name> <world> <x1> <y1> <z1> <x2> <y2> <z2>`
command.
See [Region Commands](commands.md#region-commands) for more information on how to interact with bypass regions.

You can also configure bypass regions in the `config.yml` file under the `bypass-regions` section, but this is not
recommended unless you are
familiar with the configuration format. After adding a bypass region, you can reload the regions using the
`/bungeeafk region reload` command.
This command will reload all bypass regions from the `config.yml` file, overwriting any existing regions that were not
saved!
