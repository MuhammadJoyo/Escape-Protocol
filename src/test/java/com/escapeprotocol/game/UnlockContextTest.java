package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UnlockContextTest {

    @Test
    void answersQuestionsAboutItemsAndSolvedPuzzles() {
        UnlockContext context = new UnlockContext(Set.of("key-a"), Set.of("puzzle-1"));
        assertTrue(context.hasItem("key-a"));
        assertFalse(context.hasItem("key-b"));
        assertTrue(context.isSolved("puzzle-1"));
        assertFalse(context.isSolved("puzzle-2"));
        assertFalse(context.hasItem(null));
        assertFalse(context.isSolved(null));
    }

    @Test
    void copiesTheSetsSoLaterChangesDoNotLeakIn() {
        Set<String> items = new HashSet<>(Set.of("a"));
        Set<String> solved = new HashSet<>(Set.of("p"));
        UnlockContext context = new UnlockContext(items, solved);

        items.add("b");
        solved.add("q");

        assertEquals(Set.of("a"), context.itemIds());
        assertEquals(Set.of("p"), context.solvedPuzzleIds());
    }

    @Test
    void exposedSetsCannotBeModified() {
        UnlockContext context = new UnlockContext(Set.of("a"), Set.of("p"));
        assertThrows(UnsupportedOperationException.class, () -> context.itemIds().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> context.solvedPuzzleIds().clear());
    }

    @Test
    void rejectsNullSets() {
        assertThrows(NullPointerException.class, () -> new UnlockContext(null, Set.of()));
        assertThrows(NullPointerException.class, () -> new UnlockContext(Set.of(), null));
    }

    @Test
    void equalContextsAreEqual() {
        assertEquals(new UnlockContext(Set.of("a"), Set.of("p")),
                new UnlockContext(Set.of("a"), Set.of("p")));
    }
}
