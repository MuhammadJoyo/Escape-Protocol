package com.escapeprotocol.rooms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The four stages of the game, in play order.
 *
 * <p>Each constant stores an explicit {@link #orderIndex()} (1 = first stage), so the
 * order never depends on the declaration position or {@code ordinal()}.
 */
public enum RoomId {

    /** Stage 1. */
    OFFICE(1),
    /** Stage 2. */
    LIBRARY(2),
    /** Stage 3. */
    LABORATORY(3),
    /** Stage 4: the last room. */
    FINAL_EXIT(4);

    private final int orderIndex;

    RoomId(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    /** @return the stage number of this room, from 1 (OFFICE) to 4 (FINAL_EXIT) */
    public int orderIndex() {
        return orderIndex;
    }

    /**
     * @param other another room; must not be {@code null}
     * @return {@code true} if this room comes earlier in the game than {@code other}
     */
    public boolean isBefore(RoomId other) {
        return orderIndex < other.orderIndex;
    }

    /** @return the room that follows this one, or empty for {@link #FINAL_EXIT} */
    public Optional<RoomId> next() {
        return inOrder().stream()
                .filter(room -> room.orderIndex == orderIndex + 1)
                .findFirst();
    }

    /** @return all rooms sorted by {@link #orderIndex()}, as an unmodifiable list */
    public static List<RoomId> inOrder() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(RoomId::orderIndex))
                .toList();
    }

    /**
     * @param index a stage number
     * @return the room with that order index
     * @throws IllegalArgumentException if no room has that index
     */
    public static RoomId fromOrderIndex(int index) {
        return inOrder().stream()
                .filter(room -> room.orderIndex == index)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No room has order index " + index));
    }
}
