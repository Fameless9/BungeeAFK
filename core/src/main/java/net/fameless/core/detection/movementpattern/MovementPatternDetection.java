package net.fameless.core.detection.movementpattern;

import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.detection.history.Detection;
import net.fameless.core.detection.history.DetectionType;
import net.fameless.core.event.EventDispatcher;
import net.fameless.core.event.PlayerMovementPatternDetectedEvent;
import net.fameless.core.handling.AFKState;
import net.fameless.core.location.Location;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.util.DetectionUtil;
import net.fameless.core.util.MessageBroadcaster;
import net.fameless.core.util.PlayerFilters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MovementPatternDetection {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + MovementPatternDetection.class.getSimpleName());

    private final Map<BAFKPlayer<?>, Map<Location, Deque<Long>>> playerMovementHistory = new ConcurrentHashMap<>();

    private final Consumer<BAFKPlayer<?>> defaultActionOnDetection;
    private final long cutoffTime = 10 * 60 * 1000; // Time after which an entry should be removed - 10 minutes in milliseconds
    private double certaintyThreshold;
    private int sampleSize;
    private List<String> disabledServers;
    boolean allowBypass;
    boolean enabled;

    public MovementPatternDetection() {
        if (BungeeAFK.getMovementPatternDetection() != null) {
            throw new IllegalStateException("You may not create another instance of MovementPatternDetection!");
        }
        LOGGER.info("Initializing MovementPatternDetection...");

        reloadConfigValues();
        defaultActionOnDetection = player -> {
            ActionOnDetection action;
            try {
                action = ActionOnDetection.fromIdentifier(PluginConfig.get().getString("movement-pattern.action", "kick"));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid action on movement pattern detection, defaulting to kick", e);
                action = ActionOnDetection.KICK;
            }

            switch (action) {
                case KICK -> BungeeAFK.getAFKHandler().handleKickAction(player, Caption.of("notification.movement_pattern_kick_message"),
                       Component.empty());
                case CONNECT -> BungeeAFK.getAFKHandler().handleConnectAction(player, Caption.of("notification.movement_pattern_kick_message"), Component.empty(),
                        Caption.of("notification.movement_pattern_connect_message"), Component.empty());
                case TELEPORT -> BungeeAFK.getAFKHandler().handleTeleportAction(player, Caption.of("notification.movement_pattern_teleport_message"));
            }
            player.setAfkState(AFKState.ACTION_TAKEN);
        };
    }

    public void reloadConfigValues() {
        this.certaintyThreshold = PluginConfig.get().getDouble("movement-pattern.certainty-threshold", 0.9);
        this.sampleSize = PluginConfig.get().getInt("movement-pattern.sample-size", 5);
        this.disabledServers = PluginConfig.get().getStringList("movement-pattern.disabled-servers");
        this.allowBypass = PluginConfig.get().getBoolean("movement-pattern.allow-bypass", true);
        this.enabled = PluginConfig.get().getBoolean("movement-pattern.enabled", true);
    }

    public void registerMovement(@NotNull BAFKPlayer<?> player, Location location) {
        if (!enabled) return;
        if (disabledServers.contains(player.getCurrentServerName())) return;
        if (allowBypass && player.hasPermission("bungeeafk.movement-pattern.bypass")) return;

        location = location.getBlockLocation();
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = playerMovementHistory
                .computeIfAbsent(player, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(location, k -> new ArrayDeque<>());

        // Remove timestamps older than 10 minutes
        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoffTime) {
            timestamps.pollFirst();
        }

        // If we have more than sampleSize samples, remove the oldest ones
        while (timestamps.size() >= sampleSize) {
            timestamps.pollFirst();
        }

        timestamps.addLast(now);

        // if we have enough samples, analyze the movement pattern
        if (timestamps.size() >= sampleSize) {
            List<Long> intervals = new ArrayList<>(sampleSize - 1);
            Iterator<Long> iterator = timestamps.iterator();
            long prev = iterator.next();
            while (iterator.hasNext()) {
                long current = iterator.next();
                intervals.add(current - prev);
                prev = current;
            }

            double stdDev = DetectionUtil.calculateStdDev(intervals);

            // Calculate certainty based on standard deviation
            // stddev > 1000ms -> certainty = 0
            // stddev = 0ms -> certainty = 1
            double certainty = Math.max(0, 1 - (stdDev / 1000.0));

            if (certainty > certaintyThreshold) {
                patternDetected(player, location, stdDev, certainty);
            }
        }
    }

    private void patternDetected(@NotNull BAFKPlayer<?> player, Location location, double stdDev, double certainty) {
        LOGGER.info("Suspicious Movement Pattern Detected: Player {} @ {} | stdDev={} | certainty={}",
                player.getName(), location, stdDev, String.format("%.2f", certainty * 100) + "%");

        playerMovementHistory.get(player).clear();
        new Detection(DetectionType.MOVEMENT_PATTERN, System.currentTimeMillis(),
                player.getCurrentServerName(), player.getName());

        MessageBroadcaster.broadcastMessageToFiltered(Caption.of("notification.movement_pattern_detected_admin",
                        TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))),
                PlayerFilters.hasPermission(PluginConfig.get().getString("movement-pattern.notify-permission", "bungeeafk.movement-pattern.notify")),
                PlayerFilters.notMatching(player));

        PlayerMovementPatternDetectedEvent event = new PlayerMovementPatternDetectedEvent(player, defaultActionOnDetection);
        EventDispatcher.post(event);
        event.getAction().accept(player);
    }
}
