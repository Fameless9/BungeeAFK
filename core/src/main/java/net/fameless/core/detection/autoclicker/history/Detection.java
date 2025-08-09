package net.fameless.core.detection.autoclicker.history;

import com.google.gson.JsonObject;
import net.fameless.core.config.PluginConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.text.DateFormat;
import java.util.*;

public class Detection {

    private static final List<Detection> DETECTIONS = new ArrayList<>();

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<Detection> getDetections() {
        return Collections.unmodifiableList(DETECTIONS);
    }

    public static @NotNull List<Detection> getDetectionsByPlayer(String playerName) {
        List<Detection> playerDetections = new ArrayList<>();
        for (Detection detection : DETECTIONS) {
            if (detection.getPlayerName().equals(playerName)) {
                playerDetections.add(detection);
            }
        }
        return playerDetections;
    }

    private final long timestamp;
    private final String serverName;
    private final String playerName;

    public Detection(long timestamp, String serverName, String playerName) {
        this.timestamp = timestamp;
        this.serverName = serverName;
        this.playerName = playerName;

        List<Detection> playerDetections = new ArrayList<>();
        for (Detection d : DETECTIONS) {
            if (d.playerName.equalsIgnoreCase(playerName)) {
                playerDetections.add(d);
            }
        }

        final int maxSize = PluginConfig.get().getInt("auto-clicker.detection-history-size", 10);
        if (playerDetections.size() >= maxSize) {
            Detection oldest = Collections.min(playerDetections, Comparator.comparingLong(Detection::getTimestamp));
            DETECTIONS.remove(oldest);
        }

        DETECTIONS.add(this);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getServerName() {
        return serverName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getFriendlyString() {
        return String.format("Detected %s on %s at %s",
                playerName,
                serverName,
                DateFormat.getDateTimeInstance().format(new Date(timestamp))
        );
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("timestamp", timestamp);
        json.addProperty("serverName", serverName);
        json.addProperty("playerName", playerName);
        return json;
    }

    public static @NotNull Detection fromJson(@NotNull JsonObject json) {
        long timestamp = json.get("timestamp").getAsLong();
        String serverName = json.get("serverName").getAsString();
        String playerName = json.get("playerName").getAsString();
        return new Detection(timestamp, serverName, playerName);
    }
}
