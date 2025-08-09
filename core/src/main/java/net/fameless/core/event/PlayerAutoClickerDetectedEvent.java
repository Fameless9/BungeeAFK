package net.fameless.core.event;

import net.fameless.core.player.BAFKPlayer;

import java.util.function.Consumer;

public class PlayerAutoClickerDetectedEvent {

    private final BAFKPlayer<?> player;
    private Consumer<BAFKPlayer<?>> action;

    public PlayerAutoClickerDetectedEvent(BAFKPlayer<?> player, Consumer<BAFKPlayer<?>> action) {
        this.player = player;
        this.action = action;
    }

    public BAFKPlayer<?> getPlayer() {
        return player;
    }

    public Consumer<BAFKPlayer<?>> getAction() {
        return action;
    }

    public void setAction(Consumer<BAFKPlayer<?>> action) {
        if (action == null) {
            action = player -> {};
        }
        this.action = action;
    }
}
