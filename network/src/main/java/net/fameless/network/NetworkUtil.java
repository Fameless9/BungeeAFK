package net.fameless.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NetworkUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String msg(MessageType type, Object payload) {
        return gson.toJson(new NetworkMessage(type.name(), gson.toJson(payload)));
    }

}
