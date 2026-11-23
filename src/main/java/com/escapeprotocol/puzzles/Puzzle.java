package com.escapeprotocol.puzzles;

import com.escapeprotocol.rooms.RoomId;
import java.util.List;
import java.util.Set;

/**
 * The contract every puzzle fulfils (FR-PZ-01). The game engine only knows this interface, so a
 * new puzzle can be added without changing the engine. The real puzzles (Office, Library,
 * Laboratory, Final Exit) are implemented in Module 05.
 *
 * <p>A puzzle only <em>checks answers</em>. It never changes the game, the score or the
 * inventory; the game session does that (items are granted only by the engine, FR-IV-02).
 */
public interface Puzzle {

    /**
     * @return the unique puzzle id, e.g. {@code "office_year"}; never {@code null} or blank
     */
    String id();

    /**
     * @return the room this puzzle belongs to; never {@code null}
     */
    RoomId roomId();

    /**
     * @return the text shown to the player; never {@code null}
     */
    String prompt();

    /**
     * Checks an answer.
     *
     * <p>Specification: requires nothing (the answer may be {@code null}, blank or any text);
     * has no effects (pure); never throws.
     *
     * @param answer the player's raw answer; may be {@code null}
     * @return {@code INVALID_FORMAT} if the answer fails the puzzle's format check;
     *         {@code CORRECT} if the (trimmed) answer is the expected one; otherwise
     *         {@code INCORRECT}. Never {@code null}.
     */
    PuzzleResult submit(String answer);

    /**
     * @return the progressive hint texts, easiest first; an unmodifiable list. At most three are
     *         used by the game (FR-IV-04).
     */
    List<String> hints();

    /**
     * @return ids of the items the player must already own before this puzzle may be answered
     *         (only the Final Exit needs any); an unmodifiable set, empty for most puzzles
     */
    Set<String> requiredItemIds();
}
