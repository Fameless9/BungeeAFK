package net.fameless.core.command;

import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.command.framework.CallerType;
import net.fameless.core.command.framework.Command;
import net.fameless.core.command.framework.CommandCaller;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.AFKState;
import net.fameless.core.player.BAFKPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

public class AFK extends Command {

    private final AFKHandler afkHandler;

    public AFK() {
        super(
                "afk",
                List.of(),
                CallerType.PLAYER,
                "/afk",
                "bungeeafk.afk"
        );
        this.afkHandler = BungeeAFK.getAFKHandler();
    }

    @Override
    protected void executeCommand(CommandCaller caller, String[] args) {
        BAFKPlayer<?> player = (BAFKPlayer<?>) caller;
        switch (player.getAfkState()) {
            case BYPASS -> player.sendMessage(Caption.of("command.afk_bypass"));
            case ACTIVE -> {
                long currentTime = System.currentTimeMillis();
                if (player.getAfkCommandCooldown() > currentTime) {
                    long timeLeft = (player.getAfkCommandCooldown() - currentTime) / 1000L;
                    TagResolver time = TagResolver.resolver("time", Tag.inserting(Component.text(timeLeft)));
                    player.sendMessage(Caption.of("command.afk_cooldown", time));
                    return;
                }

                player.setAfkState(AFKState.WARNED);
                player.setTimeSinceLastAction(afkHandler.getAfkDelayMillis());
                long cooldown = PluginConfig.get().getInt("afk-command-cooldown", 30) * 1000L;
                player.setAfkCommandCooldown(currentTime + cooldown);
            }
            default -> {
                long currentTime = System.currentTimeMillis();
                if (player.getAfkCommandCooldown() > currentTime) {
                    long timeLeft = (player.getAfkCommandCooldown() - currentTime) / 1000L;
                    TagResolver time = TagResolver.resolver("time", Tag.inserting(Component.text(timeLeft)));
                    player.sendMessage(Caption.of("command.afk_cooldown", time));
                    return;
                }

                player.setTimeSinceLastAction(0);
                player.setAfkState(AFKState.ACTIVE);
                afkHandler.handleAction(player);
                long cooldown = PluginConfig.get().getInt("afk-command-cooldown", 30) * 1000L;
                player.setAfkCommandCooldown(currentTime + cooldown);
            }
        }
    }

    @Override
    protected List<String> tabComplete(CommandCaller caller, String[] args) {
        return List.of();
    }
}
