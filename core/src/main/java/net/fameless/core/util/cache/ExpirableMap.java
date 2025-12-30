package net.fameless.core.util.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * A thread-safe map whose entries expire after a configurable delay.
 *
 * <p>Each key can be associated with a value and an optional time-to-live (TTL).
 * When the TTL elapses, the key is removed asynchronously by a shared daemon
 * {@link ScheduledExecutorService}. Methods like {@link #addOrRefresh(Object, Object, long, TimeUnit)}
 * reset the TTL for an existing key.</p>
 *
 * <p>Notes:
 * <ul>
 *   <li>Scheduling is best-effort; an entry may briefly remain after its TTL.</li>
 *   <li>{@link #clear()} does not cancel already scheduled removals; those tasks will still
 *       execute and clean up their bookkeeping.</li>
 *   <li>Negative delays are not permitted and will cause an {@link IllegalArgumentException} from the scheduler.</li>
 *   <li>Null keys and values are not supported by {@link ConcurrentHashMap} and will cause {@link NullPointerException}.</li>
 * </ul>
 * </p>
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author Nicolai Greeven
 */
public class ExpirableMap<K, V> {

    /**
     * Single-threaded, daemon scheduler shared by all {@code ExpirableMap} instances.
     * The daemon thread will not prevent JVM shutdown.
     */
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ExpirableMap-Scheduler");
                t.setDaemon(true);
                return t;
            });

    /**
     * Tracks the scheduled removal task for each key currently scheduled.
     * Used to cancel and reschedule TTLs.
     */
    private final ConcurrentHashMap<K, ScheduledFuture<?>> expiryTasks = new ConcurrentHashMap<>();

    /**
     * Backing concurrent map holding the current entries.
     */
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    /**
     * Adds the entry if absent and schedules its removal after the given delay.
     * If the key already exists, no new schedule is created and the map is not modified.
     *
     * @param key          the key to add
     * @param val          the value to associate with {@code key}
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit     the time unit of {@code removalDelay}; must not be {@code null}
     *
     * @return {@code true} if the entry was added, {@code false} if the key already existed
     *
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException     if {@code key}, {@code val}, or {@code timeUnit} is {@code null}
     */
    public boolean add(K key, V val, long removalDelay, TimeUnit timeUnit) {
        if (map.putIfAbsent(key, val) == null) {
            scheduleRemoval(key, removalDelay, timeUnit);
            return true;
        }
        return false;
    }

    /**
     * Adds the entry or refreshes the TTL if the key already exists.
     * The value is replaced and the prior scheduled task (if any) is canceled and rescheduled.
     *
     * @param key          the key to add or refresh
     * @param val          the value to associate with {@code key}
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit     the time unit of {@code removalDelay}; must not be {@code null}
     *
     * @return {@code true} if the key was newly added, {@code false} if it already existed
     *
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException     if {@code key}, {@code val}, or {@code timeUnit} is {@code null}
     */
    public boolean addOrRefresh(K key, V val, long removalDelay, TimeUnit timeUnit) {
        boolean contains = containsKey(key);
        map.put(key, val);
        scheduleRemoval(key, removalDelay, timeUnit);
        return !contains;
    }

    /**
     * Adds the entry only if absent and schedules its removal after the given delay.
     * If the key already exists, nothing is changed.
     *
     * @param key          the key to add if absent
     * @param val          the value to associate with {@code key}
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit     the time unit of {@code removalDelay}; must not be {@code null}
     *
     * @return {@code true} if the entry was added, {@code false} otherwise
     *
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException     if {@code key}, {@code val}, or {@code timeUnit} is {@code null}
     */
    public boolean addIfAbsent(K key, V val, long removalDelay, TimeUnit timeUnit) {
        boolean contains = containsKey(key);
        if (!contains) {
            map.put(key, val);
            scheduleRemoval(key, removalDelay, timeUnit);
        }
        return !contains;
    }

    /**
     * Associates the value with the key without scheduling expiration.
     *
     * @param key the key
     * @param val the value
     *
     * @return the previous value associated with {@code key}, or {@code null} if none
     *
     * @throws NullPointerException if {@code key} or {@code val} is {@code null}
     */
    public V add(K key, V val) {
        return map.put(key, val);
    }

    /**
     * Associates the value with the key if absent, without scheduling expiration.
     *
     * @param key the key
     * @param val the value
     *
     * @throws NullPointerException if {@code key} or {@code val} is {@code null}
     */
    public void addIfAbsent(K key, V val) {
        map.putIfAbsent(key, val);
    }

    /**
     * Schedules removal for a key currently present in the map.
     * If a prior removal task exists, it is canceled and replaced.
     *
     * @param key          the key for which to schedule removal
     * @param removalDelay the TTL duration; must be non-negative
     * @param timeUnit     the time unit of {@code removalDelay}; must not be {@code null}
     *
     * @return {@code true} if the key was contained and was (re)scheduled, {@code false} otherwise
     *
     * @throws IllegalArgumentException if {@code removalDelay} is negative
     * @throws NullPointerException     if {@code key} or {@code timeUnit} is {@code null}
     */
    public boolean scheduleRemoval(K key, long removalDelay, TimeUnit timeUnit) {
        if (expiryTasks.containsKey(key)) {
            expiryTasks.get(key).cancel(true);
        }

        if (map.containsKey(key)) {
            ScheduledFuture<?> t = scheduler.schedule(() -> {
                map.remove(key);
                expiryTasks.remove(key);
            }, removalDelay, timeUnit);

            expiryTasks.put(key, t);
        }

        return map.containsKey(key);
    }

    /**
     * Removes the entry immediately and cancels any scheduled removal task.
     *
     * @param key the key to remove
     *
     * @return {@code true} if an entry was present and removed, {@code false} otherwise
     *
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public boolean removeNow(K key) {
        ScheduledFuture<?> task = expiryTasks.remove(key);
        if (task != null) task.cancel(true);
        return map.remove(key) != null;
    }

    /**
     * Retrieves the value associated with the key, or {@code null} if absent.
     *
     * @param key the key
     *
     * @return the value or {@code null} if not present
     *
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public V get(K key) {
        return map.get(key);
    }

    /**
     * Retrieves the value associated with the key, or the provided default if absent.
     *
     * @param key          the key
     * @param defaultValue the value to return if {@code key} is not present
     *
     * @return the value or {@code defaultValue} if not present
     *
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public V getOrDefault(K key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Checks whether the key is present.
     *
     * @param key the key
     *
     * @return {@code true} if present, {@code false} otherwise
     *
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Checks whether the map contains the given value.
     *
     * @param val the value to check
     *
     * @return {@code true} if present, {@code false} otherwise
     *
     * @throws NullPointerException if {@code val} is {@code null}
     */
    public boolean containsValue(V val) {
        return map.containsValue(val);
    }

    /**
     * Removes all entries from the map.
     * Scheduled tasks are not canceled and will run at their due time to clear their bookkeeping.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns the number of entries currently present.
     *
     * @return the current size of the map
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns an unmodifiable view of the keys currently in the map.
     * The returned set is backed by the underlying {@link ConcurrentHashMap},
     * so changes to the map are reflected in the set.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Returns an unmodifiable view of the values currently in the map.
     * The returned collection is backed by the underlying {@link ConcurrentHashMap},
     * so changes to the map are reflected in the collection.
     *
     * @return a collection view of the values contained in this map
     */
    public Collection<V> values() {
        return map.values();
    }

    /**
     * Returns an unmodifiable view of the mappings currently in the map.
     * The returned set is backed by the underlying {@link ConcurrentHashMap},
     * so changes to the map are reflected in the set.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * Shuts down the shared daemon scheduler immediately, attempting to cancel
     * all actively executing and pending tasks.
     *
     * <p>This method should typically only be called during application shutdown.
     * After calling this method, no new removal tasks can be scheduled by any
     * {@code ExpirableMap} instance, and attempts to do so will cause exceptions.</p>
     *
     * <p>Note: The scheduler thread is a daemon, so the JVM will not wait for it
     * to terminate before exiting. Calling this method is optional but can help
     * ensure clean shutdown.</p>
     */
    public static void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
