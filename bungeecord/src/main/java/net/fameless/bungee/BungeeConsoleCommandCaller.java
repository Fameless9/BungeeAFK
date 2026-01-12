package net.fameless.bungee;

import net.fameless.core.command.framework.ConsoleCommandCaller;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class BungeeConsoleCommandCaller extends ConsoleCommandCaller {

    private static BungeeConsoleCommandCaller instance;

    public static BungeeConsoleCommandCaller get() {
        if (instance == null) {
            instance = new BungeeConsoleCommandCaller();
        }
        return instance;
    }

    public void sendMessage(@Nullable Component component) {
        if (component == null) return;
        BungeeUtil.BUNGEE_AUDIENCES.console().sendMessage(component);
    }
}
