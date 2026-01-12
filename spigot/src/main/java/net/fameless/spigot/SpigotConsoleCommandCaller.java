package net.fameless.spigot;

import net.fameless.core.command.framework.ConsoleCommandCaller;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class SpigotConsoleCommandCaller extends ConsoleCommandCaller {

    private static SpigotConsoleCommandCaller instance;

    public static SpigotConsoleCommandCaller get() {
        if (instance == null) {
            instance = new SpigotConsoleCommandCaller();
        }
        return instance;
    }

    public void sendMessage(@Nullable Component component) {
        if (component == null) return;
        SpigotUtil.BUKKIT_AUDIENCES.console().sendMessage(component);
    }
}
