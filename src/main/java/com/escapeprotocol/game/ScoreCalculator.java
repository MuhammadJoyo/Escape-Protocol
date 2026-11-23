package com.escapeprotocol.game;

/**
 * Pure scoring rules (FR-TS-04, FR-TS-05). It has no state and never changes anything; the game
 * session passes in the current score and stores the returned value.
 *
 * <p>Rules: the score never drops below 0; invalid-format answers and re-viewing a hint cost
 * nothing, so they never call this class. All scores passed in must be 0 or more.
 */
public final class ScoreCalculator {

    /** Score at the start of every game. */
    public static final int INITIAL_SCORE = 1000;

    /** Points lost for a wrong (but well-formed) answer. */
    public static final int WRONG_ATTEMPT_PENALTY = 25;

    /** Points lost for revealing a new hint. */
    public static final int HINT_PENALTY = 50;

    /** Points gained per remaining second on victory. */
    public static final int POINTS_PER_REMAINING_SECOND = 2;

    /**
     * Applies the wrong-attempt penalty.
     *
     * @param score the current score; must be 0 or more
     * @return {@code max(0, score - 25)}
     * @throws IllegalArgumentException if {@code score} is negative
     */
    public int afterWrongAttempt(int score) {
        return subtractWithFloor(score, WRONG_ATTEMPT_PENALTY);
    }

    /**
     * Applies the penalty for a newly revealed hint.
     *
     * @param score the current score; must be 0 or more
     * @return {@code max(0, score - 50)}
     * @throws IllegalArgumentException if {@code score} is negative
     */
    public int afterHint(int score) {
        return subtractWithFloor(score, HINT_PENALTY);
    }

    /**
     * Computes the final score on victory: the current score plus 2 points per remaining second.
     * The sum is calculated with {@code long}, so it cannot overflow; a result above
     * {@link Integer#MAX_VALUE} is capped at {@link Integer#MAX_VALUE}.
     *
     * @param score the current score; must be 0 or more
     * @param remainingSeconds the seconds left; must be 0 or more
     * @return {@code score + 2 * remainingSeconds}, capped at {@code Integer.MAX_VALUE}
     * @throws IllegalArgumentException if {@code score} or {@code remainingSeconds} is negative
     */
    public int victoryScore(int score, int remainingSeconds) {
        requireNotNegative(score, "score");
        requireNotNegative(remainingSeconds, "remainingSeconds");
        long total = (long) score + (long) POINTS_PER_REMAINING_SECOND * remainingSeconds;
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    private static int subtractWithFloor(int score, int penalty) {
        requireNotNegative(score, "score");
        return Math.max(0, score - penalty);
    }

    private static void requireNotNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative: " + value);
        }
    }
}
