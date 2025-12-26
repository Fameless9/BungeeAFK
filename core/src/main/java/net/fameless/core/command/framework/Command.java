package net.fameless.core.command.framework;

import net.fameless.core.caption.Caption;
import net.fameless.core.command.AFK;
import net.fameless.core.command.MainCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Command {

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/" + Command.class.getSimpleName());
    private static final List<Command> COMMANDS = new ArrayList<>();
    public final String id;
    public final List<String> aliases;
    public final CallerType requiredType;
    public final String usage;
    public final String permission;

    public Command(
            String id, List<String> aliases, CallerType requiredType,
            String usage, String permission
    ) {
        this.id = id;
        this.aliases = aliases;
        this.requiredType = requiredType;
        this.usage = usage;
        this.permission = permission;

        COMMANDS.add(this);
    }

    public static @NotNull Optional<Command> getCommandById(String commandId) {
        for (Command command : COMMANDS) {
            if (command.matches(commandId)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    public static void execute(String commandId, CommandCaller caller, String[] args) {
        @NotNull Optional<Command> commandOptional = getCommandById(commandId);
        commandOptional.ifPresentOrElse(
                command -> command.execute(caller, args),
                () -> logger.error("Error while trying to execute command: {}. Command not registered.", commandId)
        );
    }

    public static @NotNull @Unmodifiable List<String> tabComplete(String commandId, CommandCaller caller, String[] args) {
        @NotNull Optional<Command> commandOptional = getCommandById(commandId);
        if (commandOptional.isPresent()) return commandOptional.get().getTabCompletions(caller, args);
        logger.error("Error while trying to get tab-completions for command: {}. Command not registered.", commandId);
        return List.of();
    }

    public static void init() {
        logger.info("Initializing commands...");
        new MainCommand();
        new AFK();
    }

    public String getPermission() {
        return permission;
    }

    public boolean cannotExecute(CommandCaller caller, boolean message) {
        if (caller == null) {
            return false;
        }
        if (!this.requiredType.allows(caller)) {
            if (message) {
                caller.sendMessage(Caption.of(this.requiredType.getErrorMessageKey()));
            }
        } else if (!caller.hasPermission(getPermission())) {
            if (message) {
                caller.sendMessage(Caption.of(
                        "permission.no_permission",
                        TagResolver.resolver("permission", Tag.inserting(Component.text(this.permission)))
                ));
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean matches(String commandId) {
        return this.id.equalsIgnoreCase(commandId) || this.aliases.contains(commandId.toLowerCase());
    }

    public void sendUsage(@NotNull CommandCaller caller) {
        caller.sendMessage(Caption.of(
                "command.usage",
                TagResolver.resolver("usage", Tag.inserting(Component.text(this.usage)))
        ));
    }

    protected void execute(CommandCaller caller, String @NotNull [] args) {
        if (cannotExecute(caller, true)) {
            return;
        }
        executeCommand(caller, args);
    }

    protected List<String> getTabCompletions(CommandCaller caller, String @NotNull [] args) {
        if (args.length < 1) {
            return List.of();
        }
        if (cannotExecute(caller, false)) {
            return List.of();
        }
        return tabComplete(caller, args);
    }

    protected abstract void executeCommand(CommandCaller caller, String[] args);

    protected abstract List<String> tabComplete(CommandCaller caller, String[] args);

}
