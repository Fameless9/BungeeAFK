package net.fameless.core.util;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public class ServerPinger {

    public static CompletableFuture<Boolean> isOnline(SocketAddress address) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(address, 1000);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

}
