package com.escapeprotocol.puzzles;

import com.escapeprotocol.rooms.RoomId;
import com.escapeprotocol.util.Validators;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The Office puzzle (FR-PZ-02): three documents are dated 1998, 2004 and 2012, and the player must
 * type the year that is the password of the office computer.
 *
 * <p>Input: a four-digit year, checked by {@link Validators#parseYear(String)}. {@code 2004} is
 * CORRECT, every other four-digit year is INCORRECT, anything else is INVALID_FORMAT.
 *
 * <p>The class has no state, so {@link #submit(String)} is pure and total. The reward (the Office
 * Key) is added by the game session when the room is completed, never by this class (FR-IV-02).
 */
public final class OfficePuzzle implements Puzzle {

    /** The puzzle id. */
    public static final String ID = "office_year";

    /** The correct year. */
    public static final String CORRECT_YEAR = "2004";

    private static final String PROMPT =
            "Three documents lie on the desk:\n"
            + "  Document A, dated 1998: a cancelled supply order.\n"
            + "  Document B, dated 2004: the setup sheet of the office computer, with a password hint.\n"
            + "  Document C, dated 2012: a letter about moving the archive.\n"
            + "The computer asks for a password. Enter the year (4 digits).";

    private static final List<String> HINTS = List.of(
            "Compare the dates on all three documents.",
            "Look for the document that is relevant to the office computer.",
            "The correct document is dated 2004.");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RoomId roomId() {
        return RoomId.OFFICE;
    }

    @Override
    public String prompt() {
        return PROMPT;
    }

    @Override
    public PuzzleResult submit(String answer) {
        Optional<String> year = Validators.parseYear(answer);
        if (year.isEmpty()) {
            return new PuzzleResult(PuzzleResult.Status.INVALID_FORMAT,
                    "Please enter a year with exactly 4 digits, for example 1998.");
        }
        if (year.get().equals(CORRECT_YEAR)) {
            return new PuzzleResult(PuzzleResult.Status.CORRECT,
                    "Correct! The computer accepts the password and a drawer opens.");
        }
        return new PuzzleResult(PuzzleResult.Status.INCORRECT, "The computer rejects that year.");
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
