package net.fameless.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.fameless.core.BungeeAFK;
import net.fameless.core.BungeeAFKPlatform;
import net.fameless.core.ServerEnvironment;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Plugin(
    id = "bungeeafk",
    name = "BungeeAFK-Velocity",
    version = "2.5.0"
    ,description = "BungeeAFK for Velocity proxy"
    ,url = "https://github.com/Fameless9"
    ,authors = {"Fameless9"}
)
public class VelocityPlatform implements BungeeAFKPlatform {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/" + VelocityPlatform.class.getSimpleName());
    private static VelocityPlatform instance;

    private final ProxyServer proxyServer;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityPlatform(ProxyServer proxyServer, Metrics.Factory metricsFactory) {
        this.proxyServer = proxyServer;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onShutDown(ProxyShutdownEvent event) {
        BungeeAFK.handleShutdown();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        long startTime = System.currentTimeMillis();
        instance = this;

        VelocityCommandHandler commandHandler = new VelocityCommandHandler();
        proxyServer.getCommandManager().register("bungeeafk", commandHandler, "bafk");
        proxyServer.getCommandManager().register("afk", commandHandler);

        BungeeAFK.initCore(new VelocityModule());

        metricsFactory.make(this, 25577);
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Successfully enabled. (took {}ms)", duration);
    }

    public static ProxyServer getProxy() {
        return instance.proxyServer;
    }

    public static VelocityPlatform get() {
        return instance;
    }

    @Override
    public boolean doesServerExist(String serverName) {
        return proxyServer.getServer(serverName).isPresent();
    }

    @Override
    public List<String> getServers() {
        return proxyServer.getAllServers().stream()
                .map(server -> server.getServerInfo().getName())
                .toList();
    }

    @Override
    public ServerEnvironment getServerEnvironment() {
        return ServerEnvironment.PROXY;
    }
}
