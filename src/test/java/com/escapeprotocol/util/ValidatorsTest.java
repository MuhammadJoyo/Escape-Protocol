package com.escapeprotocol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class ValidatorsTest {

    private static final String TWENTY = "A".repeat(20);

    @Test
    void regexConstantMatchesSrs() {
        assertEquals("^[A-Za-z0-9 _-]{1,20}$", Validators.PLAYER_NAME_REGEX);
    }

    @Test
    void acceptsTypicalNames() {
        assertTrue(Validators.isValidPlayerName("Alice"));
        assertTrue(Validators.isValidPlayerName("Player_1"));
        assertTrue(Validators.isValidPlayerName("agent-47"));
        assertTrue(Validators.isValidPlayerName("Mary Jane"));
    }

    @Test
    void acceptsLengthBoundaries() {
        assertTrue(Validators.isValidPlayerName("A"));
        assertTrue(Validators.isValidPlayerName(TWENTY));
        assertFalse(Validators.isValidPlayerName(TWENTY + "A"));
    }

    @Test
    void trimsBeforeValidating() {
        assertTrue(Validators.isValidPlayerName("  Bob  "));
        assertTrue(Validators.isValidPlayerName("  " + TWENTY + "  "));
        assertEquals("Bob", Validators.requireValidPlayerName("  Bob  "));
    }

    @Test
    void rejectsNullEmptyAndBlank() {
        assertFalse(Validators.isValidPlayerName(null));
        assertFalse(Validators.isValidPlayerName(""));
        assertFalse(Validators.isValidPlayerName("     "));
    }

    @Test
    void rejectsIllegalCharacters() {
        assertFalse(Validators.isValidPlayerName("Bob!"));
        assertFalse(Validators.isValidPlayerName("<b>"));
        assertFalse(Validators.isValidPlayerName("Ünal"));
        assertFalse(Validators.isValidPlayerName("a.b"));
        assertFalse(Validators.isValidPlayerName("two\nlines"));
    }

    @Test
    void requireValidThrowsForInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Validators.requireValidPlayerName(null));
        assertThrows(IllegalArgumentException.class, () -> Validators.requireValidPlayerName(" "));
        assertThrows(IllegalArgumentException.class, () -> Validators.requireValidPlayerName("Bob!"));
        assertThrows(IllegalArgumentException.class,
                () -> Validators.requireValidPlayerName(TWENTY + "A"));
    }
    // ------------------------------------------------------------------ puzzle inputs (Module 05)

    private static final List<String> BOOKS = List.of("RED", "GREEN", "BLUE", "YELLOW");

    @Test
    void puzzleRegexConstantsMatchTheDesign() {
        assertEquals("^\\d{4}$", Validators.YEAR_REGEX);
        assertEquals("^\\d{4}-\\d{4}-\\d{1,3}$", Validators.FINAL_EXIT_CODE_REGEX);
        assertEquals("^-?\\d{1,9}$", Validators.SEQUENCE_ANSWER_REGEX);
    }

    @Test
    void yearAcceptsExactlyFourAsciiDigitsAfterTrimming() {
        assertEquals(Optional.of("2004"), Validators.parseYear("2004"));
        assertEquals(Optional.of("0000"), Validators.parseYear(" 0000 "));
        assertTrue(Validators.isValidYear("1998"));
        for (String bad : new String[] {null, "", "  ", "200", "20045", "abcd", "20 04", "+2004", "-2004", "2004.0",
                "\u0662\u0660\u0660\u0664"}) {
            assertEquals(Optional.empty(), Validators.parseYear(bad), "input " + bad);
            assertFalse(Validators.isValidYear(bad));
        }
    }

    @Test
    void libraryOrderReturnsTheNormalizedNamesInTheTypedOrder() {
        assertEquals(Optional.of(List.of("RED", "GREEN", "BLUE", "YELLOW")),
                Validators.parseLibraryOrder("red, Green ,BLUE,  yellow ", BOOKS));
        assertEquals(Optional.of(List.of("YELLOW", "BLUE", "GREEN", "RED")),
                Validators.parseLibraryOrder("YELLOW,BLUE,GREEN,RED", BOOKS));
        assertTrue(Validators.isValidLibraryOrder("RED,GREEN,BLUE,YELLOW", BOOKS));
    }

    @Test
    void libraryOrderRejectsMissingDuplicateUnknownAndExtraBooks() {
        for (String bad : new String[] {null, "", "  ", "RED", "RED,GREEN,BLUE", "RED,GREEN,BLUE,", "RED,,BLUE,YELLOW",
                "RED,RED,BLUE,YELLOW", "RED,GREEN,BLUE,PURPLE", "RED,GREEN,BLUE,YELLOW,RED", "RED GREEN BLUE YELLOW",
                "RED;GREEN;BLUE;YELLOW", "1,2,3,4"}) {
            assertEquals(Optional.empty(), Validators.parseLibraryOrder(bad, BOOKS), "input " + bad);
            assertFalse(Validators.isValidLibraryOrder(bad, BOOKS));
        }
    }

    @Test
    void libraryOrderUsesTheGivenBookListAndRejectsNullBooks() {
        assertEquals(Optional.of(List.of("A", "B", "C", "D")),
                Validators.parseLibraryOrder("a,b,c,d", List.of("A", "B", "C", "D")));
        assertEquals(Optional.empty(), Validators.parseLibraryOrder("a,b,c,d", List.of("A", "B", "C", "E")));
        assertThrows(NullPointerException.class, () -> Validators.parseLibraryOrder("a,b,c,d", null));
    }

    @Test
    void libraryOrderResultCannotBeModified() {
        List<String> order = Validators.parseLibraryOrder("RED,GREEN,BLUE,YELLOW", BOOKS).orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> order.add("X"));
    }

    @Test
    void sequenceAnswerAcceptsIntegersOfAtMostNineDigits() {
        assertEquals(OptionalInt.of(30), Validators.parseSequenceAnswer("30"));
        assertEquals(OptionalInt.of(30), Validators.parseSequenceAnswer(" 030 "));
        assertEquals(OptionalInt.of(-5), Validators.parseSequenceAnswer("-5"));
        assertEquals(OptionalInt.of(999999999), Validators.parseSequenceAnswer("999999999"));
        assertEquals(OptionalInt.of(-999999999), Validators.parseSequenceAnswer("-999999999"));
        assertTrue(Validators.isValidSequenceAnswer("0"));
    }

    @Test
    void sequenceAnswerRejectsNonIntegersAndTooLongNumbers() {
        for (String bad : new String[] {null, "", "  ", "-", "+30", "3.0", "1e2", "0x1E", "30x", "3 0", "--3",
                "1234567890", "2147483648", "-2147483649", "9".repeat(100000)}) {
            assertEquals(OptionalInt.empty(), Validators.parseSequenceAnswer(bad), "input " + bad);
            assertFalse(Validators.isValidSequenceAnswer(bad));
        }
    }

    @Test
    void finalExitCodeReturnsTheThreeParts() {
        assertEquals(Optional.of(List.of("2004", "7394", "30")), Validators.parseFinalExitCode("2004-7394-30"));
        assertEquals(Optional.of(List.of("0001", "0000", "007")), Validators.parseFinalExitCode(" 0001-0000-007 "));
        assertEquals(Optional.of(List.of("1111", "2222", "3")), Validators.parseFinalExitCode("1111-2222-3"));
        assertTrue(Validators.isValidFinalExitCode("2004-7394-30"));
    }

    @Test
    void finalExitCodeRejectsWrongShapes() {
        for (String bad : new String[] {null, "", "  ", "abc", "2004-7394", "2004-7394-", "2004--30", "200-7394-30",
                "20045-7394-30", "2004-739-30", "2004-73945-30", "2004-7394-3000", "2004/7394/30", "2004 7394 30",
                "2004-7394-30-1", "2004-7394-3.0", "2004-7394-+30", "9".repeat(100000)}) {
            assertEquals(Optional.empty(), Validators.parseFinalExitCode(bad), "input " + bad);
            assertFalse(Validators.isValidFinalExitCode(bad));
        }
    }

    @Test
    void finalExitPartsCannotBeModified() {
        List<String> parts = Validators.parseFinalExitCode("2004-7394-30").orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> parts.set(0, "x"));
    }

    @Test
    void playerNameRuleIsUnchangedByThePuzzleValidators() {
        assertTrue(Validators.isValidPlayerName("Alice_1"));
        assertFalse(Validators.isValidPlayerName("2004-7394-30!"));
        assertEquals("Bob", Validators.requireValidPlayerName("  Bob "));
    }
}
