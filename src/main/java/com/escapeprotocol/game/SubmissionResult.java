package com.escapeprotocol.game;

import java.util.Objects;

/**
 * Immutable outcome of one answer submission, returned by the game engine to the UI.
 *
 * @param status what happened (see {@link Status})
 * @param message a user-readable explanation; never {@code null}
 */
public record SubmissionResult(Status status, String message) {

    /** What happened to a submission. */
    public enum Status {
        /** Refused: the game is not in the RUNNING state. No penalty. */
        REFUSED_NOT_RUNNING,
        /** Refused: the puzzle's room is still locked. No penalty. */
        REFUSED_LOCKED,
        /** Refused: required clue items are missing (Final Exit). No penalty. */
        REFUSED_MISSING_CLUES,
        /** The puzzle was already solved earlier. No penalty, no new item. */
        ALREADY_SOLVED,
        /** The answer had the wrong format. No penalty. */
        INVALID_FORMAT,
        /** The answer was well-formed but wrong. Costs the wrong-attempt penalty. */
        INCORRECT,
        /** The answer was correct. */
        CORRECT,
        /** The correct Final Exit answer: the game is won. */
        VICTORY
    }

    /**
     * Creates a result.
     *
     * @throws NullPointerException if {@code status} or {@code message} is {@code null}
     */
    public SubmissionResult {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
