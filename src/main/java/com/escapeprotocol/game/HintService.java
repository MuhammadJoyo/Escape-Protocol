package com.escapeprotocol.game;

import java.util.List;
import java.util.Objects;

/**
 * Decides what a hint request or hint view returns. It follows the progressive-hint rules
 * (FR-IV-04, FR-IV-05).
 *
 * <p>The service is stateless and never changes the game. The caller (the game session) remembers
 * how many hints of a puzzle are already revealed, passes that number in, and applies the
 * result: only {@link HintResult.Status#REVEALED_NEW} increases the revealed count and costs
 * {@link ScoreCalculator#HINT_PENALTY}. Every other status is free.
 *
 * <p>Hint levels are 1-based. At most {@link #MAX_HINTS} hints per puzzle are available; a puzzle
 * that supplies fewer has fewer available.
 */
public final class HintService {

    /** Maximum number of hints per puzzle (FR-IV-04). */
    public static final int MAX_HINTS = 3;

    /**
     * Reveals the next hint, if there is one.
     *
     * @param hints the puzzle's hint texts in order; must not be {@code null}, and no element may
     *        be {@code null} or blank
     * @param revealedCount how many hints are already revealed, from 0 to the number available
     * @return {@code REVEALED_NEW} with the next hint text and its level, or {@code NO_MORE_HINTS}
     *         when all available hints are already revealed
     * @throws NullPointerException if {@code hints} is {@code null}
     * @throws IllegalArgumentException if a hint is null or blank, or {@code revealedCount} is
     *         negative or larger than the number of available hints
     */
    public HintResult request(List<String> hints, int revealedCount) {
        int available = availableHints(hints);
        checkRevealedCount(revealedCount, available);
        if (revealedCount >= available) {
            return new HintResult(HintResult.Status.NO_MORE_HINTS,
                    "There are no more hints for this puzzle.", 0);
        }
        int level = revealedCount + 1;
        return new HintResult(HintResult.Status.REVEALED_NEW, hints.get(revealedCount), level);
    }

    /**
     * Shows an already revealed hint again.
     *
     * @param hints the puzzle's hint texts in order (same rules as {@link #request})
     * @param revealedCount how many hints are already revealed
     * @param level the 1-based level to view
     * @return {@code VIEWED_AGAIN} with the hint text if {@code 1 <= level <= revealedCount};
     *         otherwise {@code REFUSED}
     * @throws NullPointerException if {@code hints} is {@code null}
     * @throws IllegalArgumentException if a hint is null or blank, or {@code revealedCount} is
     *         negative or larger than the number of available hints
     */
    public HintResult view(List<String> hints, int revealedCount, int level) {
        int available = availableHints(hints);
        checkRevealedCount(revealedCount, available);
        if (level < 1 || level > revealedCount) {
            return new HintResult(HintResult.Status.REFUSED,
                    "That hint has not been revealed yet.", 0);
        }
        return new HintResult(HintResult.Status.VIEWED_AGAIN, hints.get(level - 1), level);
    }

    private static int availableHints(List<String> hints) {
        Objects.requireNonNull(hints, "hints must not be null");
        for (String hint : hints) {
            if (hint == null || hint.isBlank()) {
                throw new IllegalArgumentException("Hint texts must not be null or blank.");
            }
        }
        return Math.min(hints.size(), MAX_HINTS);
    }

    private static void checkRevealedCount(int revealedCount, int available) {
        if (revealedCount < 0 || revealedCount > available) {
            throw new IllegalArgumentException(
                    "revealedCount must be between 0 and " + available + ": " + revealedCount);
        }
    }
}
