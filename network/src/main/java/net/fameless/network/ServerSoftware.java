package net.fameless.network;

public enum ServerSoftware {

    SPIGOT("Spigot"),
    LIMBO("Limbo");

    public final String friendlyName;

    ServerSoftware(String friendlyName) {this.friendlyName = friendlyName;}
}
