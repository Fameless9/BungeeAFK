package net.fameless.core.config;

import net.fameless.core.BungeeAFK;
import net.fameless.core.region.Region;
import net.fameless.core.scheduler.SchedulerService;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.fameless.core.util.YamlUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PluginConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + PluginConfig.class.getSimpleName());

    private static class Holder {
        private static final PluginConfig INSTANCE = new PluginConfig();
    }

    public static PluginConfig getInstance() {
        return Holder.INSTANCE;
    }

    private @NotNull YamlConfig config;
    private @NotNull ConfigRegistry configRegistry;

    private PluginConfig() {
        load();
    }

    public void load() {
        LOGGER.info("Loading configuration...");
        ResourceUtil.extractResourceIfMissing("config.yml", PluginPaths.getConfigFile());
        try {
            this.config = readConfigFile();
            this.configRegistry = new ConfigRegistry(config);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        loadBypassRegions();
    }

    public @NotNull YamlConfig getConfig() {
        return config;
    }

    public @NotNull YamlConfig readConfigFile() throws FileNotFoundException {
        File configFile = PluginPaths.getConfigFile();
        if (!configFile.exists()) {
            throw new FileNotFoundException("Failed to read config file: File does not exist");
        }

        String yamlContent;
        try {
            yamlContent = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new YamlConfig(YamlUtil.YAML.load(yamlContent));
    }

    public void loadBypassRegions() {
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

    public void saveRegions() {
        Map<String, Object> bypassRegions = new HashMap<>();
        for (int i = 0; i < Region.getAllRegions().size(); i++) {
            Region region = Region.getAllRegions().get(i);
            bypassRegions.put(region.getRegionName(), region.toMap());
        }
        config.set("bypass-regions", bypassRegions);
    }

    public void reloadAll() {
        LOGGER.info("Reloading all configurations...");
        this.load();
        BungeeAFK.getAFKHandler().reloadConfigValues();
        BungeeAFK.getAutoClickerDetector().reloadConfigValues();
        BungeeAFK.getMovementPatternDetection().reloadConfigValues();
        LOGGER.info("Reload complete");
    }

    public void saveNow() {
        SchedulerService.VIRTUAL_EXECUTOR.submit(() -> {
            saveRegions();
            File configFile = PluginPaths.getConfigFile();
            String fileContent = YamlUtil.generateConfig();

            try (var writer = new BufferedWriter(new FileWriter(configFile))) {
                writer.write(fileContent);
            } catch (IOException e) {
                LOGGER.error("Failed to write configuration file: {}", configFile.getAbsolutePath(), e);
            }
        });
    }

    public @NotNull ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }
}
