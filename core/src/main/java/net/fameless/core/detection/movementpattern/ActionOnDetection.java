package net.fameless.core.detection.movementpattern;

import org.jetbrains.annotations.NotNull;

public enum ActionOnDetection {

    KICK,
    CONNECT,
    TELEPORT,
    NOTHING;

    private final String IDENTIFIER = name().toLowerCase();

    public String getIdentifier() {
        return IDENTIFIER;
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
