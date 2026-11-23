package com.escapeprotocol.puzzles;

import com.escapeprotocol.rooms.RoomId;
import com.escapeprotocol.util.Validators;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The Final Exit puzzle (FR-PZ-09, FR-PZ-10): the player combines the three earlier discoveries
 * into one code {@code YYYY-CCCC-N}: the Office year, the Library code and the Laboratory number.
 * The correct answer is {@code 2004-7394-30}.
 *
 * <p>Input: checked by {@link Validators#parseFinalExitCode(String)}. The year and the code are
 * compared as text, the number is compared as an integer ({@code 030} counts as 30). Wrong input
 * format is INVALID_FORMAT. A well-formed but wrong code is INCORRECT, and the feedback says
 * <em>how many</em> of the three components are right, never <em>which</em> ones.
 *
 * <p>The puzzle declares that the player must own all three clue items before answering
 * ({@link #requiredItemIds()}). The game session performs that check and tells the player which
 * <em>kind</em> of clue is missing. This class does not check the inventory, award items or
 * change the score. It has no state, so {@link #submit(String)} is pure and total.
 *
 * <p>The three item ids are the reward ids of {@code GameCatalog}; a test keeps both in step
 * (this package must not depend on the {@code game} package).
 */
public final class FinalExitPuzzle implements Puzzle {

    /** The puzzle id. */
    public static final String ID = "final_exit";

    /** The correct Office year (first component). */
    public static final String CORRECT_YEAR = "2004";

    /** The correct Library code (second component). */
    public static final String CORRECT_CODE = "7394";

    /** The correct Laboratory number (third component). */
    public static final int CORRECT_NUMBER = 30;

    /** The ids of the three clue items that must be in the inventory. */
    public static final Set<String> REQUIRED_ITEM_IDS = Set.of("office_key", "code_7394", "lab_clue_30");

    private static final int COMPONENT_COUNT = 3;

    private static final String PROMPT =
            "The exit door has a keypad. It accepts one code made of three parts:\n"
            + "  the year from the Office, the code from the Library, and the number from the Laboratory.\n"
            + "Enter the code in the format YYYY-CCCC-N.";

    private static final List<String> HINTS = List.of(
            "The final code combines three earlier discoveries.",
            "Keep the year, the library code and the laboratory number in that order.",
            "Use the format YYYY-CCCC-N.");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RoomId roomId() {
        return RoomId.FINAL_EXIT;
    }

    @Override
    public String prompt() {
        return PROMPT;
    }

    @Override
    public PuzzleResult submit(String answer) {
        Optional<List<String>> parts = Validators.parseFinalExitCode(answer);
        if (parts.isEmpty()) {
            return new PuzzleResult(PuzzleResult.Status.INVALID_FORMAT,
                    "Please enter the code as YYYY-CCCC-N, for example 1234-5678-9.");
        }
        int correct = countCorrectComponents(parts.get());
        if (correct == COMPONENT_COUNT) {
            return new PuzzleResult(PuzzleResult.Status.CORRECT, "The keypad beeps and the door unlocks.");
        }
        return new PuzzleResult(PuzzleResult.Status.INCORRECT,
                "Wrong code. " + correct + " of " + COMPONENT_COUNT + " components are correct.");
    }

    private static int countCorrectComponents(List<String> parts) {
        int correct = 0;
        if (parts.get(0).equals(CORRECT_YEAR)) {
            correct++;
        }
        if (parts.get(1).equals(CORRECT_CODE)) {
            correct++;
        }
        if (Integer.parseInt(parts.get(2)) == CORRECT_NUMBER) {
            correct++;
        }
        return correct;
    }

    @Override
    public List<String> hints() {
        return HINTS;
    }

    @Override
    public Set<String> requiredItemIds() {
        return REQUIRED_ITEM_IDS;
    }
}
