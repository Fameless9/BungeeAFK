package net.fameless.core.detection.history;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.text.DateFormat;
import java.util.*;

public record Detection(DetectionType type, long timestamp, String serverName, String playerName) {

    private static final List<Detection> DETECTIONS = new ArrayList<>();

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<Detection> getDetections() {
        return Collections.unmodifiableList(DETECTIONS);
    }

    public static @NotNull List<Detection> getDetectionsByPlayer(String playerName, DetectionType type) {
        List<Detection> playerDetections = new ArrayList<>();
        for (Detection detection : DETECTIONS) {
            if (detection.playerName().equals(playerName) && detection.type() == type) {
                playerDetections.add(detection);
            }
        }
        return playerDetections;
    }

    public Detection(DetectionType type, long timestamp, String serverName, String playerName) {
        this.type = type;
        this.timestamp = timestamp;
        this.serverName = serverName;
        this.playerName = playerName;

        DETECTIONS.add(this);
    }

    public @NotNull String getFriendlyString() {
        return String.format("Detected %s on %s at %s",
                playerName,
                serverName,
                DateFormat.getDateTimeInstance().format(new Date(timestamp))
        );
    }

    public @NotNull JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.name());
        json.addProperty("timestamp", timestamp);
        json.addProperty("serverName", serverName);
        json.addProperty("playerName", playerName);
        return json;
    }

    public static @NotNull Detection fromJson(@NotNull JsonObject json) {
        DetectionType type = DetectionType.valueOf(json.get("type").getAsString());
        long timestamp = json.get("timestamp").getAsLong();
        String serverName = json.get("serverName").getAsString();
        String playerName = json.get("playerName").getAsString();
        return new Detection(type, timestamp, serverName, playerName);
    }
}
