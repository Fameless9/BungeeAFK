package net.fameless.core.config;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class YamlConfig {

    private final Map<String, Object> data;

    public YamlConfig(Map<String, Object> data) {
        this.data = data;
    }

    public void set(String key, Object value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }

    public @NotNull String getString(String key) {
        Object val = data.get(key);
        if (val instanceof String) {
            return (String) val;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a String");
    }

    public String getString(String key, String defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof String) {
            return (String) val;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a String");
    }

    public int getInt(String key) {
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a Number");
    }

    public int getInt(String key, int defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a Number");
    }

    public boolean getBoolean(String key) {
        Object val = data.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a Boolean");
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a Boolean");
    }

    public @NotNull Map<String, Object> getSection(String key) {
        Object val = data.get(key);
        if (val instanceof Map) {
            return (Map<String, Object>) val;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a Map");
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
