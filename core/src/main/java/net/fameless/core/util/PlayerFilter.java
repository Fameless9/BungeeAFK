package net.fameless.core.util;

import net.fameless.core.player.BAFKPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A {@link PlayerFilter} is a functional interface used to filter {@link BAFKPlayer} instances
 * based on custom conditions.
 * <p>
 * It extends {@link Predicate}, allowing compatibility with Java Streams and other functional utilities,
 * while providing a clearer method name {@link #accepts(BAFKPlayer)} for readability.
 * <p>
 * This interface also supports composition using logical operators such as {@link #and(PlayerFilter)},
 * {@link #or(PlayerFilter)}, and {@link #negate()}.
 */
@FunctionalInterface
public interface PlayerFilter extends Predicate<BAFKPlayer<?>> {

    /**
     * Tests whether the given player matches this filter.
     * This is the main method that should be implemented.
     *
     * @param player the {@link BAFKPlayer} to test, never null
     *
     * @return {@code true} if the player matches the filter, {@code false} otherwise
     */
    boolean accepts(final @NotNull BAFKPlayer<?> player);

    /**
     * Evaluates this filter against the given player.
     * Delegates to {@link #accepts(BAFKPlayer)}.
     *
     * @param player the {@link BAFKPlayer} to test
     *
     * @return {@code true} if the player matches the filter
     */
    @Override
    default boolean test(final BAFKPlayer<?> player) {
        return this.accepts(player);
    }

    /**
     * Combines this filter with another using logical AND.
     * The resulting filter returns {@code true} only if both filters return {@code true}.
     *
     * @param other another {@link PlayerFilter} to combine with
     *
     * @return a composed filter representing logical AND
     */
    default PlayerFilter and(final @NotNull PlayerFilter other) {
        return player -> this.accepts(player) && other.accepts(player);
    }

    /**
     * Combines this filter with another using logical OR.
     * The resulting filter returns {@code true} if either filter returns {@code true}.
     *
     * @param other another {@link PlayerFilter} to combine with
     *
     * @return a composed filter representing logical OR
     */
    default PlayerFilter or(final @NotNull PlayerFilter other) {
        return player -> this.accepts(player) || other.accepts(player);
    }

    /**
     * Inverts this filter using logical NOT.
     * The resulting filter returns {@code true} if this filter returns {@code false}, and vice versa.
     *
     * @return a negated version of this filter
     */
    default @NotNull PlayerFilter negate() {
        return player -> !this.accepts(player);
    }

    /**
     * Wraps a standard {@link Predicate} as a {@link PlayerFilter}.
     *
     * @param predicate the predicate to wrap
     *
     * @return a {@link PlayerFilter} that delegates to the given predicate
     */
    @Contract(pure = true)
    static @NotNull PlayerFilter of(final @NotNull Predicate<BAFKPlayer<?>> predicate) {
        return predicate::test;
    }
}
