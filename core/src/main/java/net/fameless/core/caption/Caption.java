package net.fameless.core.caption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.scheduler.SchedulerService;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class Caption {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + Caption.class.getSimpleName());
    private static final HashMap<Language, JsonObject> languageJsonObjectHashMap = new HashMap<>();
    private static Language currentLanguage = Language.ENGLISH;
    private static final Gson GSON = new GsonBuilder()
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

    public static void loadLanguage(Language language, JsonObject jsonObject) {
        languageJsonObjectHashMap.put(language, jsonObject);
    }

    public static void loadDefaultLanguages() {
        LOGGER.info("Loading default languages...");
        for (Language language : Language.values()) {
            File langFile = ResourceUtil.extractResourceIfMissing("lang_" + language.getIdentifier() + ".json", PluginPaths.getLangFile(language));

            JsonObject jsonObject;
            try (FileReader reader = new FileReader(langFile)) {
                jsonObject = GSON.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load language file: " + langFile.getPath(), e);
            }

            JsonObject defaultJsonObject = ResourceUtil.readJsonResource("lang_" + language.getIdentifier() + ".json");
            for (Map.Entry<String, JsonElement> entry : defaultJsonObject.entrySet()) {
                String key = entry.getKey();
                if (!jsonObject.has(key)) {
                    jsonObject.add(key, entry.getValue());
                    LOGGER.warn("Missing key '{}' in language '{}', adding default value.", key, language.getIdentifier());
                }
            }

            loadLanguage(language, jsonObject);
        }
    }

    public static String getString(String key) {
        JsonObject languageObject = languageJsonObjectHashMap.get(currentLanguage);
        if (!languageObject.has(key)) {
            return "<prefix><red>Error - No such key: " + key;
        }
        return languageObject.get(key).getAsString();
    }

    public static String getString(Language language, String key) {
        JsonObject languageObject = languageJsonObjectHashMap.get(language);
        if (languageObject == null || !languageObject.has(key)) {
            return "<prefix><red>Error - No such key: " + key;
        }
        return languageObject.get(key).getAsString();
    }

    public static boolean hasKey(Language language, String key) {
        JsonObject languageObject = languageJsonObjectHashMap.get(language);
        return languageObject != null && languageObject.has(key);
    }

    public static JsonObject getLanguageJsonObject(Language language) {
        return languageJsonObjectHashMap.get(language);
    }

    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setCurrentLanguage(Language newLanguage) {
        if (newLanguage != getCurrentLanguage()) {
            Caption.currentLanguage = newLanguage;
            PluginConfig.get().set("lang", newLanguage.getIdentifier());
        }
    }

    public static void saveToFile() {
        for (Language language : Language.values()) {
            SchedulerService.VIRTUAL_EXECUTOR.submit(() -> {
                File langFile = PluginPaths.getLangFile(language);
                JsonObject jsonObject = languageJsonObjectHashMap.get(language);

                if (jsonObject != null) {
                    try (var writer = new BufferedWriter(new FileWriter(langFile))) {
                        GSON.toJson(jsonObject, writer);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to save language file: " + langFile.getPath(), e);
                    }
                }
            });
        }
    }
}
