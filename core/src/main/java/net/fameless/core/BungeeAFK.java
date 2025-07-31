package net.fameless.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.fameless.core.caption.Caption;
import net.fameless.core.caption.Language;
import net.fameless.core.command.framework.Command;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.Action;
import net.fameless.core.location.Location;
import net.fameless.core.util.PluginUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BungeeAFK {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + BungeeAFK.class.getSimpleName());
    private static boolean initialized = false;
    private static BungeeAFKPlatform platform;
    private static AFKHandler afkHandler;

    public static synchronized void initCore(AbstractModule platformModule) {
        if (initialized) {
            throw new RuntimeException("You may not initialize another instance of BungeeAFK Core.");
        }
        LOGGER.info("Initializing Core...");

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                platformModule
        );

        PluginConfig.init();
        LOGGER.info("Configured AFK-Location: {}", Location.getConfiguredAfkZone());
        PluginUpdater.runTask();

        platform = injector.getInstance(BungeeAFKPlatform.class);
        afkHandler = injector.getInstance(AFKHandler.class);

        checkForMisconfiguration();

        if (!Action.isAfkServerConfigured()) {
            LOGGER.warn("AFK server is not configured. This may cause players to be kicked instead of moved to an AFK server. Ignore if 'connect' action is not used.");
        }

        Command.init();

        Caption.loadDefaultLanguages();
        Caption.setCurrentLanguage(Language.ofIdentifier(PluginConfig.get().getString("lang", "en")));

        initialized = true;
    }

    public static void handleShutdown() {
        if (!initialized) return;
        Caption.saveToFile();
        PluginConfig.shutdown();
        afkHandler.shutdown();
    }

    private static void checkForMisconfiguration() {
        String misconfiguredMessage = "";
        if (afkHandler.getWarnDelayMillis() > afkHandler.getAfkDelayMillis()) {
            misconfiguredMessage += "'Warn delay is greater than AFK delay'";
        }
        if (afkHandler.getWarnDelayMillis() > afkHandler.getActionDelayMillis()) {
            misconfiguredMessage += "'Warn delay is greater than action delay'";
        }
        if (afkHandler.getAfkDelayMillis() > afkHandler.getActionDelayMillis()) {
            misconfiguredMessage += "'AFK delay is greater than action delay'";
        }
        if (!misconfiguredMessage.isEmpty()) {
            LOGGER.warn("Misconfiguration detected: {} - This may cause unexpected behavior. Falling back to default configuration.", misconfiguredMessage);
            PluginConfig.get().set("warning-delay", 90);
            PluginConfig.get().set("afk-delay", 180);
            PluginConfig.get().set("action-delay", 420);
            afkHandler.fetchConfigValues();
        }
    }

    public static AFKHandler getAFKHandler() {
        return afkHandler;
    }

    public static BungeeAFKPlatform getPlatform() {
        return platform;
    }
}
