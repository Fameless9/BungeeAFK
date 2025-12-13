package net.fameless.core.handling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.config.Config;
import net.fameless.core.location.Location;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.player.GameMode;
import net.fameless.core.scheduler.SchedulerService;
import net.fameless.core.util.*;
import net.fameless.core.util.cache.ExpirableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AFKHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + AFKHandler.class.getSimpleName());
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Map<UUID, String> playerPreviousServerMap = new ConcurrentHashMap<>();
    private final Map<UUID, Location> playerPreviousLocationMap = new ConcurrentHashMap<>();
    private final Map<UUID, GameMode> playerPreviousGameModeMap = new ConcurrentHashMap<>();
    private final ExpirableSet<BAFKPlayer<?>> revertCooldown = new ExpirableSet<>();
    private static final long UPDATE_PERIOD_MILLIS = 500L;

    private Action action;
    private long warnDelay;
    private long afkDelay;
    private long actionDelay;
    private BroadcastStrategy broadcastStrategy;
    private boolean actionbarEnabled;
    private final ScheduledFuture<?> scheduledTask;

    public AFKHandler() {
        if (BungeeAFK.getAFKHandler() != null) throw new IllegalStateException("AFKHandler is already initialized.");
        reloadConfigValues();
        this.scheduledTask = SchedulerService.SCHEDULED_EXECUTOR
                .scheduleAtFixedRate(this::run, 0, UPDATE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
        fetchPreviousPlayerStates();
        onInit();
    }

    private void run() {
        try {
            BAFKPlayer.PLAYERS.stream()
                    .filter(PlayerFilters.isOnline())
                    .forEach(player -> {
                        player.increaseTimeSinceLastAction(UPDATE_PERIOD_MILLIS);
                        SchedulerService.VIRTUAL_EXECUTOR.submit(() -> processPlayer(player));
                    });
        } catch (Exception e) {
            LOGGER.error("Error during AFK check task", e);
            scheduledTask.cancel(false);
        }
    }

    public void processPlayer(BAFKPlayer<?> player) {
        try {
            switch (player.getAfkState()) {
                case ACTIVE -> warnIfNeeded(player);
                case WARNED -> setAFKIfNeeded(player);
                case AFK -> determineAndPerformAction(player);
            }
            revertPreviousState(player);
            sendActionBar(player);
        } catch (Exception e) {
            LOGGER.error("Error processing AFK checks for player {}", player.getName());
        }
    }

    private void revertPreviousState(@NotNull BAFKPlayer<?> player) {
        if (revertCooldown.contains(player)) return;
        if (player.getTimeSinceLastAction() >= actionDelay) return;
        if (player.getAfkState().equals(AFKState.ACTION_TAKEN)) return;

        String afkServerName = Config.getInstance().getString("afk-server-name", "");
        if (player.getCurrentServerName().equalsIgnoreCase(afkServerName)) {
            player.connect(playerPreviousServerMap.getOrDefault(player.getUniqueId(), "lobby"));
            playerPreviousServerMap.remove(player.getUniqueId());
        }

        if (playerPreviousLocationMap.containsKey(player.getUniqueId()) && playerPreviousGameModeMap.containsKey(player.getUniqueId())) {
            player.teleport(playerPreviousLocationMap.remove(player.getUniqueId()));
            player.updateGameMode(playerPreviousGameModeMap.remove(player.getUniqueId()));
        }
        revertCooldown.add(player, UPDATE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void warnIfNeeded(@NotNull BAFKPlayer<?> player) {
        if (player.getTimeSinceLastAction() < warnDelay) return;
        player.sendMessage(Caption.of("notification.afk_warning"));
        player.setAfkState(AFKState.WARNED);
    }

    private void setAFKIfNeeded(@NotNull BAFKPlayer<?> player) {
        if (player.getTimeSinceLastAction() < afkDelay) return;
        player.setAfkState(AFKState.AFK);

        long timeUntilAction = Math.max(0, actionDelay - afkDelay);
        player.sendMessage(Caption.of(
                action.getMessageKey(),
                TagResolver.resolver("action-delay", Tag.inserting(Component.text(Format.formatTime((int) (timeUntilAction / 1000)))))
        ));

        MessageBroadcaster.broadcastMessageToFiltered(
                Caption.of("notification.afk_broadcast",
                        TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))),
                broadcastStrategy.broadcastFilter(player)
        );

        LOGGER.info("{} is now AFK.", player.getName());
    }

    private void determineAndPerformAction(@NotNull BAFKPlayer<?> player) {
        if (player.getTimeSinceLastAction() < actionDelay) return;
        switch (action) {
            case CONNECT -> performConnectAction(player,
                    Caption.of("notification.afk_kick"),
                    Caption.of("notification.afk_kick_broadcast", TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))),
                    Caption.of("notification.afk_disconnect"),
                    Caption.of("notification.afk_disconnect_broadcast", TagResolver.resolver("player", Tag.inserting(Component.text(player.getName()))))
            );
            case KICK -> performKickAction(player,
                    Caption.of("notification.afk_kick"),
                    Caption.of("notification.afk_kick_broadcast", TagResolver.resolver("player", Tag.inserting(Component.text(player.getName()))))
            );
            case TELEPORT -> performTeleportAction(player, Caption.of("notification.afk_teleport"));
        }
        player.setAfkState(AFKState.ACTION_TAKEN);
    }

    public void performConnectAction(@NotNull BAFKPlayer<?> player, Component kickReason, Component kickBroadcastMessage, Component connectMessage, Component connectBroadcastMessage) {
        if (!Action.isAfkServerConfigured()) {
            LOGGER.warn("AFK server not configured. Defaulting to KICK.");
            this.action = Action.KICK;
            performKickAction(player, kickReason, kickBroadcastMessage);
            return;
        }

        String previousServer = player.getCurrentServerName();
        String afkServerName = Config.getInstance().getString("afk-server-name", "");

        player.connect(afkServerName)
                .thenAccept(success -> {
                    if (success) {
                        playerPreviousServerMap.put(player.getUniqueId(), previousServer);
                        player.sendMessage(connectMessage);

                        MessageBroadcaster.broadcastMessageToFiltered(
                                connectBroadcastMessage,
                                broadcastStrategy.broadcastFilter(player)
                        );

                        LOGGER.info("Moved {} to AFK server.", player.getName());
                    } else {
                        LOGGER.warn("Error while trying to connect {} to the AFK-Server. Defaulting to KICK", player.getName());
                        performKickAction(player, kickReason, kickBroadcastMessage);
                    }
                });
    }

    public void performKickAction(@NotNull BAFKPlayer<?> player, Component reason, Component broadcastMessage) {
        player.kick(reason);

        MessageBroadcaster.broadcastMessageToFiltered(
                broadcastMessage,
                broadcastStrategy.broadcastFilter(player)
        );

        LOGGER.info("Kicked {} for being AFK.", player.getName());
    }

    public void performTeleportAction(@NotNull BAFKPlayer<?> player, Component message) {
        playerPreviousLocationMap.put(player.getUniqueId(), player.getLocation());
        playerPreviousGameModeMap.put(player.getUniqueId(), player.getGameMode());
        player.updateGameMode(GameMode.SPECTATOR);
        player.teleport(Location.getConfiguredAfkZone());
        player.sendMessage(message);
    }

    private void sendActionBar(@NotNull BAFKPlayer<?> player) {
        if (!actionbarEnabled) return;
        if (player.getAfkState().equals(AFKState.AFK)) {
            player.sendActionbar(Caption.of("actionbar.afk"));
        } else if (player.getAfkState().equals(AFKState.ACTION_TAKEN)) {
            player.sendActionbar(Caption.of(action.equals(Action.CONNECT) ? "actionbar.afk_moved" : "actionbar.afk"));
        }
    }

    public void fetchPreviousPlayerStates() {
        File playerStatesFile = ResourceUtil.extractResourceIfMissing("persisted_player_states.json", PluginPaths.getPersistedStatesFile());

        JsonObject root;
        try (FileReader reader = new FileReader(playerStatesFile)) {
            root = GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonObject locationObject = root.has("location") ? root.getAsJsonObject("location") : new JsonObject();
        JsonObject gameModeObject = root.has("game_mode") ? root.getAsJsonObject("game_mode") : new JsonObject();
        JsonObject serverObject = root.has("server") ? root.getAsJsonObject("server") : new JsonObject();

        for (Map.Entry<String, JsonElement> entry : locationObject.entrySet()) {
            UUID playerUUID = UUID.fromString(entry.getKey());
            JsonObject locationData = entry.getValue().getAsJsonObject();
            playerPreviousLocationMap.put(playerUUID, new Location(
                    locationData.get("worldName").getAsString(),
                    locationData.get("x").getAsDouble(),
                    locationData.get("y").getAsDouble(),
                    locationData.get("z").getAsDouble(),
                    locationData.get("pitch").getAsFloat(),
                    locationData.get("yaw").getAsFloat()
            ));
        }

        for (Map.Entry<String, JsonElement> entry : gameModeObject.entrySet()) {
            UUID playerUUID = UUID.fromString(entry.getKey());
            String gameModeStr = entry.getValue().getAsString();
            try {
                GameMode gameMode = GameMode.valueOf(gameModeStr.toUpperCase());
                playerPreviousGameModeMap.put(playerUUID, gameMode);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid game mode for player {}: {}", playerUUID, gameModeStr);
            }
        }

        for (Map.Entry<String, JsonElement> entry : serverObject.entrySet()) {
            UUID playerUUID = UUID.fromString(entry.getKey());
            String previousServer = entry.getValue().getAsString();
            playerPreviousServerMap.put(playerUUID, previousServer);
        }
    }

    public void reloadConfigValues() {
        Config config = Config.getInstance();
        this.warnDelay = config.getInt("warning-delay", 300) * 1000L;
        this.afkDelay = config.getInt("afk-delay", 600) * 1000L;
        this.actionDelay = config.getInt("action-delay", 630) * 1000L;
        this.actionbarEnabled = config.getBoolean("actionbar", true);

        try {
            this.broadcastStrategy = BroadcastStrategy.valueOf(config.getString("broadcast-strategy", "PER_SERVER"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid Broadcast Strategy in config. Defaulting to 'PER_SERVER'.");
            this.broadcastStrategy = BroadcastStrategy.PER_SERVER;
        }

        try {
            this.action = Action.fromIdentifier(config.getString("action", ""));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid action identifier in config. Defaulting to 'KICK'.");
            this.action = Action.KICK;
        }

        if (action == Action.CONNECT) {
            String serverName = config.getString("afk-server-name", "");
            if (!BungeeAFK.getPlatform().doesServerExist(serverName)) {
                LOGGER.warn("AFK server not found. Defaulting to KICK.");
                this.action = Action.KICK;
            }
        }
    }

    public void shutdown() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
        }

        JsonObject locationObject = new JsonObject();
        JsonObject gameModeObject = new JsonObject();
        JsonObject serverObject = new JsonObject();

        for (Map.Entry<UUID, String> entry : playerPreviousServerMap.entrySet()) {
            serverObject.addProperty(entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, GameMode> entry : playerPreviousGameModeMap.entrySet()) {
            gameModeObject.addProperty(entry.getKey().toString(), entry.getValue().name());
        }
        for (Map.Entry<UUID, Location> entry : playerPreviousLocationMap.entrySet()) {
            locationObject.add(entry.getKey().toString(), entry.getValue().toJson());
        }

        JsonObject root = new JsonObject();
        root.add("location", locationObject);
        root.add("game_mode", gameModeObject);
        root.add("server", serverObject);

        SchedulerService.VIRTUAL_EXECUTOR.submit(() -> {
            File playerStatesFile = ResourceUtil.extractResourceIfMissing("persisted_player_states.json", PluginPaths.getPersistedStatesFile());
            try (var writer = new BufferedWriter(new FileWriter(playerStatesFile))) {
                GSON.toJson(root, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
 }

    public BroadcastStrategy getBroadcastStrategy() {
        return broadcastStrategy;
    }

    public void setActionbarEnabled(boolean actionbarEnabled) {
        this.actionbarEnabled = actionbarEnabled;
    }

    public void setBroadcastStrategy(BroadcastStrategy broadcastStrategy) {
        this.broadcastStrategy = broadcastStrategy;
    }

    public Action getAction() {
        return action;
    }

    public long getWarnDelayMillis() {
        return warnDelay;
    }

    public long getAfkDelayMillis() {
        return afkDelay;
    }

    public long getActionDelayMillis() {
        return actionDelay;
    }

    public void setAction(@NotNull Action action) {
        if (!action.isAvailable()) return;
        this.action = action;
        Config.getInstance().set("action", action.getIdentifier());
    }

    public void setWarnDelayMillis(long delay) {
        this.warnDelay = delay;
        Config.getInstance().set("warning-delay", (int) (delay / 1000));
    }

    public void setActionDelayMillis(long delay) {
        this.actionDelay = delay;
        Config.getInstance().set("action-delay", (int) (delay / 1000));
    }

    public void setAfkDelayMillis(long delay) {
        this.afkDelay = delay;
        Config.getInstance().set("afk-delay", (int) (delay / 1000));
    }

    protected abstract void onInit();
}
