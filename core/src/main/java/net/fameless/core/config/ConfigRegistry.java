package net.fameless.core.config;

import java.io.FileNotFoundException;

public record ConfigRegistry(YamlConfig configStateOnLoad) {

    public boolean hasConfigFileChanged() {
        try {
            return !PluginConfig.getInstance().readConfigFile().data().equals(configStateOnLoad.data());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
