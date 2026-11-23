package com.escapeprotocol.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.rooms.RoomId;
import org.junit.jupiter.api.Test;

class OfficePuzzleTest {

    private final OfficePuzzle puzzle = new OfficePuzzle();

    private PuzzleResult.Status statusOf(String answer) {
        return puzzle.submit(answer).status();
    }

    @Test
    void hasTheDocumentedIdAndRoom() {
        assertEquals("office_year", puzzle.id());
        assertEquals(RoomId.OFFICE, puzzle.roomId());
        assertTrue(puzzle.requiredItemIds().isEmpty());
    }

    @Test
    void promptMentionsAllThreeDocuments() {
        String prompt = puzzle.prompt();
        assertTrue(prompt.contains("1998"));
        assertTrue(prompt.contains("2004"));
        assertTrue(prompt.contains("2012"));
    }

    @Test
    void correctYearIsCorrect() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("2004"));
    }

    @Test
    void otherFourDigitYearsAreIncorrect() {
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("1998"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("2012"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("1234"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("0000"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("2005"));
    }

    @Test
    void tooShortInputIsInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("20"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("200"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("2"));
    }

    @Test
    void tooLongInputIsInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("20045"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("2004".repeat(5000)));
    }

    @Test
    void lettersAndSymbolsAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("abcd"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("20O4"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("+2004"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("-2004"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("2004.0"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("20 04"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("\u0662\u0660\u0660\u0664")); // Arabic-Indic digits
    }

    @Test
    void emptyBlankAndNullAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(""));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("    "));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(null));
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("  2004  "));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("\t2004\n"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf(" 1998 "));
    }

    @Test
    void feedbackIsNeverBlankAndDoesNotRevealTheAnswerOnFailure() {
        for (String answer : new String[] {"1998", "abcd", "", null}) {
            PuzzleResult result = puzzle.submit(answer);
            assertFalse(result.feedback().isBlank());
            assertFalse(result.feedback().contains("2004"));
        }
    }

    @Test
    void hasExactlyThreeProgressiveHints() {
        assertEquals(3, puzzle.hints().size());
        assertFalse(puzzle.hints().get(0).contains("2004"));
        assertFalse(puzzle.hints().get(1).contains("2004"));
        assertTrue(puzzle.hints().get(2).contains("2004"));
    }
}
