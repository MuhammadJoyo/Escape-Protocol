package com.escapeprotocol.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void storesTrimmedName() {
        assertEquals("Alice", new Player("  Alice ").name());
    }

    @Test
    void acceptsBoundaryLengths() {
        assertEquals("A", new Player("A").name());
        String twenty = "B".repeat(20);
        assertEquals(twenty, new Player(twenty).name());
    }

    @Test
    void rejectsInvalidNames() {
        assertThrows(IllegalArgumentException.class, () -> new Player(null));
        assertThrows(IllegalArgumentException.class, () -> new Player(""));
        assertThrows(IllegalArgumentException.class, () -> new Player("   "));
        assertThrows(IllegalArgumentException.class, () -> new Player("C".repeat(21)));
        assertThrows(IllegalArgumentException.class, () -> new Player("bad#name"));
    }

    @Test
    void equalNamesGiveEqualPlayersWithSameHashCode() {
        Player a = new Player("Bob");
        Player b = new Player("  Bob  ");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentNamesAreNotEqual() {
        assertNotEquals(new Player("Bob"), new Player("Bobby"));
        assertNotEquals(new Player("Bob"), new Player("bob"));
        assertNotEquals(new Player("Bob"), null);
        assertNotEquals(new Player("Bob"), "Bob");
    }

    @Test
    void duplicatePlayersCollapseInHashSet() {
        Set<Player> players = new HashSet<>();
        players.add(new Player("Bob"));
        players.add(new Player(" Bob "));
        assertEquals(1, players.size());
        assertTrue(players.contains(new Player("Bob")));
    }

    @Test
    void toStringContainsName() {
        assertTrue(new Player("Alice").toString().contains("Alice"));
    }
}
