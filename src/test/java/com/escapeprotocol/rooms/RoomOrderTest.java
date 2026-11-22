package com.escapeprotocol.rooms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RoomOrderTest {

    @Test
    void roomsAreInTheRequiredOrder() {
        assertEquals(
                List.of(RoomId.OFFICE, RoomId.LIBRARY, RoomId.LABORATORY, RoomId.FINAL_EXIT),
                RoomId.inOrder());
    }

    @Test
    void orderIndexesAreOneToFour() {
        assertEquals(1, RoomId.OFFICE.orderIndex());
        assertEquals(2, RoomId.LIBRARY.orderIndex());
        assertEquals(3, RoomId.LABORATORY.orderIndex());
        assertEquals(4, RoomId.FINAL_EXIT.orderIndex());
    }

    @Test
    void nextWalksThroughAllRoomsAndEndsAtFinalExit() {
        assertEquals(Optional.of(RoomId.LIBRARY), RoomId.OFFICE.next());
        assertEquals(Optional.of(RoomId.LABORATORY), RoomId.LIBRARY.next());
        assertEquals(Optional.of(RoomId.FINAL_EXIT), RoomId.LABORATORY.next());
        assertEquals(Optional.empty(), RoomId.FINAL_EXIT.next());
    }

    @Test
    void isBeforeComparesStages() {
        assertTrue(RoomId.OFFICE.isBefore(RoomId.FINAL_EXIT));
        assertTrue(RoomId.LIBRARY.isBefore(RoomId.LABORATORY));
        assertFalse(RoomId.LABORATORY.isBefore(RoomId.LIBRARY));
        assertFalse(RoomId.OFFICE.isBefore(RoomId.OFFICE));
    }

    @Test
    void fromOrderIndexRoundTripsAndRejectsUnknownIndexes() {
        for (RoomId room : RoomId.values()) {
            assertEquals(room, RoomId.fromOrderIndex(room.orderIndex()));
        }
        assertThrows(IllegalArgumentException.class, () -> RoomId.fromOrderIndex(0));
        assertThrows(IllegalArgumentException.class, () -> RoomId.fromOrderIndex(5));
    }

    @Test
    void inOrderListCannotBeModified() {
        assertThrows(UnsupportedOperationException.class, () -> RoomId.inOrder().clear());
    }
}
