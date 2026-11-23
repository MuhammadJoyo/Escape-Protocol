package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ResultTypesTest {

    @Test
    void submissionStatusesAreExactlyTheDesignList() {
        assertEquals(
                java.util.List.of("REFUSED_NOT_RUNNING", "REFUSED_LOCKED", "REFUSED_MISSING_CLUES",
                        "ALREADY_SOLVED", "INVALID_FORMAT", "INCORRECT", "CORRECT", "VICTORY"),
                java.util.Arrays.stream(SubmissionResult.Status.values()).map(Enum::name).toList());
    }

    @Test
    void hintStatusesAreExactlyTheDesignList() {
        assertEquals(
                java.util.List.of("REVEALED_NEW", "VIEWED_AGAIN", "NO_MORE_HINTS", "REFUSED"),
                java.util.Arrays.stream(HintResult.Status.values()).map(Enum::name).toList());
    }

    @Test
    void submissionResultKeepsItsValuesAndHasValueEquality() {
        SubmissionResult a = new SubmissionResult(SubmissionResult.Status.CORRECT, "Well done");
        assertEquals(SubmissionResult.Status.CORRECT, a.status());
        assertEquals("Well done", a.message());
        assertEquals(a, new SubmissionResult(SubmissionResult.Status.CORRECT, "Well done"));
        assertNotEquals(a, new SubmissionResult(SubmissionResult.Status.INCORRECT, "Well done"));
    }

    @Test
    void submissionResultRejectsNulls() {
        assertThrows(NullPointerException.class, () -> new SubmissionResult(null, "m"));
        assertThrows(NullPointerException.class,
                () -> new SubmissionResult(SubmissionResult.Status.CORRECT, null));
    }

    @Test
    void hintResultKeepsItsValuesAndRejectsInvalidOnes() {
        HintResult h = new HintResult(HintResult.Status.REVEALED_NEW, "text", 2);
        assertEquals(HintResult.Status.REVEALED_NEW, h.status());
        assertEquals("text", h.text());
        assertEquals(2, h.level());
        assertThrows(NullPointerException.class, () -> new HintResult(null, "t", 0));
        assertThrows(NullPointerException.class, () -> new HintResult(HintResult.Status.REFUSED, null, 0));
        assertThrows(IllegalArgumentException.class, () -> new HintResult(HintResult.Status.REFUSED, "t", -1));
    }
}
