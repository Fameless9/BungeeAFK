package net.fameless.core.command.framework;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface CommandCaller {

    CallerType callerType();

    String getName();

    void sendMessage(@Nullable Component component);

    boolean hasPermission(String permission);

}
