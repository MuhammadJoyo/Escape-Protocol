package com.escapeprotocol.rooms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomTest {

    @Test
    void createsRoomWithMatchingOrderIndex() {
        Room room = new Room(RoomId.LIBRARY, 2, List.of("library-order"));
        assertEquals(RoomId.LIBRARY, room.id());
        assertEquals(2, room.orderIndex());
        assertEquals(List.of("library-order"), room.requiredPuzzleIds());
    }

    @Test
    void rejectsOrderIndexThatDoesNotMatchRoomId() {
        assertThrows(IllegalArgumentException.class, () -> new Room(RoomId.OFFICE, 2, List.of("p")));
        assertThrows(IllegalArgumentException.class, () -> new Room(RoomId.FINAL_EXIT, 0, List.of("p")));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> new Room(null, 1, List.of("p")));
        assertThrows(NullPointerException.class, () -> new Room(RoomId.OFFICE, 1, null));
    }

    @Test
    void rejectsNullBlankAndDuplicatePuzzleIds() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(RoomId.OFFICE, 1, Arrays.asList("a", null)));
        assertThrows(IllegalArgumentException.class,
                () -> new Room(RoomId.OFFICE, 1, List.of("a", "  ")));
        assertThrows(IllegalArgumentException.class,
                () -> new Room(RoomId.OFFICE, 1, List.of("a", "a")));
        assertThrows(IllegalArgumentException.class,
                () -> new Room(RoomId.OFFICE, 1, List.of("a", " a ")));
    }

    @Test
    void copiesTheInputList() {
        List<String> source = new ArrayList<>(List.of("a", "b"));
        Room room = new Room(RoomId.OFFICE, 1, source);
        source.add("c");
        source.set(0, "changed");
        assertEquals(List.of("a", "b"), room.requiredPuzzleIds());
    }

    @Test
    void exposesOnlyAnUnmodifiableList() {
        Room room = new Room(RoomId.OFFICE, 1, List.of("a"));
        assertThrows(UnsupportedOperationException.class, () -> room.requiredPuzzleIds().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> room.requiredPuzzleIds().clear());
    }

    @Test
    void allowsEmptyPuzzleList() {
        assertTrue(new Room(RoomId.FINAL_EXIT, 4, List.of()).requiredPuzzleIds().isEmpty());
    }

    @Test
    void equalityUsesAllFields() {
        Room a = new Room(RoomId.OFFICE, 1, List.of("p1", "p2"));
        Room b = new Room(RoomId.OFFICE, 1, List.of("p1", "p2"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Room(RoomId.OFFICE, 1, List.of("p1")));
        assertNotEquals(a, new Room(RoomId.LIBRARY, 2, List.of("p1", "p2")));
    }
}
