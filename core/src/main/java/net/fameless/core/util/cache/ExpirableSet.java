package net.fameless.core.util.cache;

import java.util.Set;
import java.util.concurrent.*;

/**
 * A thread-safe set whose entries can expire after a configurable delay.
 *
 * <p>Each element may be added with a time-to-live (TTL). When the TTL elapses,
 * the element is removed asynchronously by a shared daemon {@link ScheduledExecutorService}.
 * {@link #addOrRefresh(Object, long, TimeUnit)} resets the TTL for an existing element.</p>
 *
 * <p>Notes:
 * <ul>
 *   <li>Scheduling is best-effort; an element may briefly remain after its TTL.</li>
 *   <li>{@link #clear()} does not cancel already scheduled removals; those tasks will still
 *       execute and clean up their bookkeeping.</li>
 *   <li>Negative delays are not permitted and will cause an {@link IllegalArgumentException} from the scheduler.</li>
 *   <li>Null elements are not supported by the underlying concurrent collections.</li>
 * </ul>
 * </p>
 * @author Nicolai Greeven
 * @param <E> the element type
 */
public class ExpirableSet<E> {

    /**
     * Single-threaded, daemon scheduler shared by all {@code ExpirableSet} instances.
     * The daemon thread will not prevent JVM shutdown.
     */
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ExpirableSet-Scheduler");
                t.setDaemon(true);
                return t;
            });

    /**
     * Tracks the scheduled removal task for each element currently scheduled.
     * Used to cancel and reschedule TTLs.
     */
    private final ConcurrentHashMap<E, ScheduledFuture<?>> expiryTasks = new ConcurrentHashMap<>();

    /**
     * Backing concurrent set holding the current elements.
     */
    private final Set<E> set = ConcurrentHashMap.newKeySet();

    /**
     * Adds the element and schedules its removal after the given delay.
     * If the element already exists, no new schedule is created.
     *
     * @param element the element to add
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit the time unit of {@code removalDelay}; must not be {@code null}
     * @return {@code true} if the set changed, {@code false} otherwise
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException if {@code element} or {@code timeUnit} is {@code null}
     */
    public boolean add(E element, long removalDelay, TimeUnit timeUnit) {
        boolean added = set.add(element);
        if (added) {
            scheduleRemoval(element, removalDelay, timeUnit);
        }
        return added;
    }

    /**
     * Adds the element without scheduling expiration.
     *
     * @param element the element to add
     * @return {@code true} if the set changed, {@code false} otherwise
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public boolean add(E element) {
        return set.add(element);
    }

    /**
     * Adds the element if absent and schedules (or reschedules) its removal after the given delay.
     * If the element was already present, its previous scheduled task is canceled and replaced.
     *
     * @param element the element to add or refresh
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit the time unit of {@code removalDelay}; must not be {@code null}
     * @return {@code true} if the element was newly added, {@code false} if it already existed
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException if {@code element} or {@code timeUnit} is {@code null}
     */
    public boolean addOrRefresh(E element, long removalDelay, TimeUnit timeUnit) {
        boolean alreadyPresent = set.contains(element);
        set.add(element);
        scheduleRemoval(element, removalDelay, timeUnit);
        return !alreadyPresent;
    }

    /**
     * Schedules removal for an element that is currently in the set.
     * If a prior removal task exists, it is canceled and replaced.
     *
     * @param element the element for which to schedule removal
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit the time unit of {@code removalDelay}; must not be {@code null}
     * @return {@code true} if the element is contained and was (re)scheduled, {@code false} otherwise
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException if {@code element} or {@code timeUnit} is {@code null}
     */
    public boolean scheduleRemoval(E element, long removalDelay, TimeUnit timeUnit) {
        if (expiryTasks.containsKey(element)) {
            // Cancel any previously scheduled task (if not yet run).
            expiryTasks.get(element).cancel(true);
        }

        boolean contains = contains(element);
        if (contains) {
            ScheduledFuture<?> t = scheduler.schedule(() -> {
                // Remove the element and clear the bookkeeping entry.
                set.remove(element);
                expiryTasks.remove(element);
            }, removalDelay, timeUnit);

            expiryTasks.put(element, t);
        }

        return contains;
    }

    /**
     * Removes the element immediately, without interacting with any scheduled task.
     * Any previously scheduled task will still execute later and attempt cleanup, which is harmless.
     *
     * @param element the element to remove
     * @return {@code true} if the element was present and removed, {@code false} otherwise
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public boolean removeNow(E element) {
        return set.remove(element);
    }

    /**
     * Checks whether the element is currently present.
     *
     * @param element the element to check
     * @return {@code true} if present, {@code false} otherwise
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public boolean contains(E element) {
        return set.contains(element);
    }

    /**
     * Removes all elements from the set.
     * Scheduled tasks are not canceled and will run at their due time to clear their bookkeeping.
     */
    public void clear() {
        set.clear();
    }

    /**
     * Returns the number of elements currently present.
     *
     * @return the current size of the set
     */
    public int size() {
        return set.size();
    }

    /**
     * Shuts down the shared daemon scheduler immediately, attempting to cancel
     * all actively executing and pending tasks.
     *
     * <p>This method should typically only be called during application shutdown.
     * After calling this method, no new removal tasks can be scheduled by any
     * {@code ExpirableSet} instance, and attempts to do so will cause exceptions.</p>
     *
     * <p>Note: The scheduler thread is a daemon, so the JVM will not wait for it
     * to terminate before exiting. Calling this method is optional but can help
     * ensure clean shutdown.</p>
     */
    public static void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
