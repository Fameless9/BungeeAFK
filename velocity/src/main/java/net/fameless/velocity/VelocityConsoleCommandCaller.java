package net.fameless.velocity;

import net.fameless.core.command.framework.ConsoleCommandCaller;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class VelocityConsoleCommandCaller extends ConsoleCommandCaller {

    private static VelocityConsoleCommandCaller instance;

    public static VelocityConsoleCommandCaller get() {
        if (instance == null) {
            instance = new VelocityConsoleCommandCaller();
        }
        return instance;
    }

    public void sendMessage(@Nullable Component component) {
        if (component == null) return;
        VelocityPlatform.getProxy().getConsoleCommandSource().sendMessage(component);
    }
}
