package net.fameless.core.config;

import net.fameless.core.BungeeAFK;
import net.fameless.core.config.adapter.RegionTypeAdapter;
import net.fameless.core.config.adapter.TypeAdapter;
import net.fameless.core.config.adapter.TypeAdapterRegistry;
import net.fameless.core.network.OutboundPacketSender;
import net.fameless.core.region.Region;
import net.fameless.core.util.PluginPaths;
import net.fameless.core.util.ResourceUtil;
import net.fameless.core.util.SchedulerService;
import net.fameless.core.util.YamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Config {

    private static class Holder {
        private static final Config INSTANCE = new Config();
    }

    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/Config");

    private final Map<String, Object> defaultData;
    private volatile Map<String, Object> data = Collections.emptyMap();
    private volatile ConfigRegistry configRegistry;

    private final boolean initialized;
    private final TypeAdapterRegistry typeAdapterRegistry = new TypeAdapterRegistry();
    private final Object writeLock = new Object();

    public Config() {
        logger.info("Initializing Config...");
        this.defaultData = YamlUtil.YAML.load(ResourceUtil.readResource("config.yml"));
        ResourceUtil.extractResourceIfMissing("config.yml", PluginPaths.getConfigFile());
        typeAdapterRegistry.register(Region.class, new RegionTypeAdapter());
        load();
        initialized = true;
    }

    public void load() {
        synchronized (writeLock) {
            Map<String, Object> loadedData = readConfigFile();
            if (loadedData == null) loadedData = new HashMap<>();

            this.data = deepUnmodifiable(loadedData);
            this.configRegistry = new ConfigRegistry(data);

            checkMissingKeys();

            if (initialized) {
                OutboundPacketSender.getInstance().sendConfigurationPacket();
            }
        }
    }

    public void checkMissingKeys() {
        Set<String> missingKeys = new HashSet<>(defaultData.keySet());
        missingKeys.removeAll(data.keySet());
        if (missingKeys.isEmpty()) return;
        logger.warn("Config is missing the following key(s): {}. Default values will be used", missingKeys);
    }

    public Map<String, Object> readConfigFile() {
        try {
            String yamlContent = Files.readString(PluginPaths.getConfigFile());
            if (yamlContent.isBlank()) {
                throw new RuntimeException("Error reading config file: config file is blank");
            }
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

    @SuppressWarnings("unchecked")
    public void set(@NotNull String key, @Nullable Object value) {
        synchronized (writeLock) {
            String[] parts = key.split("\\.");

            Map<String, Object> newData = new HashMap<>(data);

            Map<String, Object> current = newData;
            Object original = data;

            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];

                Object child = (original instanceof Map<?, ?> map) ? map.get(part) : null;

                if (child instanceof Map<?, ?> childMap) {
                    Map<String, Object> copiedChild = new HashMap<>((Map<String, Object>) childMap);
                    current.put(part, copiedChild);
                    current = copiedChild;
                    original = childMap;
                } else {
                    Map<String, Object> newChild = new HashMap<>();
                    current.put(part, newChild);
                    current = newChild;
                    original = null;
                }
            }

            String last = parts[parts.length - 1];
            if (value == null) current.remove(last);
            else current.put(last, value);

            if (!this.data.equals(newData)) {
                this.data = deepUnmodifiable(newData);
                OutboundPacketSender.getInstance().sendConfigurationPacket();
            }
        }
    }

    public @Nullable <T> T get(String key, Class<T> type) {
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

    public @NotNull <T> T getOrDefault(String key, Class<T> type, @NotNull T def) {
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
        try (var writer = Files.newBufferedWriter(PluginPaths.getConfigFile())) {
            writer.write(YamlUtil.generateConfig());
        } catch (IOException e) {
            logger.error("Failed to write configuration", e);
        }
    }

    public void saveConfigAsync() {
        SchedulerService.VIRTUAL_EXECUTOR.submit(this::saveConfig);
    }

    public void reloadAll() {
        logger.info("Reloading all configurations...");
        load();
        BungeeAFK.getAFKHandler().reloadConfigValues();
        BungeeAFK.getAutoClickerDetector().reloadConfigValues();
        BungeeAFK.getMovementPatternDetection().reloadConfigValues();
        logger.info("Reload complete");
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

    @SuppressWarnings("unchecked")
    private @NotNull @UnmodifiableView Map<String, Object> deepUnmodifiable(@NotNull Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        for (var entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> subMap) {
                value = deepUnmodifiable((Map<String, Object>) subMap);
            } else if (value instanceof List<?> list) {
                value = List.copyOf(list);
            }
            newMap.put(entry.getKey(), value);
        }
        return Collections.unmodifiableMap(newMap);
    }


    public @Nullable String getString(String key) {
        return get(key, String.class);
    }

    public @NotNull String getString(String key, @NotNull String fallback) {
        return getOrDefault(key, String.class, fallback);
    }

    public @Nullable Integer getInt(String key) {
        return get(key, Integer.class);
    }

    public int getInt(String key, int fallback) {
        return getOrDefault(key, Integer.class, fallback);
    }

    public @Nullable Long getLong(String key) {
        return get(key, Long.class);
    }

    public long getLong(String key, long fallback) {
        return getOrDefault(key, Long.class, fallback);
    }

    public @Nullable Double getDouble(String key) {
        return get(key, Double.class);
    }

    public double getDouble(String key, double fallback) {
        return getOrDefault(key, Double.class, fallback);
    }

    public @Nullable Float getFloat(String key) {
        return get(key, Float.class);
    }

    public float getFloat(String key, float fallback) {
        return getOrDefault(key, Float.class, fallback);
    }

    public @Nullable Boolean getBoolean(String key) {
        return get(key, Boolean.class);
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
