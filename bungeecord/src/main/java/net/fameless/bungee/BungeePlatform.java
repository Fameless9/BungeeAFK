package net.fameless.bungee;

import net.fameless.core.BungeeAFK;
import net.fameless.core.BungeeAFKPlatform;
import net.fameless.core.ServerEnvironment;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class BungeePlatform extends Plugin implements BungeeAFKPlatform {

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/" + BungeePlatform.class.getSimpleName());
    private static BungeePlatform instance;
    private static ProxyServer proxyServer;

    public static BungeePlatform get() {
        return instance;
    }

    public static ProxyServer proxyServer() {
        return proxyServer;
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;
        proxyServer = getProxy();

        getProxy().getPluginManager().registerCommand(this, new BungeeCommandHandler("bungeeafk", new String[]{"bafk"}));
        getProxy().getPluginManager().registerCommand(this, new BungeeCommandHandler("afk", new String[]{}));

        BungeeAFK.initCore(new BungeeModule());

        new Metrics(this, 25576);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Successfully enabled. (took {}ms)", duration);
    }

    @Override
    public void onDisable() {
        BungeeAFK.handleShutdown();
    }

    @Override
    public boolean doesServerExist(String serverName) {
        return getProxy().getServerInfo(serverName) != null;
    }

    @Override
    public List<String> getServers() {
        return getProxy().getServers().keySet().stream().toList();
    }

    @Override
    public ServerEnvironment getServerEnvironment() {
        return ServerEnvironment.PROXY;
    }
}
