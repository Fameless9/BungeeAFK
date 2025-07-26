package net.fameless.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + PluginUpdater.class.getSimpleName());
    private static final String CURRENT_VERSION = "2.1.0";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/fameless9/BungeeAFK/releases/latest";
    private static final String JSON_TAG_NAME = "tag_name";
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void runTask() {
        scheduler.scheduleAtFixedRate(PluginUpdater::checkForUpdate, 0, 1, TimeUnit.DAYS);
    }

    private static void checkForUpdate() {
        try {
            JsonObject releaseData = fetchLatestRelease();
            String latestVersion = releaseData.get(JSON_TAG_NAME).getAsString();
            if (latestVersion.equals(CURRENT_VERSION)) return;

            LOGGER.info("New version found. Version {} can now be downloaded from {}.", latestVersion, "https://github.com/Fameless9/BungeeAFK/releases/latest");
        } catch (IOException e) {
            LOGGER.error("Failed to fetch or update the plugin: {}", e.getMessage());
        }
    }

    private static JsonObject fetchLatestRelease() throws IOException {
        URL url = URI.create(GITHUB_API_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return new Gson().fromJson(response.toString(), JsonObject.class);
        }
    }
}
