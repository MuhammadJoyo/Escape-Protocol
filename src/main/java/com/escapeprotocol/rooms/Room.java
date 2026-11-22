package com.escapeprotocol.rooms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable room definition: which room it is and which puzzles must be solved to leave it.
 * It holds no gameplay behaviour; the game engine (a later module) uses it as data.
 *
 * <p><b>Abstraction function:</b> AF(id, orderIndex, requiredPuzzleIds) = the room {@code id},
 * which is stage {@code orderIndex} of the game and is finished when every puzzle id in
 * {@code requiredPuzzleIds} has been solved.
 *
 * <p><b>Representation invariant:</b>
 * <ul>
 *   <li>{@code id} is non-null;</li>
 *   <li>{@code orderIndex == id.orderIndex()};</li>
 *   <li>{@code requiredPuzzleIds} is an unmodifiable list that has no null, blank or duplicate
 *       elements (ids are stored trimmed). It may be empty.</li>
 * </ul>
 */
public final class Room {

    private final RoomId id;
    private final int orderIndex;
    private final List<String> requiredPuzzleIds;

    /**
     * Creates a room definition.
     *
     * @param id the room; must not be {@code null}
     * @param orderIndex the stage number; must equal {@code id.orderIndex()}
     * @param requiredPuzzleIds ids of puzzles that must be solved; must not be {@code null}
     *        and must not contain null, blank or duplicate ids. The list is copied.
     * @throws NullPointerException if {@code id} or {@code requiredPuzzleIds} is {@code null}
     * @throws IllegalArgumentException if the order index does not match the room, or a puzzle id
     *         is null, blank or duplicated
     */
    public Room(RoomId id, int orderIndex, List<String> requiredPuzzleIds) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(requiredPuzzleIds, "requiredPuzzleIds must not be null");
        if (orderIndex != id.orderIndex()) {
            throw new IllegalArgumentException(
                    "orderIndex " + orderIndex + " does not match " + id + " (expected " + id.orderIndex() + ")");
        }
        List<String> copy = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String puzzleId : requiredPuzzleIds) {
            if (puzzleId == null || puzzleId.isBlank()) {
                throw new IllegalArgumentException("Puzzle ids must not be null or blank.");
            }
            String trimmed = puzzleId.trim();
            if (!seen.add(trimmed)) {
                throw new IllegalArgumentException("Duplicate puzzle id: " + trimmed);
            }
            copy.add(trimmed);
        }
        this.id = id;
        this.orderIndex = orderIndex;
        this.requiredPuzzleIds = List.copyOf(copy);
    }

    /** @return which room this is */
    public RoomId id() {
        return id;
    }

    /** @return the stage number of this room */
    public int orderIndex() {
        return orderIndex;
    }

    /** @return an unmodifiable list of the puzzle ids required to finish this room */
    public List<String> requiredPuzzleIds() {
        return requiredPuzzleIds;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Room)) {
            return false;
        }
        Room that = (Room) other;
        return id == that.id
                && orderIndex == that.orderIndex
                && requiredPuzzleIds.equals(that.requiredPuzzleIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderIndex, requiredPuzzleIds);
    }

    @Override
    public String toString() {
        return "Room[id=" + id + ", orderIndex=" + orderIndex + ", requiredPuzzleIds=" + requiredPuzzleIds + "]";
    }
}
