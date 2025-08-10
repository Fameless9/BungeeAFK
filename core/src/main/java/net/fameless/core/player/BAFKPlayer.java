package net.fameless.core.player;

import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.command.framework.CommandCaller;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.event.EventDispatcher;
import net.fameless.core.event.PlayerAFKStateChangeEvent;
import net.fameless.core.handling.AFKState;
import net.fameless.core.location.Location;
import net.fameless.core.region.Region;
import net.fameless.core.util.PlayerFilters;
import net.fameless.core.util.MessageBroadcaster;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BAFKPlayer<PlatformPlayer> implements CommandCaller {

    protected static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/Player");
    public static final List<BAFKPlayer<?>> PLAYERS = new CopyOnWriteArrayList<>();

    protected String name;
    private final UUID uuid;
    private long timeSinceLastAction = 0;
    private AFKState afkState = AFKState.ACTIVE;
    private GameMode gameMode = GameMode.SURVIVAL;
    private Location location = new Location("world", 0, 0, 0, 0, 0);

    public BAFKPlayer(UUID uuid) {
        if (of(uuid).isPresent()) {
            throw new IllegalArgumentException("A player with this UUID already exists: " + uuid);
        }
        this.uuid = uuid;
        PLAYERS.add(this);
    }

    public static @NotNull Optional<BAFKPlayer<?>> of(String name) {
        for (BAFKPlayer<?> bafkPlayer : PLAYERS) {
            if (bafkPlayer.getName().equalsIgnoreCase(name)) {
                return Optional.of(bafkPlayer);
            }
        }
        return Optional.empty();
    }

    public static @NotNull Optional<BAFKPlayer<?>> of(UUID uuid) {
        for (BAFKPlayer<?> player : PLAYERS) {
            if (player.getUniqueId().equals(uuid)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public long getTimeSinceLastAction() {
        return timeSinceLastAction;
    }

    public void setTimeSinceLastAction(long timeSinceLastAction) {
        this.timeSinceLastAction = timeSinceLastAction;
    }

    public void increaseTimeSinceLastAction(long increment) {
        if (afkState == AFKState.BYPASS) return;
        this.timeSinceLastAction += increment;
    }

    public AFKState getAfkState() {
        if ((PluginConfig.get().getBoolean("allow-bypass") && hasPermission("bungeeafk.bypass")) ||
                PluginConfig.get().getStringList("disabled-servers").contains(getCurrentServerName()) ||
                Region.isLocationInAnyBypassRegion(location)
        ) {
            return AFKState.BYPASS;
        }
        return afkState;
    }

    public void setAfkState(AFKState afkState) {
        if (this.afkState == afkState) return;

        PlayerAFKStateChangeEvent event = new PlayerAFKStateChangeEvent(this, this.afkState, afkState);
        EventDispatcher.post(event);

        if (event.getNewState() == this.afkState) return;

        if ((this.afkState == AFKState.AFK || this.afkState == AFKState.ACTION_TAKEN) && event.getNewState() == AFKState.ACTIVE) {
            sendMessage(Caption.of("notification.afk_return"));
            MessageBroadcaster.broadcastMessageToFiltered(
                    Caption.of("notification.afk_return_broadcast",
                            TagResolver.resolver("player", Tag.inserting(Component.text(getName())))),
                    PlayerFilters.notMatching(this)
            );
        }
        this.afkState = event.getNewState();
    }

    public void sendMessage(Component message) {
        getAudience().sendMessage(message);
    }

    public void sendActionbar(Component message) {
        getAudience().sendActionBar(message);
    }

    public void playSound(Sound sound) {
        getAudience().playSound(sound);
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        if (!this.location.equalsBlock(location)) {
            BungeeAFK.getMovementPatternDetection().registerMovement(this, location);
        }
        this.location = location;
    }


    public abstract String getName();

    public abstract Audience getAudience();

    public abstract Optional<PlatformPlayer> getPlatformPlayer();

    public abstract boolean isOffline();

    public abstract void connect(String serverName);

    public abstract void kick(Component reason);

    public abstract boolean hasPermission(String permission);

    public abstract String getCurrentServerName();

    public abstract void updateGameMode(GameMode gameMode);

    public abstract void teleport(Location location);

    public abstract void openEmptyInventory();
}
