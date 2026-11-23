package com.escapeprotocol.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.rooms.RoomId;
import org.junit.jupiter.api.Test;

class SequencePuzzleTest {

    private final SequencePuzzle puzzle = new SequencePuzzle();

    private PuzzleResult.Status statusOf(String answer) {
        return puzzle.submit(answer).status();
    }

    @Test
    void hasTheDocumentedIdAndRoom() {
        assertEquals("lab_sequence", puzzle.id());
        assertEquals(RoomId.LABORATORY, puzzle.roomId());
        assertTrue(puzzle.requiredItemIds().isEmpty());
    }

    @Test
    void promptShowsTheSequence() {
        assertTrue(puzzle.prompt().contains("2, 6, 12, 20, ?"));
    }

    @Test
    void thirtyIsCorrect() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("30"));
    }

    @Test
    void surroundingWhitespaceAndLeadingZerosAreAccepted() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("  30  "));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("030"));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("0000030"));
    }

    @Test
    void otherIntegersAreIncorrect() {
        for (String answer : new String[] {"29", "31", "20", "42", "0", "1", "300", "3", "-30", "-1", "-0"}) {
            assertEquals(PuzzleResult.Status.INCORRECT, statusOf(answer), "answer " + answer);
        }
    }

    @Test
    void negativeIntegersAreValidButIncorrect() {
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("-30"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("-999999999"));
    }

    @Test
    void largestAcceptedNumberIsStillJustIncorrect() {
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("999999999"));
    }

    @Test
    void nonIntegersAreInvalidFormat() {
        for (String answer : new String[] {"thirty", "30x", "x30", "3 0", "3.0", "30.5", "1e2", "0x1E",
                "+30", "--30", "-", "\u221230", "\u0663\u0660", "30,0", "30!"}) {
            assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(answer), "answer " + answer);
        }
    }

    @Test
    void numbersThatWouldOverflowAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("1234567890"));          // 10 digits
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("2147483648"));          // Integer.MAX_VALUE + 1
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("-2147483649"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("99999999999999999999"));
    }

    @Test
    void excessivelyLongInputIsInvalidFormatWithoutAnException() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("9".repeat(100000)));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("0".repeat(10) + "30"));
    }

    @Test
    void emptyBlankAndNullAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(""));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("   "));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(null));
    }

    @Test
    void hasExactlyThreeProgressiveHints() {
        assertEquals(3, puzzle.hints().size());
        assertFalse(puzzle.hints().get(0).contains("30"));
        assertFalse(puzzle.hints().get(1).contains("30"));
        assertTrue(puzzle.hints().get(2).contains("10"));
    }
}
