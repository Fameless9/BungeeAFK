package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class Config {

    private final Map<String, Object> configData;

    public Config() {
        Yaml yaml = new Yaml();
        File configFile = ResourceUtil.extractResourceIfMissing("config.yml", new File(LimboTracking.getInstance().getDataFolder(), "config.yml"));
        try {
            configData = yaml.load(Files.readString(configFile.toPath()));
            Limbo.getInstance().getConsole().sendMessage(configData.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getBoolean(String path) {
        return Boolean.parseBoolean(configData.get(path).toString());
    }

    public boolean getBoolean(String path, boolean def) {
        if (configData.containsKey(path)) {
            try {
                return Boolean.parseBoolean(configData.get(path).toString());
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }

    public int getInt(String path) {
        return Integer.parseInt(configData.get(path).toString());
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

    public String getString(String path) {
        return configData.get(path).toString();
    }

    public String getString(String path, String def) {
        if (configData.containsKey(path)) {
            try {
                return configData.get(path).toString();
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }

}
