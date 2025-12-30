package net.fameless.core.detection.history;

import com.google.gson.*;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.fameless.core.util.SchedulerService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DetectionHistoryManager {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void loadDetections() {
        Path detectionsFile = ResourceUtil.extractResourceIfMissing("detection_history.json", PluginPaths.getAutoClickerDetectionHistoryFile());

        JsonArray detectionHistoryArray;
        try (var reader = Files.newBufferedReader(detectionsFile)) {
            detectionHistoryArray = gson.fromJson(reader, JsonArray.class);
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
        SchedulerService.VIRTUAL_EXECUTOR.submit(() -> {
            Path detectionsFile = PluginPaths.getAutoClickerDetectionHistoryFile();
            JsonArray detectionHistoryArray = new JsonArray();

            for (Detection detection : Detection.getDetections()) {
                detectionHistoryArray.add(detection.toJson());
            }

            try (var writer = Files.newBufferedWriter(detectionsFile)) {
                gson.toJson(detectionHistoryArray, writer);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write detection history file: " + detectionsFile, e);
            }
        });
    }
}
