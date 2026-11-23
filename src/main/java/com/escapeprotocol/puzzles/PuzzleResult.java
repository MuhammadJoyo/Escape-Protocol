package com.escapeprotocol.puzzles;

import java.util.Objects;

/**
 * Immutable result of checking one answer against one puzzle (FR-PZ-01).
 *
 * @param status whether the answer was correct, incorrect or badly formatted
 * @param feedback a user-readable message; never {@code null}
 */
public record PuzzleResult(Status status, String feedback) {

    /** The three possible outcomes of checking an answer. */
    public enum Status {
        /** The answer is well-formed and right. */
        CORRECT,
        /** The answer is well-formed but wrong. */
        INCORRECT,
        /** The answer does not have the format the puzzle expects. */
        INVALID_FORMAT
    }

    /**
     * Creates a result.
     *
     * @throws NullPointerException if {@code status} or {@code feedback} is {@code null}
     */
    public PuzzleResult {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(feedback, "feedback must not be null");
    }
}
