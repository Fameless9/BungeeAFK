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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + PluginConfig.class.getSimpleName());

    private static class Holder {
        private static final PluginConfig INSTANCE = new PluginConfig();
    }

    public static PluginConfig getInstance() {
        return Holder.INSTANCE;
    }

    private static final Object loadLock = new Object();

    private volatile YamlConfig config;
    private volatile ConfigRegistry configRegistry;

    private PluginConfig() {
        load();
    }

    public void load() {
        synchronized (loadLock) {
            LOGGER.info("Loading configuration...");

            ResourceUtil.extractResourceIfMissing("config.yml", PluginPaths.getConfigFile());

            YamlConfig config = readConfigFile();
            ConfigRegistry registry = new ConfigRegistry(config);

            this.config = config;
            this.configRegistry = registry;

            LOGGER.info("Configuration loaded successfully");
        }
    }

    public List<Region> readRegionsFromConfig() {
        List<Region> list = new ArrayList<>();
        if (config.contains("bypass-regions")) {
            Map<String, Object> section = config.getSection("bypass-regions");
            section.forEach((k, v) -> list.add(Region.fromMap((Map<String, Object>) v)));
        }
        return list;
    }

    public void saveRegionsToConfig(@NotNull List<Region> regions) {
        synchronized (loadLock) {
            Map<String, Object> map = new HashMap<>();
            for (Region region : regions) {
                map.put(region.getRegionName(), region.toMap());
            }

            config.set("bypass-regions", map);
        }
    }

    public @NotNull YamlConfig readConfigFile() {
        File configFile = PluginPaths.getConfigFile();

        try {
            String yamlContent = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
            return new YamlConfig(YamlUtil.YAML.load(yamlContent));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveNow() {
        SchedulerService.VIRTUAL_EXECUTOR.submit(() -> {
            try (var writer = new BufferedWriter(new FileWriter(PluginPaths.getConfigFile()))) {
                writer.write(YamlUtil.generateConfig());
            } catch (IOException e) {
                LOGGER.error("Failed to write configuration", e);
            }
        });
    }

    public void reloadAll() {
        LOGGER.info("Reloading all configurations...");
        load();
        BungeeAFK.getAFKHandler().reloadConfigValues();
        BungeeAFK.getAutoClickerDetector().reloadConfigValues();
        BungeeAFK.getMovementPatternDetection().reloadConfigValues();
        LOGGER.info("Reload complete");
    }

    public @NotNull YamlConfig getConfig() {
        return config;
    }

    public @NotNull ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }
}
