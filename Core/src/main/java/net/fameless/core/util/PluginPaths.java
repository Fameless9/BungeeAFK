package net.fameless.core.util;

import net.fameless.core.caption.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PluginPaths {

    public static final File BASE_FOLDER = new File("plugins" + File.separator + "BungeeAFK");

    @Contract("_ -> new")
    public static @NotNull File getLangFile(@NotNull Language language) {
        return new File(BASE_FOLDER, "lang/lang_" + language.getIdentifier() + ".json");
    }

    public static @NotNull File getConfigFile() {
        return new File(BASE_FOLDER, "config.yml");
    }

}
