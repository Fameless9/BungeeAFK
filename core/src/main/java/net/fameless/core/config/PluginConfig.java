package net.fameless.core.config;

import net.fameless.core.region.Region;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.fameless.core.util.YamlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PluginConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + PluginConfig.class.getSimpleName());
    public static Yaml YAML;
    private static YamlConfig config;

    @SuppressWarnings("unchecked")
    public static void init() {
        LOGGER.info("Loading configuration...");
        File configFile = PluginPaths.getConfigFile();

        if (!configFile.exists()) {
            ResourceUtil.extractResourceIfMissing("config.yml", configFile);
        }

        String yamlContent;
        try {
            yamlContent = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        YAML = new Yaml();
        config = new YamlConfig(YAML.load(yamlContent));

        loadBypassRegions();

        LOGGER.info("\nYour BungeeAFK configuration: \n  * lang: {} \n  * warning-delay: {} \n  * afk-delay: {} \n  * action-delay: {} \n  * action: {} \n  * afk-server-name: {} \n  * allow-bypass: {}",
                config.getString("lang", "en"),
                config.getInt("warning-delay", 300),
                config.getInt("afk-delay", 600),
                config.getInt("action-delay", 630),
                config.getString("action", "kick"),
                config.getString("afk-server-name", "not configured"),
                config.getBoolean("allow-bypass", true));
    }

    public static void loadBypassRegions() {
        Region.clearRegions();
        if (config.contains("bypass-regions")) {
            Map<String, Object> bypassRegions = config.getSection("bypass-regions");
            for (Map.Entry<String, Object> entry : bypassRegions.entrySet()) {
                Map<String, Object> regionData = (Map<String, Object>) entry.getValue();
                Region.fromMap(regionData);
            }
        } else {
            LOGGER.info("No bypass regions found in the configuration.");
        }
    }

    public static void saveRegions() {
        Map<String, Object> bypassRegions = new HashMap<>();
        for (int i = 0; i < Region.getAllRegions().size(); i++) {
            Region region = Region.getAllRegions().get(i);
            bypassRegions.put(region.getRegionName(), region.toMap());
        }
        config.set("bypass-regions", bypassRegions);
    }

    public static void reload() {
        init();
    }

    public static void shutdown() {
        saveNow();
    }

    public static void saveNow() {
        saveRegions();
        File configFile = PluginPaths.getConfigFile();

        String fileContent = YamlUtil.generateConfig();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(fileContent);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to write configuration file: {}", configFile.getAbsolutePath(), e);
        }
    }

    public static YamlConfig get() {
        return config;
    }
}
