package net.fameless.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.fameless.core.caption.Caption;
import net.fameless.core.caption.Language;
import net.fameless.core.command.framework.Command;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.detection.autoclicker.AutoClickerDetector;
import net.fameless.core.detection.history.DetectionHistoryManager;
import net.fameless.core.detection.movementpattern.MovementPatternDetection;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.Action;
import net.fameless.core.scheduler.SchedulerService;
import net.fameless.core.util.ColorUtil;
import net.fameless.core.util.PluginUpdater;
import net.fameless.core.util.cache.ExpirableMap;
import net.fameless.core.util.cache.ExpirableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BungeeAFK {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + BungeeAFK.class.getSimpleName());
    private static boolean initialized = false;
    private static BungeeAFKPlatform platform;
    private static AFKHandler afkHandler;
    private static AutoClickerDetector autoClickerDetector;
    private static MovementPatternDetection movementPatternDetection;

    public static synchronized void initCore(AbstractModule platformModule) {
        if (initialized) {
            throw new RuntimeException("You may not initialize another instance of BungeeAFK Core.");
        }

        if (Runtime.version().feature() < 21) {
            throw new IllegalStateException("BungeeAFK requires at least Java 21");
        }

        String startupMessage = ColorUtil.ANSI_CYAN + """

                ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗███████╗ █████╗ ███████╗██╗  ██╗
                ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔════╝██╔══██╗██╔════╝██║ ██╔╝
                ██████╔╝██║   ██║██╔██╗ ██║██║  ███╗█████╗  █████╗  ███████║█████╗  █████╔╝
                ██╔══██╗██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██╔══╝  ██╔══██║██╔══╝  ██╔═██╗
                ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗███████╗██║  ██║██║     ██║  ██╗
                ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝  ╚═╝
                """ + ColorUtil.ANSI_GREEN + "Running BungeeAFK Version: %s".formatted(PluginUpdater.CURRENT_VERSION + ColorUtil.ANSI_RESET);
        LOGGER.info(startupMessage);

        LOGGER.info("Initializing Core...");

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                platformModule
        );

        PluginConfig.init();
        PluginUpdater.runTask();

        platform = injector.getInstance(BungeeAFKPlatform.class);
        afkHandler = injector.getInstance(AFKHandler.class);
        autoClickerDetector = new AutoClickerDetector();
        movementPatternDetection = new MovementPatternDetection();
        DetectionHistoryManager.loadDetections();

        checkForMisconfiguration();

        if (!Action.isAfkServerConfigured()) {
            LOGGER.warn("AFK server is not configured. This may cause players to be kicked instead of being moved to the AFK server. Ignore if 'connect' action is not used.");
        }

        Command.init();

        Caption.loadDefaultLanguages();
        Caption.setCurrentLanguage(Language.ofIdentifier(PluginConfig.get().getString("lang", "en")));

        LOGGER.info("initializing BungeeAFK API...");
        new BungeeAFKAPIImpl();

        initialized = true;
    }

    public static void handleShutdown() {
        if (!initialized) return;
        Caption.saveToFile();
        DetectionHistoryManager.saveDetections();
        if (PluginConfig.getConfigRegistry().hasConfigFileChanged()) {
            if (!PluginConfig.get().getBoolean("overwrite-file-changes", true)) {
                LOGGER.info("Configuration file changed on disk during runtime - skipping save to avoid overwriting external edits");
            } else {
                LOGGER.info("Configuration file changed on disk during runtime - overwriting file with cached config values");
                PluginConfig.saveNow();
            }
        } else PluginConfig.saveNow();
        afkHandler.shutdown();
        ExpirableMap.shutdownScheduler();
        ExpirableSet.shutdownScheduler();
        SchedulerService.shutdown();
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

    public static boolean isAPIAvailable() {
        try {
            Class.forName("net.fameless.api.service.BackendAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isProxy() {
        return platform.getServerEnvironment() == ServerEnvironment.PROXY;
    }

    public static AFKHandler getAFKHandler() {
        return afkHandler;
    }

    public static BungeeAFKPlatform getPlatform() {
        return platform;
    }

    public static AutoClickerDetector getAutoClickerDetector() {
        return autoClickerDetector;
    }

    public static MovementPatternDetection getMovementPatternDetection() {
        return movementPatternDetection;
    }
}
