package net.fameless.core.config;

import java.io.FileNotFoundException;

public record ConfigRegistry(YamlConfig configStateOnLoad) {

    public boolean hasConfigFileChanged() {
        YamlConfig currentState;
        try {
            currentState = PluginConfig.readConfigFile();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return !currentState.data().equals(configStateOnLoad.data());
    }
}
