package net.fameless.core.command;

import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.command.framework.CallerType;
import net.fameless.core.command.framework.Command;
import net.fameless.core.command.framework.CommandCaller;
import net.fameless.core.config.Config;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.AFKState;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.util.cache.ExpirableMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AFK extends Command {

    private final AFKHandler afkHandler;
    private final ExpirableMap<BAFKPlayer<?>, Long> cooldownCache = new ExpirableMap<>();

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
        if (player.getAfkState().equals(AFKState.BYPASS)) {
            player.sendMessage(Caption.of("command.afk_bypass"));
            return;
        }

        Long cooldownExpiry = cooldownCache.get(player);
        if (cooldownExpiry != null) {
            long timeLeft = (cooldownExpiry - System.currentTimeMillis()) / 1000L;
            player.sendMessage(Caption.of("command.afk_cooldown", TagResolver.resolver("time", Tag.inserting(Component.text(timeLeft)))));
            return;
        }

        if (player.getAfkState().equals(AFKState.ACTIVE)) {
            player.setAfkState(AFKState.WARNED);
            player.setTimeSinceLastAction(afkHandler.getAfkDelayMillis());
        } else {
            player.setActive();
        }

        long cooldown = Config.getInstance().getInt("afk-command-cooldown", 30);
        cooldownCache.addOrRefresh(player, System.currentTimeMillis() + cooldown * 1000L, cooldown, TimeUnit.SECONDS);
    }

    @Override
    protected List<String> tabComplete(CommandCaller caller, String[] args) {
        return List.of();
    }
}
