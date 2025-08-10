package net.fameless.core.detection.autoclicker;

import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.detection.history.Detection;
import net.fameless.core.detection.history.DetectionType;
import net.fameless.core.event.EventDispatcher;
import net.fameless.core.event.PlayerAutoClickerDetectedEvent;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.util.DetectionUtil;
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

public class AutoClickerDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + AutoClickerDetector.class.getSimpleName());

    private final Map<BAFKPlayer<?>, Deque<Long>> playerClickTimes = new ConcurrentHashMap<>();
    private final Map<BAFKPlayer<?>, Integer> detectionStreaks = new ConcurrentHashMap<>();

    private final Consumer<BAFKPlayer<?>> defaultActionOnDetection;
    private int sampleSize;
    private int consecutiveDetectionsRequired;
    private int stddevThresholdMillis;
    private int minClickIntervalMillis;
    private List<String> disabledServers;
    boolean allowBypass;
    boolean enabled;

    public AutoClickerDetector() {
        if (BungeeAFK.getAutoClickerDetector() != null) {
            throw new IllegalStateException("You may not create another instance of AutoClickerDetector!");
        }

        reloadConfigValues();

        this.defaultActionOnDetection = player -> {
            String notifyPermission = PluginConfig.get().getString("auto-clicker.notify-permission", "bungeeafk.autoclicker.notify");
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Auto-clicker detected for player: {}", player.getName());
            }

            BAFKPlayer.PLAYERS.stream()
                    .filter(PlayerFilters.isOnline())
                    .filter(PlayerFilters.hasPermission(notifyPermission))
                    .filter(PlayerFilters.notMatching(player))
                    .forEach(toNotify -> toNotify.sendMessage(Caption.of(
                            "notification.auto_clicker_detected_admin",
                            TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))
                    )));

            if (PluginConfig.get().getBoolean("auto-clicker.notify-player", true)) {
                player.sendMessage(Caption.of("notification.auto_clicker_detected_player"));
            }

            String actionIdentifier = PluginConfig.get().getString("auto-clicker.action", "kick");
            ActionOnDetection action = ActionOnDetection.existsByIdentifier(actionIdentifier)
                    ? ActionOnDetection.fromIdentifier(actionIdentifier)
                    : ActionOnDetection.KICK;
            switch (action) {
                case KICK -> player.kick(Caption.of("notification.auto_clicker_kick_message"));
                case OPEN_INVENTORY -> player.openEmptyInventory();
            }
        };
    }

    public void reloadConfigValues() {
        sampleSize = PluginConfig.get().getInt("auto-clicker.sample-size", 20);
        consecutiveDetectionsRequired = PluginConfig.get().getInt("auto-clicker.consecutive-detections", 3);
        stddevThresholdMillis = PluginConfig.get().getInt("auto-clicker.stddev-threshold", 50);
        minClickIntervalMillis = PluginConfig.get().getInt("auto-clicker.min-click-interval", 50);
        disabledServers = PluginConfig.get().getStringList("disabled-servers");
        allowBypass = PluginConfig.get().getBoolean("auto-clicker.allow-bypass", true);
        enabled = PluginConfig.get().getBoolean("auto-clicker.enabled", true);
    }

    public synchronized void registerClick(BAFKPlayer<?> player) {
        if (!enabled) return;
        if (allowBypass && player.hasPermission("bungeeafk.autoclicker.bypass")) return;
        if (disabledServers.contains(player.getCurrentServerName())) return;

        long now = System.currentTimeMillis();
        playerClickTimes.computeIfAbsent(player, k -> new ArrayDeque<>()).add(now);

        Deque<Long> clicks = playerClickTimes.get(player);
        if (clicks.size() > sampleSize) {
            clicks.pollFirst();
        }

        if (clicks.size() >= sampleSize) {
            List<Long> intervals = new ArrayList<>(sampleSize - 1);
            Iterator<Long> iterator = clicks.iterator();
            long prev = iterator.next();
            while (iterator.hasNext()) {
                long current = iterator.next();
                intervals.add(current - prev);
                prev = current;
            }

            double stdDev = DetectionUtil.calculateStdDev(intervals);

            boolean impossibleSpeed = intervals.stream().anyMatch(i -> i < minClickIntervalMillis);
            if (impossibleSpeed) {
                LOGGER.info("Auto-clicker detected for player {}: impossible click speed detected (intervals: {})",
                        player.getName(), intervals);
                autoClickerDetected(player);
                return;
            }

            if (stdDev < stddevThresholdMillis) {
                int streak = detectionStreaks.merge(player, 1, Integer::sum);

                if (streak >= consecutiveDetectionsRequired) {
                    LOGGER.info("Auto-clicker detected for player {}: consistent click speed detected (streak: {}, intervals: {})",
                            player.getName(), streak, intervals);
                    autoClickerDetected(player);
                }
            } else {
                detectionStreaks.remove(player);
            }
        }
    }

    private void autoClickerDetected(@NotNull BAFKPlayer<?> player) {
        new Detection(DetectionType.AUTO_CLICKER, System.currentTimeMillis(), player.getCurrentServerName(), player.getName());
        playerClickTimes.remove(player);
        detectionStreaks.remove(player);

        PlayerAutoClickerDetectedEvent event = new PlayerAutoClickerDetectedEvent(player, this.defaultActionOnDetection);
        EventDispatcher.post(event);
        event.getAction().accept(player);
    }
}
