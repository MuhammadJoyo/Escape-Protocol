package com.escapeprotocol.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Simple headless tests for Main. They only read constants, so no window
 * is created and they work on machines without a display.
 */
class MainTest {

    @Test
    void titleHasExpectedText() {
        assertEquals("Escape Protocol — The Last Exit", Main.TITLE);
    }

    @Test
    void titleMentionsGameNameAndSubtitle() {
        assertTrue(Main.TITLE.startsWith("Escape Protocol"));
        assertTrue(Main.TITLE.endsWith("The Last Exit"));
    }

    @Test
    void welcomeAndStatusMessagesAreNotBlank() {
        assertFalse(Main.WELCOME_MESSAGE.isBlank());
        assertFalse(Main.STATUS_MESSAGE.isBlank());
        assertFalse(Main.SUBTITLE.isBlank());
    }
}