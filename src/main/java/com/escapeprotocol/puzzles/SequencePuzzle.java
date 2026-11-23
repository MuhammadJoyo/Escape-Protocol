package com.escapeprotocol.puzzles;

import com.escapeprotocol.rooms.RoomId;
import com.escapeprotocol.util.Validators;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * The Laboratory sequence puzzle (FR-PZ-06): the display shows {@code 2, 6, 12, 20, ?} and the
 * player must type the next number, {@code 30}.
 *
 * <p>Input: an integer of at most nine digits with an optional minus sign, checked by
 * {@link Validators#parseSequenceAnswer(String)}; longer or non-integer input is INVALID_FORMAT
 * (so a huge number can never overflow). {@code 30} is CORRECT, every other integer is INCORRECT.
 *
 * <p>The class has no state, so {@link #submit(String)} is pure and total. The reward (the clue
 * containing 30) is added by the game session when the room is completed, never by this class.
 * Because the symbol puzzle (FR-PZ-07, P2) is deferred, this puzzle alone completes the Laboratory.
 */
public final class SequencePuzzle implements Puzzle {

    /** The puzzle id. */
    public static final String ID = "lab_sequence";

    /** The correct answer. */
    public static final int CORRECT_VALUE = 30;

    private static final String PROMPT =
            "A display shows a number sequence:\n"
            + "  2, 6, 12, 20, ?\n"
            + "Type the number that replaces the question mark.";

    private static final List<String> HINTS = List.of(
            "Look at the differences between consecutive numbers.",
            "The differences increase by two each time.",
            "The next difference is 10.");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RoomId roomId() {
        return RoomId.LABORATORY;
    }

    @Override
    public String prompt() {
        return PROMPT;
    }

    @Override
    public PuzzleResult submit(String answer) {
        OptionalInt value = Validators.parseSequenceAnswer(answer);
        if (value.isEmpty()) {
            return new PuzzleResult(PuzzleResult.Status.INVALID_FORMAT,
                    "Please enter a whole number with at most 9 digits.");
        }
        if (value.getAsInt() == CORRECT_VALUE) {
            return new PuzzleResult(PuzzleResult.Status.CORRECT,
                    "Correct! The display flashes green and a compartment opens.");
        }
        return new PuzzleResult(PuzzleResult.Status.INCORRECT, "The display flashes red.");
    }

    @Override
    public List<String> hints() {
        return HINTS;
    }

    @Override
    public Set<String> requiredItemIds() {
        return Set.of();
    }
}
