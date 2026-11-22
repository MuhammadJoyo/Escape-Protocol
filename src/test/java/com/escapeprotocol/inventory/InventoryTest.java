package com.escapeprotocol.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryTest {

    private static Item item(String id) {
        return new Item(id, "Name of " + id, "Description of " + id, ItemKind.CLUE);
    }

    @Test
    void newInventoryIsEmpty() {
        Inventory inventory = new Inventory();
        assertTrue(inventory.items().isEmpty());
        assertFalse(inventory.contains("anything"));
    }

    @Test
    void addNewItemReturnsTrueAndItemIsContained() {
        Inventory inventory = new Inventory();
        assertTrue(inventory.add(item("key-1")));
        assertTrue(inventory.contains("key-1"));
        assertFalse(inventory.contains("key-2"));
    }

    @Test
    void addingSameIdAgainReturnsFalseAndKeepsTheFirstItem() {
        Inventory inventory = new Inventory();
        Item first = new Item("code-1", "First", "first description", ItemKind.CODE);
        Item sameId = new Item("code-1", "Second", "second description", ItemKind.KEY);

        assertTrue(inventory.add(first));
        assertFalse(inventory.add(sameId));

        assertEquals(1, inventory.items().size());
        assertEquals("First", inventory.items().get(0).name());
    }

    @Test
    void itemsKeepPickupOrder() {
        Inventory inventory = new Inventory();
        inventory.add(item("c"));
        inventory.add(item("a"));
        inventory.add(item("b"));
        inventory.add(item("a")); // duplicate must not change the order

        List<String> ids = inventory.items().stream().map(Item::id).toList();
        assertEquals(List.of("c", "a", "b"), ids);
    }

    @Test
    void containsHandlesNullAndUnknownIds() {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        assertFalse(inventory.contains(null));
        assertFalse(inventory.contains(""));
        assertFalse(inventory.contains("A"));
    }

    @Test
    void addNullIsRejectedAndInventoryIsUnchanged() {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        assertThrows(NullPointerException.class, () -> inventory.add(null));
        assertEquals(1, inventory.items().size());
    }

    @Test
    void returnedListCannotBeUsedToChangeTheInventory() {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        List<Item> view = inventory.items();

        assertThrows(UnsupportedOperationException.class, () -> view.add(item("b")));
        assertThrows(UnsupportedOperationException.class, view::clear);
        assertThrows(UnsupportedOperationException.class, () -> view.remove(0));

        assertTrue(inventory.contains("a"));
        assertFalse(inventory.contains("b"));
    }

    @Test
    void returnedListIsASnapshot() {
        Inventory inventory = new Inventory();
        inventory.add(item("a"));
        List<Item> before = inventory.items();
        inventory.add(item("b"));
        assertEquals(1, before.size());
        assertEquals(2, inventory.items().size());
    }
}
