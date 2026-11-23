package com.escapeprotocol.puzzles;

import com.escapeprotocol.rooms.RoomId;
import com.escapeprotocol.util.Validators;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The Library puzzle (FR-PZ-04): four books are published in 1985 (Red), 1992 (Green), 2001 (Blue)
 * and 2010 (Yellow), and the player must put them in order from oldest to newest.
 *
 * <p>Input: four distinct book names separated by commas, for example
 * {@code RED,GREEN,BLUE,YELLOW}. Spaces around the names and upper or lower case are ignored
 * (see {@link Validators#parseLibraryOrder(String, java.util.Collection)}). The correct order is
 * CORRECT, every other order of the four books is INCORRECT, and a missing, duplicate, unknown
 * or extra book (or any other text) is INVALID_FORMAT.
 *
 * <p>The class has no state, so {@link #submit(String)} is pure and total. The reward (the code
 * 7394) is added by the game session when the room is completed, never by this class.
 */
public final class LibraryPuzzle implements Puzzle {

    /** The puzzle id. */
    public static final String ID = "library_order";

    /** The books from oldest (1985) to newest (2010): the correct answer. */
    public static final List<String> CORRECT_ORDER = List.of("RED", "GREEN", "BLUE", "YELLOW");

    private static final String PROMPT =
            "Four books stand on the shelf in no particular order:\n"
            + "  Blue book, published 2001\n"
            + "  Yellow book, published 2010\n"
            + "  Red book, published 1985\n"
            + "  Green book, published 1992\n"
            + "Put them in order from the oldest to the newest.\n"
            + "Type the four colours separated by commas, for example: RED,GREEN,BLUE,YELLOW";

    private static final List<String> HINTS = List.of(
            "Think about arranging the books by publication year.",
            "Start with the oldest publication and move forward.",
            "The order is Red, Green, Blue, Yellow.");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RoomId roomId() {
        return RoomId.LIBRARY;
    }

    @Override
    public String prompt() {
        return PROMPT;
    }

    @Override
    public PuzzleResult submit(String answer) {
        Optional<List<String>> order = Validators.parseLibraryOrder(answer, CORRECT_ORDER);
        if (order.isEmpty()) {
            return new PuzzleResult(PuzzleResult.Status.INVALID_FORMAT,
                    "Please list each of the four books exactly once, separated by commas.");
        }
        if (order.get().equals(CORRECT_ORDER)) {
            return new PuzzleResult(PuzzleResult.Status.CORRECT,
                    "Correct! The books slide into place and reveal a hidden code.");
        }
        return new PuzzleResult(PuzzleResult.Status.INCORRECT, "The shelf does not react to that order.");
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
