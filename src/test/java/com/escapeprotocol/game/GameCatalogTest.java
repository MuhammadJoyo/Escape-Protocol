package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.inventory.ItemKind;
import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.rooms.Room;
import com.escapeprotocol.rooms.RoomId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GameCatalogTest {

    private static List<Puzzle> allFakePuzzles() {
        return List.of(
                FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE),
                FakePuzzle.simple(GameCatalog.LIBRARY_PUZZLE_ID, RoomId.LIBRARY),
                FakePuzzle.simple(GameCatalog.LABORATORY_PUZZLE_ID, RoomId.LABORATORY),
                FakePuzzle.simple(GameCatalog.FINAL_EXIT_PUZZLE_ID, RoomId.FINAL_EXIT));
    }

    @Test
    void approvedIdsHaveTheDecidedValues() {
        assertEquals("office_year", GameCatalog.OFFICE_PUZZLE_ID);
        assertEquals("library_order", GameCatalog.LIBRARY_PUZZLE_ID);
        assertEquals("lab_sequence", GameCatalog.LABORATORY_PUZZLE_ID);
        assertEquals("final_exit", GameCatalog.FINAL_EXIT_PUZZLE_ID);
        assertEquals("office_key", GameCatalog.OFFICE_KEY_ID);
        assertEquals("code_7394", GameCatalog.LIBRARY_CODE_ID);
        assertEquals("lab_clue_30", GameCatalog.LABORATORY_CLUE_ID);
    }

    @Test
    void standardRoomsFollowTheRoomIdOrderAndRequireOnePuzzleEach() {
        GameCatalog catalog = GameCatalog.standard(List.of());
        List<Room> rooms = catalog.rooms();

        assertEquals(RoomId.inOrder(), rooms.stream().map(Room::id).toList());
        for (Room room : rooms) {
            assertEquals(room.id().orderIndex(), room.orderIndex());
            assertEquals(1, room.requiredPuzzleIds().size());
        }
        assertEquals(List.of("office_year"), catalog.room(RoomId.OFFICE).requiredPuzzleIds());
        assertEquals(List.of("library_order"), catalog.room(RoomId.LIBRARY).requiredPuzzleIds());
        assertEquals(List.of("lab_sequence"), catalog.room(RoomId.LABORATORY).requiredPuzzleIds());
        assertEquals(List.of("final_exit"), catalog.room(RoomId.FINAL_EXIT).requiredPuzzleIds());
    }

    @Test
    void completionItemsAreKeyCodeAndClueAndFinalExitHasNone() {
        GameCatalog catalog = GameCatalog.standard(List.of());

        Item key = catalog.completionItem(RoomId.OFFICE).orElseThrow();
        Item code = catalog.completionItem(RoomId.LIBRARY).orElseThrow();
        Item clue = catalog.completionItem(RoomId.LABORATORY).orElseThrow();

        assertEquals("office_key", key.id());
        assertEquals(ItemKind.KEY, key.kind());
        assertEquals("code_7394", code.id());
        assertEquals(ItemKind.CODE, code.kind());
        assertEquals("lab_clue_30", clue.id());
        assertEquals(ItemKind.CLUE, clue.kind());
        assertEquals(Optional.empty(), catalog.completionItem(RoomId.FINAL_EXIT));
    }

    @Test
    void rewardItemTextsContainTheValuesNeededForTheFinalAnswer() {
        GameCatalog catalog = GameCatalog.standard(List.of());
        assertTrue(catalog.completionItem(RoomId.OFFICE).orElseThrow().description().contains("2004"));
        assertTrue(catalog.completionItem(RoomId.LIBRARY).orElseThrow().description().contains("7394"));
        assertTrue(catalog.completionItem(RoomId.LABORATORY).orElseThrow().description().contains("30"));
    }

    @Test
    void itemsCanBeLookedUpById() {
        GameCatalog catalog = GameCatalog.standard(List.of());
        assertEquals("Office Key", catalog.item("office_key").orElseThrow().name());
        assertEquals(Optional.empty(), catalog.item("nothing"));
        assertEquals(Optional.empty(), catalog.item(null));
    }

    @Test
    void catalogWithoutPuzzlesReportsEveryRequiredPuzzleAsMissing() {
        GameCatalog catalog = GameCatalog.standard(List.of());
        assertEquals(List.of("office_year", "library_order", "lab_sequence", "final_exit"),
                catalog.missingPuzzleIds());
        assertEquals(Optional.empty(), catalog.puzzle("office_year"));
        assertTrue(catalog.puzzles().isEmpty());
    }

    @Test
    void suppliedPuzzlesAreFoundAndMissingListShrinks() {
        List<Puzzle> two = List.of(
                FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE),
                FakePuzzle.simple(GameCatalog.FINAL_EXIT_PUZZLE_ID, RoomId.FINAL_EXIT));
        GameCatalog catalog = GameCatalog.standard(two);

        assertSame(two.get(0), catalog.puzzle("office_year").orElseThrow());
        assertEquals(List.of("library_order", "lab_sequence"), catalog.missingPuzzleIds());
        assertEquals(Optional.empty(), catalog.puzzle("unknown"));
        assertEquals(Optional.empty(), catalog.puzzle(null));
    }

    @Test
    void completeCatalogHasNoMissingPuzzles() {
        GameCatalog catalog = GameCatalog.standard(allFakePuzzles());
        assertTrue(catalog.missingPuzzleIds().isEmpty());
        assertEquals(4, catalog.puzzles().size());
    }

    @Test
    void returnedListsCannotBeModified() {
        GameCatalog catalog = GameCatalog.standard(allFakePuzzles());
        assertThrows(UnsupportedOperationException.class, () -> catalog.rooms().clear());
        assertThrows(UnsupportedOperationException.class, () -> catalog.puzzles().clear());
        assertThrows(UnsupportedOperationException.class, () -> catalog.missingPuzzleIds().clear());
    }

    @Test
    void suppliedPuzzleCollectionIsCopied() {
        List<Puzzle> source = new ArrayList<>(List.of(
                FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE)));
        GameCatalog catalog = GameCatalog.standard(source);
        source.add(FakePuzzle.simple(GameCatalog.LIBRARY_PUZZLE_ID, RoomId.LIBRARY));
        assertEquals(1, catalog.puzzles().size());
    }

    @Test
    void rejectsPuzzleThatNoRoomRequires() {
        List<Puzzle> bad = List.of(FakePuzzle.simple("surprise", RoomId.OFFICE));
        assertThrows(IllegalArgumentException.class, () -> GameCatalog.standard(bad));
    }

    @Test
    void rejectsPuzzleDeclaredInTheWrongRoom() {
        List<Puzzle> bad = List.of(FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.LIBRARY));
        assertThrows(IllegalArgumentException.class, () -> GameCatalog.standard(bad));
    }

    @Test
    void rejectsDuplicatePuzzleIdsAndNullPuzzles() {
        Puzzle one = FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE);
        Puzzle again = FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE);
        assertThrows(IllegalArgumentException.class, () -> GameCatalog.standard(List.of(one, again)));
        List<Puzzle> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> GameCatalog.standard(withNull));
    }

    @Test
    void rejectsMissingOrDuplicateRooms() {
        List<Room> three = GameCatalog.standardRooms().subList(0, 3);
        assertThrows(IllegalArgumentException.class,
                () -> new GameCatalog(three, GameCatalog.standardCompletionItems(), List.of()));

        List<Room> duplicate = new ArrayList<>(GameCatalog.standardRooms());
        duplicate.add(new Room(RoomId.OFFICE, 1, List.of("another")));
        assertThrows(IllegalArgumentException.class,
                () -> new GameCatalog(duplicate, GameCatalog.standardCompletionItems(), List.of()));
    }

    @Test
    void rejectsAPuzzleIdUsedByTwoRooms() {
        List<Room> rooms = List.of(
                new Room(RoomId.OFFICE, 1, List.of("shared")),
                new Room(RoomId.LIBRARY, 2, List.of("shared")),
                new Room(RoomId.LABORATORY, 3, List.of("c")),
                new Room(RoomId.FINAL_EXIT, 4, List.of("d")));
        assertThrows(IllegalArgumentException.class,
                () -> new GameCatalog(rooms, Map.of(), List.of()));
    }

    @Test
    void rejectsDuplicateRewardItemIds() {
        Item first = new Item("same", "A", "a", ItemKind.KEY);
        Item second = new Item("same", "B", "b", ItemKind.CODE);
        Map<RoomId, Item> rewards = Map.of(RoomId.OFFICE, first, RoomId.LIBRARY, second);
        assertThrows(IllegalArgumentException.class,
                () -> new GameCatalog(GameCatalog.standardRooms(), rewards, List.of()));
    }

    @Test
    void rejectsNullArgumentsAndLookups() {
        assertThrows(NullPointerException.class, () -> GameCatalog.standard(null));
        assertThrows(NullPointerException.class,
                () -> new GameCatalog(null, Map.of(), List.of()));
        GameCatalog catalog = GameCatalog.standard(List.of());
        assertThrows(NullPointerException.class, () -> catalog.room(null));
        assertThrows(NullPointerException.class, () -> catalog.completionItem(null));
        assertFalse(catalog.rooms().isEmpty());
    }
}
