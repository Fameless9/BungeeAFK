package net.fameless.core.command.framework;

import net.kyori.adventure.text.Component;

public abstract class ConsoleCommandCaller implements CommandCaller {

    @Override
    public CallerType callerType() {
        return CallerType.CONSOLE;
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public abstract void sendMessage(Component component);
}
