package net.fameless.core.config;

import net.fameless.core.BungeeAFK;
import net.fameless.core.config.adapter.RegionTypeAdapter;
import net.fameless.core.config.adapter.TypeAdapter;
import net.fameless.core.config.adapter.TypeAdapterRegistry;
import net.fameless.core.region.Region;
import net.fameless.core.scheduler.SchedulerService;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.YamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private static class Holder {
        private static final Config INSTANCE = new Config();
    }

    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/Config");

    private volatile Map<String, Object> data = Collections.emptyMap();
    private volatile ConfigRegistry configRegistry;

    private final TypeAdapterRegistry typeAdapterRegistry = new TypeAdapterRegistry();
    private final Object writeLock = new Object();

    public Config() {
        LOGGER.info("Initializing Config...");
        typeAdapterRegistry.register(Region.class, new RegionTypeAdapter());
        load();
    }

    public void load() {
        synchronized (writeLock) {
            this.data = deepUnmodifiable(readConfigFile());
            this.configRegistry = new ConfigRegistry(data);
        }
    }

    public Map<String, Object> readConfigFile() {
        File configFile = PluginPaths.getConfigFile();
        try {
            String yamlContent = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
            return YamlUtil.YAML.load(yamlContent);
        } catch (Exception e) {
            throw new RuntimeException("Error reading config file", e);
        }
    }

    public @Nullable Object getValue(@NotNull String key) {
        String[] parts = key.split("\\.");
        Object current = data;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!(current instanceof Map<?, ?> map)) {
                return (i == parts.length - 1) ? current : null;
            }
            current = map.get(part);
            if (current == null) return null;
        }
        return current;
    }

    public void set(@NotNull String key, Object value) {
        synchronized (writeLock) {
            String[] parts = key.split("\\.");

            Map<String, Object> newData = new ConcurrentHashMap<>(data);

            Map<String, Object> current = newData;
            Object original = data;

            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];

                Object child = (original instanceof Map<?, ?> map) ? map.get(part) : null;

                if (child instanceof Map<?, ?> childMap) {
                    Map<String, Object> copiedChild = new ConcurrentHashMap<>((Map<String, Object>) childMap);
                    current.put(part, copiedChild);
                    current = copiedChild;
                    original = childMap;
                } else {
                    Map<String, Object> newChild = new ConcurrentHashMap<>();
                    current.put(part, newChild);
                    current = newChild;
                    original = null;
                }
            }

            String last = parts[parts.length - 1];
            if (value == null) current.remove(last);
            else current.put(last, value);

            this.data = Collections.unmodifiableMap(newData);
        }
    }

    public <T> T get(String key, Class<T> type) {
        Object value = getValue(key);
        if (value == null) return null;
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        TypeAdapter<T> adapter = typeAdapterRegistry.getAdapter(type);
        if (adapter != null) {
            return adapter.adapt(value);
        }
        return defaultConvert(value, type);
    }

    public <T> T getOrDefault(String key, Class<T> type, T def) {
        try {
            T val = get(key, type);
            return val != null ? val : def;
        } catch (RuntimeException e) {
            return def;
        }
    }

    public String dump() {
        return YamlUtil.YAML.dump(data);
    }

    public void saveConfig() {
        try (var writer = new BufferedWriter(new FileWriter(PluginPaths.getConfigFile()))) {
            writer.write(YamlUtil.generateConfig());
        } catch (IOException e) {
            LOGGER.error("Failed to write configuration", e);
        }
    }

    public void saveConfigAsync() {
        SchedulerService.VIRTUAL_EXECUTOR.submit(this::saveConfig);
    }

    public void reloadAll() {
        LOGGER.info("Reloading all configurations...");
        load();
        BungeeAFK.getAFKHandler().reloadConfigValues();
        BungeeAFK.getAutoClickerDetector().reloadConfigValues();
        BungeeAFK.getMovementPatternDetection().reloadConfigValues();
        LOGGER.info("Reload complete");
    }

    private <T> @Unmodifiable @NotNull T defaultConvert(Object raw, Class<T> type) {
        if (raw instanceof Number n) {
            if (type == Integer.class) return type.cast(n.intValue());
            if (type == Long.class) return type.cast(n.longValue());
            if (type == Double.class) return type.cast(n.doubleValue());
            if (type == Float.class) return type.cast(n.floatValue());
        }
        if (raw instanceof Boolean b && type == String.class) {
            return type.cast(b.toString());
        }
        if (raw instanceof String s) {
            try {
                if (type == Integer.class) return type.cast(Integer.parseInt(s));
                if (type == Long.class) return type.cast(Long.parseLong(s));
                if (type == Double.class) return type.cast(Double.parseDouble(s));
                if (type == Float.class) return type.cast(Float.parseFloat(s));
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("Cannot convert type " + raw.getClass().getName() + " to " + type.getName() + "; No type adapter registered");
    }

    private @NotNull @UnmodifiableView Map<String, Object> deepUnmodifiable(@NotNull Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        for (var entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = deepUnmodifiable((Map<String, Object>) value);
            }
            newMap.put(entry.getKey(), value);
        }
        return Collections.unmodifiableMap(newMap);
    }


    public String getString(String key) {
        return getOrDefault(key, String.class, null);
    }

    public String getString(String key, String fallback) {
        return getOrDefault(key, String.class, fallback);
    }

    public Integer getInt(String key) {
        return getOrDefault(key, Integer.class, null);
    }

    public int getInt(String key, int fallback) {
        return getOrDefault(key, Integer.class, fallback);
    }

    public Long getLong(String key) {
        return getOrDefault(key, Long.class, null);
    }

    public long getLong(String key, long fallback) {
        return getOrDefault(key, Long.class, fallback);
    }

    public Double getDouble(String key) {
        return getOrDefault(key, Double.class, null);
    }

    public double getDouble(String key, double fallback) {
        return getOrDefault(key, Double.class, fallback);
    }

    public Float getFloat(String key) {
        return getOrDefault(key, Float.class, null);
    }

    public float getFloat(String key, float fallback) {
        return getOrDefault(key, Float.class, fallback);
    }

    public Boolean getBoolean(String key) {
        return getOrDefault(key, Boolean.class, null);
    }

    public boolean getBoolean(String key, boolean fallback) {
        return getOrDefault(key, Boolean.class, fallback);
    }

    @SuppressWarnings("unchecked")
    public @NotNull Map<String, Object> getSection(String key) {
        Object v = getValue(key);
        if (v instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<String> getStringList(String key) {
        Object v = getValue(key);
        if (v instanceof List<?> list) {
            return (List<String>) Collections.unmodifiableList(list);
        }
        return new ArrayList<>();
    }

    public boolean contains(String key) {
        return getValue(key) != null;
    }

    public TypeAdapterRegistry getTypeAdapterRegistry() {
        return typeAdapterRegistry;
    }

    public ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }
}
