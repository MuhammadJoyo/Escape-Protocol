package com.escapeprotocol.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.rooms.RoomId;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Contract checks that every real puzzle must pass (Puzzle Javadoc, FR-PZ-01): non-null id and
 * prompt, the right room, exactly three non-blank immutable hints, immutable required items, and a
 * pure and total {@code submit}. The same loops run over all four puzzles.
 */
class PuzzleContractTest {

    private static final List<Puzzle> PUZZLES = List.of(
            new OfficePuzzle(), new LibraryPuzzle(), new SequencePuzzle(), new FinalExitPuzzle());

    private static final Map<String, RoomId> EXPECTED_ROOMS = Map.of(
            "office_year", RoomId.OFFICE,
            "library_order", RoomId.LIBRARY,
            "lab_sequence", RoomId.LABORATORY,
            "final_exit", RoomId.FINAL_EXIT);

    private static final Map<String, String> CORRECT_ANSWERS = Map.of(
            "office_year", "2004",
            "library_order", "RED,GREEN,BLUE,YELLOW",
            "lab_sequence", "30",
            "final_exit", "2004-7394-30");

    private static final String[] GARBAGE = {null, "", " ", "   ", "@@@@", "\u0000", "abc def", "!!!",
            "9".repeat(10000), "\n\t"};

    @Test
    void idsAreNonBlankUniqueAndAsApproved() {
        Set<String> ids = new HashSet<>();
        for (Puzzle puzzle : PUZZLES) {
            assertNotNull(puzzle.id());
            assertFalse(puzzle.id().isBlank());
            assertTrue(ids.add(puzzle.id()), "duplicate id " + puzzle.id());
        }
        assertEquals(EXPECTED_ROOMS.keySet(), ids);
    }

    @Test
    void everyPuzzleBelongsToItsRoom() {
        for (Puzzle puzzle : PUZZLES) {
            assertEquals(EXPECTED_ROOMS.get(puzzle.id()), puzzle.roomId(), puzzle.id());
        }
    }

    @Test
    void everyPromptIsNonBlankAndStable() {
        for (Puzzle puzzle : PUZZLES) {
            assertNotNull(puzzle.prompt());
            assertFalse(puzzle.prompt().isBlank(), puzzle.id());
            assertEquals(puzzle.prompt(), puzzle.prompt());
        }
    }

    @Test
    void everyPuzzleHasExactlyThreeNonBlankDistinctHints() {
        for (Puzzle puzzle : PUZZLES) {
            List<String> hints = puzzle.hints();
            assertEquals(3, hints.size(), puzzle.id());
            assertEquals(3, new HashSet<>(hints).size(), puzzle.id() + " hints must differ");
            for (String hint : hints) {
                assertNotNull(hint);
                assertFalse(hint.isBlank(), puzzle.id());
            }
        }
    }

    @Test
    void hintListsCannotBeModified() {
        for (Puzzle puzzle : PUZZLES) {
            List<String> hints = puzzle.hints();
            assertThrows(UnsupportedOperationException.class, () -> hints.add("extra"), puzzle.id());
            assertThrows(UnsupportedOperationException.class, () -> hints.set(0, "changed"), puzzle.id());
            assertThrows(UnsupportedOperationException.class, hints::clear, puzzle.id());
            assertEquals(3, puzzle.hints().size());
        }
    }

    @Test
    void requiredItemIdsAreImmutableAndOnlyTheFinalExitHasAny() {
        for (Puzzle puzzle : PUZZLES) {
            Set<String> required = puzzle.requiredItemIds();
            assertNotNull(required);
            assertThrows(UnsupportedOperationException.class, () -> required.add("x"), puzzle.id());
            assertThrows(UnsupportedOperationException.class, required::clear, puzzle.id());
            if (puzzle.id().equals("final_exit")) {
                assertEquals(3, required.size());
            } else {
                assertTrue(required.isEmpty(), puzzle.id());
            }
        }
    }

    @Test
    void submitAlwaysReturnsANonNullResultWithFeedback() {
        for (Puzzle puzzle : PUZZLES) {
            for (String answer : GARBAGE) {
                PuzzleResult result = puzzle.submit(answer);
                assertNotNull(result, puzzle.id());
                assertNotNull(result.status());
                assertFalse(result.feedback().isBlank(), puzzle.id());
            }
            PuzzleResult correct = puzzle.submit(CORRECT_ANSWERS.get(puzzle.id()));
            assertNotNull(correct);
            assertFalse(correct.feedback().isBlank());
        }
    }

    @Test
    void invalidInputReturnsInvalidFormatInsteadOfThrowing() {
        for (Puzzle puzzle : PUZZLES) {
            for (String answer : GARBAGE) {
                assertEquals(PuzzleResult.Status.INVALID_FORMAT, puzzle.submit(answer).status(),
                        puzzle.id() + " with " + (answer == null ? "null" : "length " + answer.length()));
            }
        }
    }

    @Test
    void theCorrectAnswerIsCorrectForEveryPuzzle() {
        for (Puzzle puzzle : PUZZLES) {
            assertEquals(PuzzleResult.Status.CORRECT, puzzle.submit(CORRECT_ANSWERS.get(puzzle.id())).status(),
                    puzzle.id());
        }
    }

    @Test
    void anAnswerForAnotherPuzzleIsNeverAcceptedAsCorrect() {
        for (Puzzle puzzle : PUZZLES) {
            for (Map.Entry<String, String> other : CORRECT_ANSWERS.entrySet()) {
                if (!other.getKey().equals(puzzle.id())) {
                    assertFalse(puzzle.submit(other.getValue()).status() == PuzzleResult.Status.CORRECT,
                            puzzle.id() + " must not accept the answer of " + other.getKey());
                }
            }
        }
    }

    @Test
    void submitIsRepeatableAndDoesNotChangeThePuzzle() {
        for (Puzzle puzzle : PUZZLES) {
            List<String> hintsBefore = puzzle.hints();
            Set<String> requiredBefore = puzzle.requiredItemIds();
            String promptBefore = puzzle.prompt();

            String correct = CORRECT_ANSWERS.get(puzzle.id());
            PuzzleResult first = puzzle.submit(correct);
            assertEquals(first, puzzle.submit(correct), puzzle.id());
            PuzzleResult wrong1 = puzzle.submit("0000");
            PuzzleResult invalid1 = puzzle.submit("@@@");
            puzzle.submit("0000");
            assertEquals(wrong1, puzzle.submit("0000"), puzzle.id());
            assertEquals(invalid1, puzzle.submit("@@@"), puzzle.id());
            assertEquals(first, puzzle.submit(correct), puzzle.id() + " after other submissions");

            assertEquals(hintsBefore, puzzle.hints());
            assertEquals(requiredBefore, puzzle.requiredItemIds());
            assertEquals(promptBefore, puzzle.prompt());
        }
    }

    @Test
    void separateInstancesBehaveTheSame() {
        List<Puzzle> others = List.of(new OfficePuzzle(), new LibraryPuzzle(), new SequencePuzzle(),
                new FinalExitPuzzle());
        for (int i = 0; i < PUZZLES.size(); i++) {
            Puzzle a = PUZZLES.get(i);
            Puzzle b = others.get(i);
            assertEquals(a.id(), b.id());
            assertEquals(a.hints(), b.hints());
            assertEquals(a.submit("1234"), b.submit("1234"));
        }
    }

    @Test
    void puzzlesHoldNoInstanceStateSoTheyCannotTouchTheGame() {
        for (Puzzle puzzle : PUZZLES) {
            Class<?> type = puzzle.getClass();
            assertTrue(Modifier.isFinal(type.getModifiers()), type.getSimpleName() + " must be final");
            for (Field field : type.getDeclaredFields()) {
                assertTrue(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()),
                        type.getSimpleName() + "." + field.getName() + " must be a static final constant");
            }
        }
    }
}
