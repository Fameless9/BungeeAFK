package net.fameless.network;

public class NetworkMessage {

    public String type;
    public String payload;

    public NetworkMessage(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }
}
