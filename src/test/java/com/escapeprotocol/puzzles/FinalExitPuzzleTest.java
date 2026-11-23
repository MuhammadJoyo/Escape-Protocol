package com.escapeprotocol.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.rooms.RoomId;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FinalExitPuzzleTest {

    private final FinalExitPuzzle puzzle = new FinalExitPuzzle();

    private PuzzleResult.Status statusOf(String answer) {
        return puzzle.submit(answer).status();
    }

    @Test
    void hasTheDocumentedIdAndRoom() {
        assertEquals("final_exit", puzzle.id());
        assertEquals(RoomId.FINAL_EXIT, puzzle.roomId());
    }

    @Test
    void declaresTheThreeRequiredClueItems() {
        assertEquals(Set.of("office_key", "code_7394", "lab_clue_30"), puzzle.requiredItemIds());
        assertThrows(UnsupportedOperationException.class, () -> puzzle.requiredItemIds().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> puzzle.requiredItemIds().clear());
    }

    @Test
    void promptExplainsTheFormatWithoutGivingTheAnswer() {
        assertTrue(puzzle.prompt().contains("YYYY-CCCC-N"));
        assertFalse(puzzle.prompt().contains("2004-7394-30"));
        assertFalse(puzzle.prompt().contains("7394"));
    }

    @Test
    void correctCodeIsCorrect() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("2004-7394-30"));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("  2004-7394-30  "));
    }

    @Test
    void thirdComponentIsComparedAsANumber() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("2004-7394-030"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("2004-7394-300"));
    }

    @Test
    void wrongSingleComponentIsIncorrect() {
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("2004-7394-31"));  // sequence value
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("2004-1111-30"));  // library code
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("1999-7394-30"));  // year
    }

    @Test
    void allThreeWrongIsIncorrect() {
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("1999-1111-20"));
    }

    @Test
    void feedbackCountsTheCorrectComponents() {
        assertTrue(puzzle.submit("2004-7394-31").feedback().contains("2 of 3"));
        assertTrue(puzzle.submit("2004-1111-30").feedback().contains("2 of 3"));
        assertTrue(puzzle.submit("1999-7394-30").feedback().contains("2 of 3"));
        assertTrue(puzzle.submit("2004-1111-20").feedback().contains("1 of 3"));
        assertTrue(puzzle.submit("1999-7394-20").feedback().contains("1 of 3"));
        assertTrue(puzzle.submit("1999-1111-30").feedback().contains("1 of 3"));
        assertTrue(puzzle.submit("1999-1111-20").feedback().contains("0 of 3"));
    }

    @Test
    void feedbackDoesNotRevealWhichComponentsAreCorrect() {
        // The three inputs below are right in different places but must get the very same feedback.
        String wrongYear = puzzle.submit("1999-7394-30").feedback();
        String wrongCode = puzzle.submit("2004-1111-30").feedback();
        String wrongNumber = puzzle.submit("2004-7394-31").feedback();
        assertEquals(wrongYear, wrongCode);
        assertEquals(wrongCode, wrongNumber);

        for (String feedback : new String[] {wrongYear, puzzle.submit("1999-1111-20").feedback()}) {
            assertFalse(feedback.contains("2004"));
            assertFalse(feedback.contains("7394"));
            assertFalse(feedback.contains("year"));
            assertFalse(feedback.contains("library"));
            assertFalse(feedback.contains("laboratory"));
        }
    }

    @Test
    void wrongSeparatorsAreInvalidFormat() {
        for (String answer : new String[] {"2004/7394/30", "2004 7394 30", "2004_7394_30", "2004.7394.30",
                "2004,7394,30", "2004--7394-30", "2004-7394-30-", "-2004-7394-30", "2004-7394-30-1"}) {
            assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(answer), "answer " + answer);
        }
    }

    @Test
    void missingComponentsAreInvalidFormat() {
        for (String answer : new String[] {"2004-7394", "2004", "2004-7394-", "2004--30", "-7394-30", "--"}) {
            assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(answer), "answer " + answer);
        }
    }

    @Test
    void wrongLengthsAreInvalidFormat() {
        for (String answer : new String[] {"200-7394-30", "20045-7394-30", "2004-739-30", "2004-73945-30",
                "2004-7394-3000", "2004-7394-12345", "2004-7394-1234567890123"}) {
            assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(answer), "answer " + answer);
        }
    }

    @Test
    void lettersAndSymbolsAreInvalidFormat() {
        for (String answer : new String[] {"abc", "abcd-efgh-i", "2004-7394-3o", "2004-7394-+30", "2004-7394-3.0",
                "2004-7394-\u0663\u0660"}) {
            assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(answer), "answer " + answer);
        }
    }

    @Test
    void emptyBlankAndNullAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(""));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("   "));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(null));
    }

    @Test
    void excessiveInputIsInvalidFormatWithoutAnException() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("9".repeat(100000)));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("2004-7394-" + "3".repeat(100000)));
    }

    @Test
    void invalidFormatFeedbackExplainsTheFormat() {
        assertTrue(puzzle.submit("abc").feedback().contains("YYYY-CCCC-N"));
    }

    @Test
    void hasExactlyThreeProgressiveHints() {
        assertEquals(3, puzzle.hints().size());
        for (String hint : puzzle.hints()) {
            assertFalse(hint.contains("2004"));
            assertFalse(hint.contains("7394"));
        }
        assertTrue(puzzle.hints().get(2).contains("YYYY-CCCC-N"));
    }
}
