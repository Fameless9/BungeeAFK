package net.fameless.core.config;

import java.util.Map;

public record ConfigRegistry(Map<String, Object> configStateOnLoad) {

    public boolean hasConfigFileChanged() {
        return !Config.getInstance().readConfigFile().equals(configStateOnLoad);
    }
}
