package net.fameless.core.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginPaths {

    public static final Path BASE_FOLDER = Paths.get("plugins", "BungeeAFK");

    public static @NotNull Path getLangFile(@NotNull String language) {
        return BASE_FOLDER.resolve("lang").resolve("lang_" + language + ".json");
    }

    public static @NotNull Path getLangDir() {
        return BASE_FOLDER.resolve("lang");
    }

    public static @NotNull Path getConfigFile() {
        return BASE_FOLDER.resolve("config.yml");
    }

    public static @NotNull Path getPersistedStatesFile() {
        return BASE_FOLDER.resolve("storage").resolve("persisted_states.json");
    }

    public static @NotNull Path getAutoClickerDetectionHistoryFile() {
        return BASE_FOLDER.resolve("storage").resolve("detection_history.json");
    }
}
