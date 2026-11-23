package com.escapeprotocol.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PuzzleResultTest {

    @Test
    void statusesAreExactlyTheThreeFromFrPz01() {
        assertEquals(List.of("CORRECT", "INCORRECT", "INVALID_FORMAT"),
                Arrays.stream(PuzzleResult.Status.values()).map(Enum::name).toList());
    }

    @Test
    void keepsItsValuesAndHasValueEquality() {
        PuzzleResult result = new PuzzleResult(PuzzleResult.Status.CORRECT, "Right");
        assertEquals(PuzzleResult.Status.CORRECT, result.status());
        assertEquals("Right", result.feedback());
        assertEquals(result, new PuzzleResult(PuzzleResult.Status.CORRECT, "Right"));
        assertNotEquals(result, new PuzzleResult(PuzzleResult.Status.INCORRECT, "Right"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new PuzzleResult(null, "x"));
        assertThrows(NullPointerException.class,
                () -> new PuzzleResult(PuzzleResult.Status.CORRECT, null));
    }
}
