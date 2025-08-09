package net.fameless.core;


import java.util.List;

public interface BungeeAFKPlatform {

    boolean doesServerExist(String serverName);

    List<String> getServers();

    ServerEnvironment getServerEnvironment();

}
