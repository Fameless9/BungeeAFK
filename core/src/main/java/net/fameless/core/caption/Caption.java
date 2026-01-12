package net.fameless.core.caption;

import com.google.gson.*;
import net.fameless.core.config.Config;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.fameless.core.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public final class Caption {

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/" + Caption.class.getSimpleName());
    private static final HashMap<String, JsonObject> languageJsonObjectHashMap = new HashMap<>();
    private static String currentLanguage;
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private Caption() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static @Nullable Component of(String key, TagResolver... replacements) {
        String message = getString(key);
        if (Config.getInstance().getStringList("suppressed-messages").contains(key) || message.isEmpty()) {
            return null;
        }
        message = message.replace("<prefix>", getString("prefix"));
        return MiniMessage.miniMessage().deserialize(message, replacements);
    }

    public static void setJsonObject(String language, JsonObject jsonObject) {
        if (!languageJsonObjectHashMap.containsKey(language)) return;
        languageJsonObjectHashMap.put(language, jsonObject);
    }

    public static void loadLanguageFiles() {
        logger.info("Loading language files...");

        Path langDir = PluginPaths.getLangDir();
        try {
            Files.createDirectories(langDir);
        } catch (IOException e) {
            logger.error("Failed to create language directory", e);
            return;
        }

        ResourceUtil.extractResourceIfMissing("lang_en.json", PluginPaths.getLangFile("en"));
        ResourceUtil.extractResourceIfMissing("lang_de.json", PluginPaths.getLangFile("de"));

        try (Stream<Path> stream = Files.list(langDir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        if (!fileName.startsWith("lang_") || !fileName.endsWith(".json")) {
                            logger.warn("Invalid file name detected in language directory: '{}'. Format: 'lang_xx.json' - Skipping file...", path);
                            return;
                        }

                        String identifier = fileName.substring(5, fileName.lastIndexOf(".json"));
                        if (StringUtil.containsChar(identifier, ' ')) {
                            logger.error("Failed to load language '{}' located at '{}' - No space allowed in identifier", identifier, path);
                            return;
                        }

                        try (var reader = Files.newBufferedReader(path)) {
                            JsonObject loadedLang = gson.fromJson(reader, JsonObject.class);
                            if (loadedLang == null) {
                                logger.error("Failed to load language '{}' located at '{}' - File is empty", identifier, path);
                                return;
                            }

                            validateKeys(identifier, loadedLang);

                            languageJsonObjectHashMap.put(identifier, loadedLang);
                            logger.info("Successfully loaded language '{}'", identifier);
                        } catch (IOException | JsonIOException e) {
                            logger.error("Failed to load language '{}' located at '{}'", identifier, path, e);
                        } catch (JsonSyntaxException e) {
                            logger.error("Failed to load language '{}' located at '{}' - File does not contain a valid JSON syntax", identifier, path);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error listing language files", e);
        }

        // If no language could be loaded successfully, the files will be overwritten and the default files will be loaded
        if (languageJsonObjectHashMap.isEmpty()) {
            logger.warn("No language could be loaded successfully... Replacing with default language files and reloading");
            ResourceUtil.extractResource("lang_en.json", PluginPaths.getLangFile("en"));
            ResourceUtil.extractResource("lang_de.json", PluginPaths.getLangFile("de"));
            loadLanguageFiles();
        }
    }

    public static String getString(String key) {
        JsonObject langObject = getCurrentJsonObject();
        if (!langObject.has(key)) {
            return "<prefix><red>Error - No such key: " + key;
        }
        return langObject.get(key).getAsString();
    }

    public static String getString(String language, String key) {
        JsonObject langObject = languageJsonObjectHashMap.get(language);
        if (!hasKey(language, key)) {
            return "<prefix><red>Error - No such key: " + key;
        }
        return langObject.get(key).getAsString();
    }

    public static boolean hasKey(String key) {
        return hasKey(currentLanguage, key);
    }

    public static boolean hasKey(String language, String key) {
        JsonObject langObject = languageJsonObjectHashMap.get(language);
        return langObject != null && langObject.keySet().contains(key);
    }

    public static @NotNull JsonObject getLanguageJsonObject(String language) {
        JsonObject object = languageJsonObjectHashMap.get(language);
        if (object == null) return new JsonObject();
        return object.deepCopy();
    }

    public static @NotNull JsonObject getCurrentJsonObject() {
        return getLanguageJsonObject(currentLanguage);
    }

    public static @NotNull String getCurrentLanguage() {
        return currentLanguage.toLowerCase(Locale.US);
    }

    public static void setCurrentLanguage(@NotNull String newLanguage) {
        if (newLanguage.equals(currentLanguage)) return;
        if (!existsLanguage(newLanguage)) return;
        currentLanguage = newLanguage.toLowerCase(Locale.US);
        Config.getInstance().set("lang", newLanguage);
    }

    public static boolean existsLanguage(String lang) {
        return languageJsonObjectHashMap.containsKey(lang);
    }

    public static @NotNull Set<String> getAvailableLanguages() {
        return new HashSet<>(languageJsonObjectHashMap.keySet());
    }

    public static void saveToFile() {
        for (var entry : languageJsonObjectHashMap.entrySet()) {
            Path file = PluginPaths.getLangFile(entry.getKey());

            validateKeys(entry.getKey(), entry.getValue());

            try (var writer = Files.newBufferedWriter(file)) {
                gson.toJson(entry.getValue(), writer);
            } catch (IOException e) {
                logger.error("Failed to save language file: {}", entry.getKey(), e);
            }
        }
    }

    private static void validateKeys(String identifier, JsonObject langObject) {
        String defaultSource = identifier;
        JsonObject defaultLang;

        try {
            defaultLang = ResourceUtil.readJsonResource("lang_" + identifier + ".json");
        } catch (Throwable t) {
            defaultLang = ResourceUtil.readJsonResource("lang_en.json");
            defaultSource = "en";
        }

        Set<String> missingKeys = new HashSet<>();
        for (var entry : defaultLang.entrySet()) {
            if (!langObject.has(entry.getKey())) {
                missingKeys.add(entry.getKey());
                langObject.add(entry.getKey(), entry.getValue());
            }
        }

        if (!missingKeys.isEmpty()) {
            logger.warn("Language '{}' located at '{}' is missing the following key(s): '{}' - Using default values from '{}'",
                    identifier, PluginPaths.getLangFile(identifier), String.join(", ", missingKeys), defaultSource
            );
        }
    }
}
