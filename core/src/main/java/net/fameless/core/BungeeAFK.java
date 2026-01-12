package net.fameless.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import me.neznamy.tab.api.TabAPI;
import net.fameless.core.caption.Caption;
import net.fameless.core.command.framework.Command;
import net.fameless.core.config.Config;
import net.fameless.core.detection.autoclicker.AutoClickerDetector;
import net.fameless.core.detection.history.DetectionHistoryManager;
import net.fameless.core.detection.movementpattern.MovementPatternDetection;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.Action;
import net.fameless.core.network.NettyServerBootstrap;
import net.fameless.core.tab.TabPlaceholder;
import net.fameless.core.util.ColorUtil;
import net.fameless.core.util.PluginUpdater;
import net.fameless.core.util.SchedulerService;
import net.fameless.core.util.cache.ExpirableMap;
import net.fameless.core.util.cache.ExpirableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BungeeAFK {

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/" + BungeeAFK.class.getSimpleName());
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
        logger.info(startupMessage);

        logger.info("Initializing Core...");

        Injector injector = Guice.createInjector(
                Stage.PRODUCTION,
                platformModule
        );

        PluginUpdater.runTask();

        platform = injector.getInstance(BungeeAFKPlatform.class);
        afkHandler = injector.getInstance(AFKHandler.class);
        autoClickerDetector = new AutoClickerDetector();
        movementPatternDetection = new MovementPatternDetection();
        DetectionHistoryManager.loadDetections();

        checkForMisconfiguration();

        if (!Action.isAfkServerConfigured()) {
            logger.warn("AFK server is not configured. This may cause players to be kicked instead of being moved to the AFK server. Ignore if 'connect' action is not used.");
        }

        Command.init();

        Caption.loadLanguageFiles();

        String configuredLanguage = Config.getInstance().getString("lang", "en");
        if (!Caption.existsLanguage(configuredLanguage)) {
            String fallBack = Caption.getAvailableLanguages().stream().findFirst().orElseThrow();
            logger.warn("Configured language does not exists: {}. Falling back to: {}", configuredLanguage, fallBack);
            Caption.setCurrentLanguage(fallBack);
        } else {
            Caption.setCurrentLanguage(configuredLanguage);
        }

        logger.info("initializing BungeeAFK API...");
        new BungeeAFKAPIImpl();

        try {
            NettyServerBootstrap.initializeServer();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while initializing netty Socket", e);
        }

        try {
            TabAPI.getInstance();
            logger.info("TAB instance detected, registering placeholders...");
            TabPlaceholder.register();
        } catch (Exception e) {
            logger.info("No TAB instance detected. Placeholders are unavailable");
        }

        initialized = true;
    }

    public static void handleShutdown() {
        if (!initialized) return;
        Caption.saveToFile();
        DetectionHistoryManager.saveDetections();
        if (Config.getInstance().getConfigRegistry().hasConfigFileChanged()) {
            if (!Config.getInstance().getBoolean("overwrite-file-changes", true)) {
                logger.info("Configuration file changed on disk during runtime - skipping save to avoid overwriting external edits");
            } else {
                logger.info("Configuration file changed on disk during runtime - overwriting file with cached config values");
                Config.getInstance().saveConfigAsync();
            }
        } else Config.getInstance().saveConfigAsync();
        afkHandler.shutdown();
        NettyServerBootstrap.shutdownServer();
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
            logger.warn("Misconfiguration detected: {} - This may cause unexpected behavior. Falling back to default configuration.", misconfiguredMessage);
            Config.getInstance().set("warning-delay", 90);
            Config.getInstance().set("afk-delay", 180);
            Config.getInstance().set("action-delay", 420);
            afkHandler.reloadConfigValues();
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
