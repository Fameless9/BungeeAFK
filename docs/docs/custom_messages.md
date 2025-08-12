BungeeAFK supports English(`en`) and German(`de`) by default.

The language files are located at `plugins/BungeeAFK/lang/lang_xx.json` after the plugin's first startup.
The language files must not be renamed or moved, as the plugin expects them to be in this specific location.
Otherwise, the plugin will load a copy of the default language file.
You can adjust all messages displayed by the plugin in this file, but you need to make sure that all the keys remain the same.  
For example, you can change the AFK warning message by modifying the value for `notification.afk_warning` key in the language file,
but the key itself must not be changed.
```json  
{  
  "notification.afk_warning": "Your desired message here"
}  
```  
Creating a new language file is **currently not supported**. However, you can modify the existing language files to add your own translations or change the messages as needed.

If a key is missing in the language file, the plugin will use the default message.