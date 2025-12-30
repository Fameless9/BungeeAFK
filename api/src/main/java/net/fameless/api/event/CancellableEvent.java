package net.fameless.api.event;

public interface CancellableEvent {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

}
