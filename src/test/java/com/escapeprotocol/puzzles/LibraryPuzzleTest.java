package com.escapeprotocol.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.rooms.RoomId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LibraryPuzzleTest {

    private static final String CORRECT = "RED,GREEN,BLUE,YELLOW";

    private final LibraryPuzzle puzzle = new LibraryPuzzle();

    private PuzzleResult.Status statusOf(String answer) {
        return puzzle.submit(answer).status();
    }

    private static List<List<String>> permutations(List<String> items) {
        if (items.size() <= 1) {
            return List.of(items);
        }
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            List<String> rest = new ArrayList<>(items);
            String first = rest.remove(i);
            for (List<String> tail : permutations(rest)) {
                List<String> permutation = new ArrayList<>();
                permutation.add(first);
                permutation.addAll(tail);
                result.add(permutation);
            }
        }
        return result;
    }

    @Test
    void hasTheDocumentedIdAndRoom() {
        assertEquals("library_order", puzzle.id());
        assertEquals(RoomId.LIBRARY, puzzle.roomId());
        assertTrue(puzzle.requiredItemIds().isEmpty());
    }

    @Test
    void promptShowsAllFourBooksWithTheirYears() {
        String prompt = puzzle.prompt();
        for (String text : new String[] {"1985", "1992", "2001", "2010", "Red", "Green", "Blue", "Yellow"}) {
            assertTrue(prompt.contains(text), "prompt should contain " + text);
        }
    }

    @Test
    void correctOrderIsCorrect() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf(CORRECT));
    }

    @Test
    void everyOtherPermutationIsIncorrect() {
        List<List<String>> all = permutations(List.of("RED", "GREEN", "BLUE", "YELLOW"));
        assertEquals(24, all.size());
        int correct = 0;
        for (List<String> order : all) {
            PuzzleResult.Status status = statusOf(String.join(",", order));
            if (order.equals(LibraryPuzzle.CORRECT_ORDER)) {
                assertEquals(PuzzleResult.Status.CORRECT, status);
                correct++;
            } else {
                assertEquals(PuzzleResult.Status.INCORRECT, status, "order " + order);
            }
        }
        assertEquals(1, correct);
    }

    @Test
    void caseIsIgnored() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("red,green,blue,yellow"));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("Red,Green,Blue,Yellow"));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("rEd,GREEN,bLuE,yElLoW"));
        assertEquals(PuzzleResult.Status.INCORRECT, statusOf("yellow,blue,green,red"));
    }

    @Test
    void whitespaceAroundBooksIsIgnored() {
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("  RED , GREEN ,BLUE,  YELLOW  "));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("RED, GREEN, BLUE, YELLOW"));
        assertEquals(PuzzleResult.Status.CORRECT, statusOf("\tRED,GREEN,BLUE,YELLOW\n"));
    }

    @Test
    void missingBooksAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,,BLUE,YELLOW"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(",RED,GREEN,BLUE"));
    }

    @Test
    void duplicateBooksAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,RED,BLUE,YELLOW"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,RED"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("red,RED,Red,rEd"));
    }

    @Test
    void unknownBooksAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,PURPLE"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("ONE,TWO,THREE,FOUR"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,YELLOWS"));
    }

    @Test
    void extraTokensAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,YELLOW,RED"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,YELLOW,PURPLE"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,YELLOW,"));
    }

    @Test
    void wrongSeparatorsAndSymbolsAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED GREEN BLUE YELLOW"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED;GREEN;BLUE;YELLOW"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED-GREEN-BLUE-YELLOW"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("1985,1992,2001,2010"));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,GREEN,BLUE,YELLOW!"));
    }

    @Test
    void emptyBlankAndNullAreInvalidFormat() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(""));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("   "));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf(null));
    }

    @Test
    void veryLongInputIsInvalidFormatAndDoesNotFail() {
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("RED,".repeat(20000)));
        assertEquals(PuzzleResult.Status.INVALID_FORMAT, statusOf("A".repeat(100000)));
    }

    @Test
    void hasExactlyThreeProgressiveHints() {
        assertEquals(3, puzzle.hints().size());
        assertFalse(puzzle.hints().get(0).toUpperCase().contains("RED, GREEN"));
        assertTrue(puzzle.hints().get(2).contains("Red, Green, Blue, Yellow"));
    }
}
