package com.escapeprotocol.game;

import java.util.Objects;

/**
 * Immutable outcome of a hint request or hint view.
 *
 * @param status what happened (see {@link Status})
 * @param text the hint text, or a user-readable message when there is no hint to show; never {@code null}
 * @param level the 1-based hint level that was revealed or viewed, or 0 when no hint is shown
 */
public record HintResult(Status status, String text, int level) {

    /** What happened to a hint request or view. */
    public enum Status {
        /** A new hint was revealed. This is the only status that costs the hint penalty. */
        REVEALED_NEW,
        /** An already revealed hint was shown again. Free. */
        VIEWED_AGAIN,
        /** All available hints have been revealed. Free. */
        NO_MORE_HINTS,
        /** The request was refused (game not running, room locked, bad level, ...). Free. */
        REFUSED
    }

    /**
     * Creates a result.
     *
     * @throws NullPointerException if {@code status} or {@code text} is {@code null}
     * @throws IllegalArgumentException if {@code level} is negative
     */
    public HintResult {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(text, "text must not be null");
        if (level < 0) {
            throw new IllegalArgumentException("level must not be negative: " + level);
        }
    }
}
