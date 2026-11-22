package com.escapeprotocol.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ItemImmutabilityTest {

    @Test
    void classIsFinalSoNoSubclassCanAddMutableState() {
        assertTrue(Modifier.isFinal(Item.class.getModifiers()));
    }

    @Test
    void everyFieldIsPrivateAndFinal() {
        for (Field field : Item.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName() + " must be private");
            assertTrue(Modifier.isFinal(field.getModifiers()), field.getName() + " must be final");
        }
    }

    @Test
    void thereAreNoSetterMethods() {
        for (Method method : Item.class.getDeclaredMethods()) {
            assertFalse(method.getName().startsWith("set"), method.getName() + " looks like a setter");
        }
    }

    @Test
    void gettersReturnTheStoredTrimmedValues() {
        Item item = new Item("  key-1 ", " Brass key ", " Opens the drawer ", ItemKind.KEY);
        assertEquals("key-1", item.id());
        assertEquals("Brass key", item.name());
        assertEquals("Opens the drawer", item.description());
        assertEquals(ItemKind.KEY, item.kind());
    }

    @Test
    void changingTheCallersBuilderAfterwardsDoesNotAffectTheItem() {
        StringBuilder text = new StringBuilder("original");
        Item item = new Item("id-1", text.toString(), "desc", ItemKind.CLUE);
        text.append(" changed");
        assertEquals("original", item.name());
    }

    @Test
    void constructorRejectsNullAndBlankValues() {
        assertThrows(NullPointerException.class, () -> new Item(null, "n", "d", ItemKind.KEY));
        assertThrows(NullPointerException.class, () -> new Item("i", null, "d", ItemKind.KEY));
        assertThrows(NullPointerException.class, () -> new Item("i", "n", null, ItemKind.KEY));
        assertThrows(NullPointerException.class, () -> new Item("i", "n", "d", null));
        assertThrows(IllegalArgumentException.class, () -> new Item("  ", "n", "d", ItemKind.KEY));
        assertThrows(IllegalArgumentException.class, () -> new Item("i", "", "d", ItemKind.KEY));
        assertThrows(IllegalArgumentException.class, () -> new Item("i", "n", " ", ItemKind.KEY));
    }
}
