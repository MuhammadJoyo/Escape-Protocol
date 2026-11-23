package com.escapeprotocol.game;

import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.inventory.ItemKind;
import com.escapeprotocol.puzzles.FinalExitPuzzle;
import com.escapeprotocol.puzzles.LibraryPuzzle;
import com.escapeprotocol.puzzles.OfficePuzzle;
import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.puzzles.SequencePuzzle;
import com.escapeprotocol.rooms.Room;
import com.escapeprotocol.rooms.RoomId;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The fixed definitions of a game: the four rooms, the reward item of each room, and the puzzles.
 *
 * <p>Rooms and reward items are defined here. The puzzles themselves are supplied from outside
 * (Module 05 provides the real ones), so this class contains no puzzle implementation.
 * {@link #missingPuzzleIds()} lists required puzzles that have not been supplied yet.
 *
 * <p>Reward rule: when every required puzzle of a room is solved, the game session adds that room's
 * reward item ({@link #completionItem(RoomId)}) to the inventory. The Final Exit has no reward;
 * solving it wins the game.
 *
 * <p>The catalog is immutable after construction and safe to share between threads. Its ids come
 * from the approved Module 04 decisions: the puzzle id {@code lab_sequence} and the item ids
 * {@code office_key} and {@code code_7394} appear in the SRS and Design; the others were approved
 * for Module 04 and may be revisited in Module 05.
 */
public final class GameCatalog {

    /** Puzzle id of the Office puzzle. */
    public static final String OFFICE_PUZZLE_ID = "office_year";
    /** Puzzle id of the Library puzzle. */
    public static final String LIBRARY_PUZZLE_ID = "library_order";
    /** Puzzle id of the Laboratory sequence puzzle. */
    public static final String LABORATORY_PUZZLE_ID = "lab_sequence";
    /** Puzzle id of the Final Exit puzzle. */
    public static final String FINAL_EXIT_PUZZLE_ID = "final_exit";

    /** Id of the item rewarded for completing the Office. */
    public static final String OFFICE_KEY_ID = "office_key";
    /** Id of the item rewarded for completing the Library. */
    public static final String LIBRARY_CODE_ID = "code_7394";
    /** Id of the item rewarded for completing the Laboratory. */
    public static final String LABORATORY_CLUE_ID = "lab_clue_30";

    private final EnumMap<RoomId, Room> rooms = new EnumMap<>(RoomId.class);
    private final EnumMap<RoomId, Item> completionItems = new EnumMap<>(RoomId.class);
    private final Map<String, Puzzle> puzzlesById = new LinkedHashMap<>();
    private final List<Room> roomsInOrder;

    /**
     * Creates a catalog after checking that its parts fit together.
     *
     * @param rooms exactly one room for each {@link RoomId}; no puzzle id may appear in two rooms
     * @param completionItems the reward item of each room (a room may have none); item ids must
     *        be unique
     * @param puzzles the available puzzles; each must be listed in the required puzzle ids of its
     *        own room, and ids must be unique. It is allowed to supply fewer puzzles than the rooms
     *        require (see {@link #missingPuzzleIds()})
     * @throws NullPointerException if an argument or any element is {@code null}
     * @throws IllegalArgumentException if a room is missing or duplicated, a puzzle id or an item
     *         id is duplicated, or a puzzle does not belong to a required slot of its room
     */
    public GameCatalog(Collection<Room> rooms, Map<RoomId, Item> completionItems,
                       Collection<Puzzle> puzzles) {
        Objects.requireNonNull(rooms, "rooms must not be null");
        Objects.requireNonNull(completionItems, "completionItems must not be null");
        Objects.requireNonNull(puzzles, "puzzles must not be null");

        for (Room room : rooms) {
            Objects.requireNonNull(room, "room must not be null");
            if (this.rooms.put(room.id(), room) != null) {
                throw new IllegalArgumentException("Duplicate room: " + room.id());
            }
        }
        for (RoomId id : RoomId.values()) {
            if (!this.rooms.containsKey(id)) {
                throw new IllegalArgumentException("Missing room: " + id);
            }
        }

        Set<String> requiredIds = new HashSet<>();
        for (Room room : this.rooms.values()) {
            for (String puzzleId : room.requiredPuzzleIds()) {
                if (!requiredIds.add(puzzleId)) {
                    throw new IllegalArgumentException("Puzzle id used by two rooms: " + puzzleId);
                }
            }
        }

        Set<String> itemIds = new HashSet<>();
        for (Map.Entry<RoomId, Item> entry : completionItems.entrySet()) {
            RoomId roomId = Objects.requireNonNull(entry.getKey(), "reward room must not be null");
            Item item = Objects.requireNonNull(entry.getValue(), "reward item must not be null");
            if (!itemIds.add(item.id())) {
                throw new IllegalArgumentException("Duplicate reward item id: " + item.id());
            }
            this.completionItems.put(roomId, item);
        }

        for (Puzzle puzzle : puzzles) {
            Objects.requireNonNull(puzzle, "puzzle must not be null");
            String puzzleId = Objects.requireNonNull(puzzle.id(), "puzzle id must not be null");
            RoomId roomId = Objects.requireNonNull(puzzle.roomId(), "puzzle room must not be null");
            if (!this.rooms.get(roomId).requiredPuzzleIds().contains(puzzleId)) {
                throw new IllegalArgumentException(
                        "Puzzle " + puzzleId + " is not required by room " + roomId);
            }
            if (puzzlesById.putIfAbsent(puzzleId, puzzle) != null) {
                throw new IllegalArgumentException("Duplicate puzzle id: " + puzzleId);
            }
        }

        this.roomsInOrder = RoomId.inOrder().stream().map(this.rooms::get).toList();
    }

    /**
     * Creates the standard game: the four standard rooms and reward items, with the given puzzles.
     *
     * @param puzzles the available puzzles (see the constructor for the rules)
     * @return the catalog
     */
    public static GameCatalog standard(Collection<Puzzle> puzzles) {
        return new GameCatalog(standardRooms(), standardCompletionItems(), puzzles);
    }

    /**
     * Creates the real game: the standard rooms and reward items with the four real puzzles
     * ({@link OfficePuzzle}, {@link LibraryPuzzle}, {@link SequencePuzzle},
     * {@link FinalExitPuzzle}). The result has no missing puzzles. The symbol puzzle (FR-PZ-07,
     * P2) is deferred, so the Laboratory requires {@code lab_sequence} alone.
     *
     * @return a complete catalog
     */
    public static GameCatalog standardWithPuzzles() {
        return standard(List.of(new OfficePuzzle(), new LibraryPuzzle(), new SequencePuzzle(),
                new FinalExitPuzzle()));
    }

    /**
     * @return the four standard rooms in play order: one required puzzle each
     */
    public static List<Room> standardRooms() {
        return List.of(
                new Room(RoomId.OFFICE, RoomId.OFFICE.orderIndex(), List.of(OFFICE_PUZZLE_ID)),
                new Room(RoomId.LIBRARY, RoomId.LIBRARY.orderIndex(), List.of(LIBRARY_PUZZLE_ID)),
                new Room(RoomId.LABORATORY, RoomId.LABORATORY.orderIndex(),
                        List.of(LABORATORY_PUZZLE_ID)),
                new Room(RoomId.FINAL_EXIT, RoomId.FINAL_EXIT.orderIndex(),
                        List.of(FINAL_EXIT_PUZZLE_ID)));
    }

    /**
     * @return the standard reward items: Office, Library and Laboratory each reward one item;
     *         the Final Exit has none. The returned map cannot be modified.
     */
    public static Map<RoomId, Item> standardCompletionItems() {
        return Map.of(
                RoomId.OFFICE, new Item(OFFICE_KEY_ID, "Office Key",
                        "A brass key from the Office drawer. The password year was 2004.",
                        ItemKind.KEY),
                RoomId.LIBRARY, new Item(LIBRARY_CODE_ID, "Library Code",
                        "The books in the right order reveal the code 7394.",
                        ItemKind.CODE),
                RoomId.LABORATORY, new Item(LABORATORY_CLUE_ID, "Laboratory Clue",
                        "The next number in the sequence 2, 6, 12, 20 is 30.",
                        ItemKind.CLUE));
    }

    /** @return the rooms in play order, as an unmodifiable list */
    public List<Room> rooms() {
        return roomsInOrder;
    }

    /**
     * @param id a room id; must not be {@code null}
     * @return the room definition
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public Room room(RoomId id) {
        Objects.requireNonNull(id, "id must not be null");
        return rooms.get(id);
    }

    /**
     * @param id a room id; must not be {@code null}
     * @return the item rewarded for completing that room, or empty if the room has none
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public Optional<Item> completionItem(RoomId id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(completionItems.get(id));
    }

    /**
     * @param itemId an item id; may be {@code null}
     * @return the reward item with that id, or empty if there is none (or {@code itemId} is null)
     */
    public Optional<Item> item(String itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return completionItems.values().stream().filter(item -> item.id().equals(itemId)).findFirst();
    }

    /**
     * @param puzzleId a puzzle id; may be {@code null}
     * @return the puzzle with that id, or empty if it was not supplied (or {@code puzzleId} is null)
     */
    public Optional<Puzzle> puzzle(String puzzleId) {
        if (puzzleId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(puzzlesById.get(puzzleId));
    }

    /** @return the supplied puzzles, as an unmodifiable list */
    public List<Puzzle> puzzles() {
        return List.copyOf(puzzlesById.values());
    }

    /**
     * @return the required puzzle ids (in room order) for which no puzzle has been supplied; an
     *         empty list means the catalog is complete
     */
    public List<String> missingPuzzleIds() {
        Set<String> missing = new LinkedHashSet<>();
        for (Room room : roomsInOrder) {
            for (String puzzleId : room.requiredPuzzleIds()) {
                if (!puzzlesById.containsKey(puzzleId)) {
                    missing.add(puzzleId);
                }
            }
        }
        return List.copyOf(missing);
    }
}
