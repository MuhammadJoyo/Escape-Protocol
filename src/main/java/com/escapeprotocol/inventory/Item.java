package com.escapeprotocol.inventory;

import java.util.Objects;

/**
 * An immutable item (key, code or clue) the player can collect.
 *
 * <p><b>Abstraction function:</b> AF(id, name, description, kind) = one collectable thing,
 * identified by {@code id}, shown to the player as {@code name} with the text {@code description}.
 *
 * <p><b>Representation invariant:</b> all four fields are non-null; {@code id}, {@code name} and
 * {@code description} are trimmed and not empty.
 *
 * <p><b>Equality:</b> two items are equal when their {@code id} values are equal. Name,
 * description and kind are ignored, so an inventory can never hold two items with the same id.
 *
 * <p>The class is final, has only final fields and no setters. Strings and enums are immutable,
 * so no defensive copies are needed.
 */
public final class Item {

    private final String id;
    private final String name;
    private final String description;
    private final ItemKind kind;

    /**
     * Creates an item.
     *
     * @param id unique id, e.g. {@code "key-drawer"}; must not be null or blank
     * @param name display name; must not be null or blank
     * @param description text shown to the player; must not be null or blank
     * @param kind the kind of item; must not be null
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code id}, {@code name} or {@code description} is blank
     */
    public Item(String id, String name, String description, ItemKind kind) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.description = requireText(description, "description");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return trimmed;
    }

    /** @return the unique item id */
    public String id() {
        return id;
    }

    /** @return the display name */
    public String name() {
        return name;
    }

    /** @return the description text */
    public String description() {
        return description;
    }

    /** @return the kind of item */
    public ItemKind kind() {
        return kind;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Item)) {
            return false;
        }
        return id.equals(((Item) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Item[id=" + id + ", name=" + name + ", kind=" + kind + "]";
    }
}
