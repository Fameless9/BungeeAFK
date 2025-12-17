package net.fameless.limbo;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;

public class Config {

    private final Map<String, Object> configData;

    public Config() {
        Yaml yaml = new Yaml();
        File configFile = ResourceUtil.extractResourceIfMissing("config.yml", new File(LimboTracking.getInstance().getDataFolder(), "config.yml"));
        try {
            configData = yaml.load(Files.readString(configFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getBoolean(String path, boolean def) {
        String s = configData.getOrDefault(path, def).toString();
        return switch (s.toLowerCase(Locale.US)) {
            case "true" -> true;
            case "false" ->  false;
            default -> def;
        };
    }

    public int getInt(String path, int def) {
        if (configData.containsKey(path)) {
            try {
                return Integer.parseInt(configData.get(path).toString());
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }

    public String getString(String path, String def) {
        return configData.getOrDefault(path, def).toString();
    }
}
