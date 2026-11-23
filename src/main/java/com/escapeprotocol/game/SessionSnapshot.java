package com.escapeprotocol.game;

import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.rooms.RoomId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable copy of a game session's state at one moment, for the UI and (later) for saving.
 *
 * <p>All collections are copied on construction and cannot be modified, and every {@link Item} is
 * immutable, so a snapshot can be shared between threads and never changes, even if the session
 * keeps changing. It holds no reference to the session.
 *
 * @param playerName the player's name
 * @param state the game state
 * @param score the current score (0 or more)
 * @param remainingSeconds the seconds left (0 or more)
 * @param unlockedRooms the unlocked rooms, in play order
 * @param solvedPuzzleIds ids of the solved puzzles
 * @param items the collected items, in pickup order
 * @param revealedHints for each puzzle that has revealed hints, how many are revealed
 * @param wrongAttempts the number of wrong (valid-format) answers so far
 */
public record SessionSnapshot(
        String playerName,
        GameState state,
        int score,
        int remainingSeconds,
        List<RoomId> unlockedRooms,
        Set<String> solvedPuzzleIds,
        List<Item> items,
        Map<String, Integer> revealedHints,
        int wrongAttempts) {

    /**
     * Creates a snapshot with defensive copies.
     *
     * @throws NullPointerException if an argument (or a collection element) is {@code null}
     * @throws IllegalArgumentException if {@code score}, {@code remainingSeconds} or
     *         {@code wrongAttempts} is negative
     */
    public SessionSnapshot {
        Objects.requireNonNull(playerName, "playerName must not be null");
        Objects.requireNonNull(state, "state must not be null");
        if (score < 0) {
            throw new IllegalArgumentException("score must not be negative: " + score);
        }
        if (remainingSeconds < 0) {
            throw new IllegalArgumentException("remainingSeconds must not be negative: " + remainingSeconds);
        }
        if (wrongAttempts < 0) {
            throw new IllegalArgumentException("wrongAttempts must not be negative: " + wrongAttempts);
        }
        unlockedRooms = List.copyOf(unlockedRooms);
        solvedPuzzleIds = Set.copyOf(solvedPuzzleIds);
        items = List.copyOf(items);
        revealedHints = Map.copyOf(revealedHints);
    }

    /**
     * @param room a room; may be {@code null}
     * @return {@code true} if the room is unlocked in this snapshot
     */
    public boolean isUnlocked(RoomId room) {
        return unlockedRooms.contains(room);
    }

    /**
     * @param puzzleId a puzzle id; may be {@code null}
     * @return how many hints of that puzzle are revealed (0 if none)
     */
    public int hintsRevealed(String puzzleId) {
        return revealedHints.getOrDefault(puzzleId, 0);
    }
}
