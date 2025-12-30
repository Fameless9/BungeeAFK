package net.fameless.limbo;

import com.loohp.limbo.Limbo;
import org.jetbrains.annotations.NotNull;

public final class Logger {

    private static final String PREFIX = "[BungeeAFK-Tracking] ";

    private Logger() {}

    public static void info(String msg, Object... args) {
        send(formatted(msg, args));
    }

    private static @NotNull String formatted(String msg, Object... args) {
        if (msg == null) return "null";
        if (args == null || args.length == 0) return msg;

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int i = 0;
        while (i < msg.length()) {
            int j = msg.indexOf("{}", i);
            if (j == -1) {
                sb.append(msg.substring(i));
                break;
            }
            sb.append(msg, i, j);
            if (argIndex < args.length) {
                sb.append(args[argIndex++]);
            } else {
                sb.append("{}");
            }
            i = j + 2;
        }
        return sb.toString();
    }

    private static void send(String message) {
        Limbo.getInstance().getConsole().sendMessage(PREFIX + message);
    }
}
