package net.fameless.core.detection.movementpattern;

import net.fameless.api.event.EventDispatcher;
import net.fameless.api.event.PlayerMovementPatternDetectedEvent;
import net.fameless.core.BungeeAFK;
import net.fameless.core.adapter.APIAdapter;
import net.fameless.core.caption.Caption;
import net.fameless.core.config.Config;
import net.fameless.core.detection.history.Detection;
import net.fameless.core.detection.history.DetectionType;
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

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/" + MovementPatternDetection.class.getSimpleName());

    private final Map<BAFKPlayer<?>, Map<Location, Deque<Long>>> playerMovementHistory = new ConcurrentHashMap<>();

    private final Consumer<BAFKPlayer<?>> defaultActionOnDetection;
    private double certaintyThreshold;
    private int sampleSize;
    private List<String> disabledServers;
    boolean allowBypass;
    boolean enabled;
    int clearAfterSeconds;

    public MovementPatternDetection() {
        if (BungeeAFK.getMovementPatternDetection() != null) {
            throw new IllegalStateException("You may not create another instance of MovementPatternDetection!");
        }
        logger.info("Initializing MovementPatternDetection...");

        reloadConfigValues();
        defaultActionOnDetection = player -> {
            ActionOnDetection action;
            try {
                action = ActionOnDetection.fromIdentifier(Config.getInstance().getString("movement-pattern.action", "kick"));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid action on movement pattern detection, defaulting to kick", e);
                action = ActionOnDetection.KICK;
            }

            switch (action) {
                case KICK -> BungeeAFK.getAFKHandler().performKickAction(player, Caption.of("notification.movement_pattern_kick_message"),
                       Component.empty());
                case CONNECT -> BungeeAFK.getAFKHandler().performConnectAction(player, Caption.of("notification.movement_pattern_kick_message"), Component.empty(),
                        Caption.of("notification.movement_pattern_connect_message"), Component.empty());
                case TELEPORT -> BungeeAFK.getAFKHandler().performTeleportAction(player, Caption.of("notification.movement_pattern_teleport_message"));
            }
            player.setAfkState(AFKState.ACTION_TAKEN);
        };
    }

    public void reloadConfigValues() {
        this.certaintyThreshold = Config.getInstance().getDouble("movement-pattern.certainty-threshold", 0.9);
        this.sampleSize = Config.getInstance().getInt("movement-pattern.sample-size", 5);
        this.disabledServers = Config.getInstance().getStringList("movement-pattern.disabled-servers");
        this.allowBypass = Config.getInstance().getBoolean("movement-pattern.allow-bypass", true);
        this.enabled = Config.getInstance().getBoolean("movement-pattern.enabled", true);
        this.clearAfterSeconds = Config.getInstance().getInt("movement-pattern.clear-after", 600);

        if (clearAfterSeconds < 0) {
            clearAfterSeconds = 0;
        }
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

        // Remove timestamps older than clearAfterSeconds
        if (clearAfterSeconds != 0) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - clearAfterSeconds * 1000L) {
                timestamps.pollFirst();
            }
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
        logger.info("Suspicious Movement Pattern Detected: Player {} @ {} | stdDev={} | certainty={}",
                player.getName(), location, stdDev, String.format("%.2f", certainty * 100) + "%");

        playerMovementHistory.get(player).clear();
        new Detection(DetectionType.MOVEMENT_PATTERN, System.currentTimeMillis(),
                player.getCurrentServerName(), player.getName());

        MessageBroadcaster.broadcastMessageToFiltered(Caption.of("notification.movement_pattern_detected_admin",
                        TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))),
                PlayerFilters.hasPermission(Config.getInstance().getString("movement-pattern.notify-permission", "bungeeafk.movement-pattern.notify")),
                PlayerFilters.notMatching(player));

        PlayerMovementPatternDetectedEvent event = new PlayerMovementPatternDetectedEvent(APIAdapter.adapt(player), APIAdapter.adaptModelConsumer(defaultActionOnDetection));
        EventDispatcher.post(event);
        APIAdapter.adaptCoreConsumer(event.getAction()).accept(player);
    }
}
