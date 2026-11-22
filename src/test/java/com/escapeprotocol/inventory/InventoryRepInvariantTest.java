package com.escapeprotocol.inventory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the representation invariant (RI) of {@link Inventory}.
 *
 * <p>The private {@code checkRep()} method and the private set are reached with reflection, only
 * so that the tests can deliberately break the RI and prove that {@code checkRep()} notices it.
 */
class InventoryRepInvariantTest {

    private static Item item(String id) {
        return new Item(id, "Name " + id, "Description " + id, ItemKind.KEY);
    }

    private static void callCheckRep(Inventory inventory) throws Exception {
        Method method = Inventory.class.getDeclaredMethod("checkRep");
        method.setAccessible(true);
        method.invoke(inventory);
    }

    @SuppressWarnings("unchecked")
    private static Set<Item> internalSet(Inventory inventory) throws Exception {
        Field field = Inventory.class.getDeclaredField("items");
        field.setAccessible(true);
        return (Set<Item>) field.get(inventory);
    }

    @Test
    void invariantHoldsForEmptyInventory() {
        assertDoesNotThrow(() -> callCheckRep(new Inventory()));
    }

    @Test
    void invariantHoldsAfterAddsAndRejectedDuplicates() {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        inventory.add(item("b"));
        inventory.add(item("a"));
        assertDoesNotThrow(() -> callCheckRep(inventory));

        // No two stored items share an id.
        Set<String> ids = new HashSet<>();
        for (Item stored : inventory.items()) {
            assertTrue(ids.add(stored.id()));
        }
        assertEquals(2, ids.size());
    }

    @Test
    void checkRepDetectsANullElementInTheInternalSet() throws Exception {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        internalSet(inventory).add(null); // deliberately corrupt the representation

        InvocationTargetException error =
                assertThrows(InvocationTargetException.class, () -> callCheckRep(inventory));
        assertInstanceOf(IllegalStateException.class, error.getCause());
    }

    @Test
    void mutatingOperationsRunCheckRepSoCorruptionIsDetectedOnNextAdd() throws Exception {
        Inventory inventory = new Inventory();
        internalSet(inventory).add(null);

        assertThrows(IllegalStateException.class, () -> inventory.add(item("b")));
    }

    @Test
    void returnedItemsListIsNotTheInternalSet() throws Exception {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        List<Item> view = inventory.items();
        assertTrue(view != internalSet(inventory));
        assertThrows(UnsupportedOperationException.class, () -> view.add(item("z")));
        assertEquals(1, internalSet(inventory).size());
    }
}
