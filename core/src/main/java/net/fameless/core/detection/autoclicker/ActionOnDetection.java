package net.fameless.core.detection.autoclicker;

import org.jetbrains.annotations.NotNull;

public enum ActionOnDetection {

    KICK("kick"),
    OPEN_INVENTORY("open-inv"),
    NOTHING("nothing");

    private final String identifier;

    ActionOnDetection(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static boolean existsByIdentifier(String identifier) {
        for (ActionOnDetection action : values()) {
            if (action.getIdentifier().equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    public static @NotNull ActionOnDetection fromIdentifier(String identifier) throws IllegalArgumentException {
        for (ActionOnDetection action : values()) {
            if (action.getIdentifier().equalsIgnoreCase(identifier)) {
                return action;
            }
        }
        throw new IllegalArgumentException("No action found for identifier: " + identifier);
    }

}
