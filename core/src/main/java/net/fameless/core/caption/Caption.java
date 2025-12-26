package net.fameless.core.caption;

import com.google.gson.*;
import net.fameless.core.config.Config;
import net.fameless.core.scheduler.SchedulerService;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.fameless.core.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
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

    public static @NotNull Component of(String key, TagResolver... replacements) {
        String message = getString(key);
        message = message.replace("<prefix>", getString("prefix"));
        return MiniMessage.miniMessage().deserialize(message, replacements);
    }

    public static @NotNull String getAsLegacy(String key, TagResolver... replacements) {
        Component component = of(key, replacements);
        return LegacyComponentSerializer.legacySection().serialize(component);
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

        Set<String> requiredKeys = ResourceUtil.readJsonResource("lang_en.json").keySet();
        ResourceUtil.extractResourceIfMissing("lang_en.json", PluginPaths.getLangFile("en"));
        ResourceUtil.extractResourceIfMissing("lang_de.json", PluginPaths.getLangFile("de"));

        try (Stream<Path> stream = Files.list(langDir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();

                        if (fileName.startsWith("lang_") && fileName.endsWith(".json")) {
                            String identifier = fileName.substring(5, fileName.lastIndexOf(".json"));
                            if (StringUtil.containsChar(identifier, ' ')) {
                                logger.error("Failed to load language '{}' located at '{}' - No space allowed in identifier", identifier, fileName);
                                return;
                            }

                            JsonObject langObject;
                            try(var reader = Files.newBufferedReader(path)) {
                                langObject = gson.fromJson(reader, JsonObject.class);
                                if (langObject == null) {
                                    logger.error("Failed to load language '{}' located at '{}' - File is empty", identifier, fileName);
                                    return;
                                }

                                Set<String> missingKeys = new HashSet<>(requiredKeys);
                                missingKeys.removeAll(langObject.keySet());

                                if (!missingKeys.isEmpty()) {
                                    logger.error("Failed to load language '{}' located at '{}' - Missing the following key(s): {}", identifier, fileName, missingKeys);
                                    return;
                                }

                                languageJsonObjectHashMap.put(identifier, langObject);
                                logger.info("Successfully loaded language '{}'", identifier);
                            } catch (IOException | JsonIOException e) {
                                logger.error("Failed to load language '{}' located at '{}'", identifier, fileName, e);
                            } catch (JsonSyntaxException e) {
                                logger.error("Failed to load language '{}' located at '{}' - File does not contain a valid JSON syntax", identifier, fileName);
                            }
                        } else {
                            logger.warn("Invalid file name detected in language directory: '{}'. Format: 'lang_xx.json' - Skipping file...", fileName);
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
        JsonObject languageObject = languageJsonObjectHashMap.get(currentLanguage);
        if (!languageObject.has(key)) {
            return "<prefix><red>Error - No such key: " + key;
        }
        return languageObject.get(key).getAsString();
    }

    public static String getString(String language, String key) {
        JsonObject languageObject = languageJsonObjectHashMap.get(language);
        if (languageObject == null || !languageObject.has(key)) {
            return "<prefix><red>Error - No such key: " + key;
        }
        return languageObject.get(key).getAsString();
    }

    public static boolean hasKey(String language, String key) {
        JsonObject languageObject = languageJsonObjectHashMap.get(language);
        return languageObject != null && languageObject.has(key);
    }

    public static JsonObject getLanguageJsonObject(String language) {
        return languageJsonObjectHashMap.get(language);
    }

    public static @NonNull String getCurrentLanguage() {
        return currentLanguage.toLowerCase(Locale.US);
    }

    public static void setCurrentLanguage(@NonNull String newLanguage) {
        if (newLanguage.equals(currentLanguage)) return;
        if (!existsLanguage(newLanguage)) return;
        currentLanguage = newLanguage.toLowerCase(Locale.US);
        Config.getInstance().set("lang", newLanguage);
    }


    public static boolean existsLanguage(String lang) {
        return languageJsonObjectHashMap.containsKey(lang);
    }

    public static @NonNull Set<String> getAvailableLanguages() {
        return new HashSet<>(languageJsonObjectHashMap.keySet());
    }

    public static void saveToFile() {
        for (var entry : languageJsonObjectHashMap.entrySet()) {
            SchedulerService.VIRTUAL_EXECUTOR.submit(() -> {
                Path file = PluginPaths.getLangFile(entry.getKey());
                try(var writer = Files.newBufferedWriter(file)) {
                    gson.toJson(entry.getValue(), writer);
                } catch (IOException e) {
                    logger.error("Failed to save language file: {}", entry.getKey(), e);
                }
            });
        }
    }
}
