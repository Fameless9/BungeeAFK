package net.fameless.core.detection.autoclicker.history;

import com.google.gson.*;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;

import java.io.*;

public class DetectionHistoryManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void loadDetections() {
        File detectionsFile = PluginPaths.getAutoClickerDetectionHistoryFile();
        ResourceUtil.extractResourceIfMissing("detection_history.json", detectionsFile);

        JsonArray detectionHistoryArray;
        try (Reader reader = new FileReader(detectionsFile)) {
            detectionHistoryArray = GSON.fromJson(reader, JsonArray.class);
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            throw new RuntimeException("Failed to read detection history file: " + detectionsFile, e);
        }

        detectionHistoryArray.asList().forEach(jsonElement -> {
            if (jsonElement.isJsonObject()) {
                JsonObject detectionObject = jsonElement.getAsJsonObject();
                Detection.fromJson(detectionObject);
            } else {
                throw new IllegalArgumentException("Invalid detection data: " + jsonElement);
            }
        });
    }

    public static void saveDetections() {
        File detectionsFile = PluginPaths.getAutoClickerDetectionHistoryFile();
        JsonArray detectionHistoryArray = new JsonArray();

        for (Detection detection : Detection.getDetections()) {
            detectionHistoryArray.add(detection.toJson());
        }

        try (Writer writer = new FileWriter(detectionsFile)) {
            GSON.toJson(detectionHistoryArray, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write detection history file: " + detectionsFile, e);
        }
    }
}
