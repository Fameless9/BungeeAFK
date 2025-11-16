package net.fameless.core.config;

public record ConfigRegistry(YamlConfig configStateOnLoad) {

    public boolean hasConfigFileChanged() {
        return !PluginConfig.getInstance().readConfigFile().data().equals(configStateOnLoad.data());
    }
}
