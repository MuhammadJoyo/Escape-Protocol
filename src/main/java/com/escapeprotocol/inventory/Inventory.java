package com.escapeprotocol.inventory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract data type: the collection of items a player has picked up, in pickup order.
 *
 * <p><b>Abstraction function:</b> AF(items) = the sequence of collected items, oldest pickup
 * first, which is exactly the iteration order of the {@link LinkedHashSet} {@code items}.
 *
 * <p><b>Representation invariant (RI):</b>
 * <ul>
 *   <li>{@code items} is not null;</li>
 *   <li>{@code items} contains no null element;</li>
 *   <li>no two elements of {@code items} have the same item id.</li>
 * </ul>
 * {@link #checkRep()} verifies the RI after construction and after every mutating operation.
 *
 * <p><b>Representation exposure:</b> the internal set is never returned; {@link #items()} returns
 * an unmodifiable snapshot copy.
 *
 * <p>This class is not thread-safe. Synchronisation belongs to the game session (a later module).
 */
public final class Inventory {

    private final LinkedHashSet<Item> items = new LinkedHashSet<>();

    /** Creates an empty inventory. */
    public Inventory() {
        checkRep();
    }

    /**
     * Adds an item unless an item with the same id is already present.
     *
     * @param item the item to add; must not be {@code null}
     * @return {@code true} if the item was added; {@code false} if an item with the same id
     *         already exists (the existing item is kept unchanged)
     * @throws NullPointerException if {@code item} is {@code null}
     */
    public boolean add(Item item) {
        Objects.requireNonNull(item, "item must not be null");
        boolean added = items.add(item);
        checkRep();
        return added;
    }

    /**
     * Tells whether an item with the given id has been collected.
     *
     * @param itemId the id to look for; may be {@code null}
     * @return {@code true} if present; {@code false} if absent or if {@code itemId} is {@code null}
     */
    public boolean contains(String itemId) {
        if (itemId == null) {
            return false;
        }
        for (Item item : items) {
            if (item.id().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the collected items in pickup order.
     *
     * @return an unmodifiable snapshot; later changes to the inventory do not affect it
     */
    public List<Item> items() {
        return List.copyOf(items);
    }

    /** Throws {@link IllegalStateException} if the representation invariant is broken. */
    private void checkRep() {
        if (items == null) {
            throw new IllegalStateException("RI violated: items is null");
        }
        Set<String> seenIds = new HashSet<>();
        for (Item item : items) {
            if (item == null) {
                throw new IllegalStateException("RI violated: items contains null");
            }
            if (!seenIds.add(item.id())) {
                throw new IllegalStateException("RI violated: duplicate item id " + item.id());
            }
        }
    }
}
