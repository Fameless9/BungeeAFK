package net.fameless.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.fameless.core.caption.Caption;
import net.fameless.core.caption.Language;
import net.fameless.core.command.framework.Command;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.inject.PlatformConfig;
import net.fameless.core.util.ResourceUtil;

import java.util.logging.Logger;

public class BungeeAFK {

    private static boolean initialized = false;
    private static BungeeAFKPlatform platform;
    private static Logger logger;
    private static Injector injector;
    private static PlatformConfig config;

    public static synchronized void initCore(AbstractModule platformModule) {
        if (initialized) {
            throw new RuntimeException("You may not initialize another instance of BungeeAFK Core.");
        }

        injector = Guice.createInjector(
                Stage.PRODUCTION,
                platformModule
        );

        platform = injector.getInstance(BungeeAFKPlatform.class);
        config = injector.getInstance(PlatformConfig.class);
        injector.getInstance(AFKHandler.class).init();
        logger = platform.getLogger();
        Command.init();

        Caption.setCurrentLanguage(Language.ofIdentifier(config.getString("lang", "en")));
        initLanguages();

        initialized = true;
    }

    private static void initLanguages() {
        Caption.loadLanguage(Language.ENGLISH, ResourceUtil.readJsonResource("lang_en.json"));
        Caption.loadLanguage(Language.GERMAN, ResourceUtil.readJsonResource("lang_de.json"));
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Injector injector() {
        return injector;
    }

    public static PlatformConfig getConfig() {
        return config;
    }

    public static BungeeAFKPlatform platform() {
        return platform;
    }

}
