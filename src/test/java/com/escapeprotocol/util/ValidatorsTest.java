package com.escapeprotocol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
