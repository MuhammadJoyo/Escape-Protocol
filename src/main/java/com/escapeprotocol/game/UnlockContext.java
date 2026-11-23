package com.escapeprotocol.game;

import java.util.Set;

/**
 * Immutable view of what the player has: the ids of owned items and of solved puzzles.
 * An {@link UnlockRule} decides from this view alone whether a room is unlocked.
 *
 * <p>Both sets are copied on construction, so later changes to the caller's sets have no effect,
 * and the sets exposed by this record cannot be modified.
 *
 * @param itemIds ids of the items in the inventory
 * @param solvedPuzzleIds ids of the puzzles that are solved
 */
public record UnlockContext(Set<String> itemIds, Set<String> solvedPuzzleIds) {

    /**
     * Creates a context with defensive copies of both sets.
     *
     * @throws NullPointerException if a set is {@code null} or contains {@code null}
     */
    public UnlockContext {
        itemIds = Set.copyOf(itemIds);
        solvedPuzzleIds = Set.copyOf(solvedPuzzleIds);
    }

    /**
     * @param itemId an item id; may be {@code null}
     * @return {@code true} if the player owns the item
     */
    public boolean hasItem(String itemId) {
        return itemId != null && itemIds.contains(itemId);
    }

    /**
     * @param puzzleId a puzzle id; may be {@code null}
     * @return {@code true} if the puzzle is solved
     */
    public boolean isSolved(String puzzleId) {
        return puzzleId != null && solvedPuzzleIds.contains(puzzleId);
    }
}
