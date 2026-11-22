package com.escapeprotocol.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ItemEqualityTest {

    private static Item item(String id, String name, ItemKind kind) {
        return new Item(id, name, "description of " + name, kind);
    }

    @Test
    void sameIdMeansEqualEvenWhenOtherFieldsDiffer() {
        Item a = item("key-1", "Brass key", ItemKind.KEY);
        Item b = item("key-1", "Another name", ItemKind.CLUE);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentIdMeansNotEqualEvenWhenOtherFieldsMatch() {
        assertNotEquals(item("key-1", "Brass key", ItemKind.KEY), item("key-2", "Brass key", ItemKind.KEY));
    }

    @Test
    void equalsContractHolds() {
        Item a = item("x", "A", ItemKind.KEY);
        Item b = item("x", "B", ItemKind.CODE);
        Item c = item("x", "C", ItemKind.CLUE);
        assertEquals(a, a);                       // reflexive
        assertEquals(a, b);
        assertEquals(b, a);                       // symmetric
        assertEquals(b, c);
        assertEquals(a, c);                       // transitive
        assertNotEquals(a, null);
        assertNotEquals(a, "x");
    }

    @Test
    void idIsTrimmedSoPaddedIdsAreEqual() {
        assertEquals(item("key-1", "A", ItemKind.KEY), item("  key-1 ", "A", ItemKind.KEY));
    }

    @Test
    void hashSetKeepsOnlyTheFirstItemWithAGivenId() {
        Item first = item("code-1", "First", ItemKind.CODE);
        Item duplicate = item("code-1", "Second", ItemKind.CODE);
        Item other = item("code-2", "Other", ItemKind.CODE);

        Set<Item> set = new HashSet<>();
        assertTrue(set.add(first));
        assertTrue(!set.add(duplicate));
        assertTrue(set.add(other));

        assertEquals(2, set.size());
        assertTrue(set.contains(item("code-1", "Anything", ItemKind.KEY)));
        for (Item stored : set) {
            if (stored.id().equals("code-1")) {
                assertSame(first, stored);
            }
        }
    }

    @Test
    void toStringShowsIdNameAndKind() {
        String text = item("key-1", "Brass key", ItemKind.KEY).toString();
        assertTrue(text.contains("key-1"));
        assertTrue(text.contains("Brass key"));
        assertTrue(text.contains("KEY"));
    }
}
