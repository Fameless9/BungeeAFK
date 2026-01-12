For any version higher than _2.6.0_, your can create your own language files by copying one of the default
language files located in the `plugins/BungeeAFK/lang/` directory and renaming it to `lang_xx.json`, where
`xx` is your desired language identifier. You can then edit the messages inside the language file and the
plugin will automatically load the custom language file on startup or after running `/bafk lang reload`.
To set the plugin language to your custom language, use the `/bafk lang <language_identifier>` command.

Custom language files must meet the following requirements in order to be loaded correctly by the plugin:

* The file must be named `lang_xx.json`, where `xx` is your desired language identifier.
* The identifier must not be blank and must not contain spaces
* The identifier must not conflict with existing language identifiers used by the plugin (e.g., `en`, `de`).
* The file must be placed in the `plugins/BungeeAFK/lang/` directory.
* **The file must contain all the same keys as the default language files**, with translated values.
* The file must be a valid JSON file.

If the plugin fails to load your custom language file, it will log an error message to the console and
fall back to the default language (English). Make sure to check the console for any error messages
if you encounter issues with your custom language file.