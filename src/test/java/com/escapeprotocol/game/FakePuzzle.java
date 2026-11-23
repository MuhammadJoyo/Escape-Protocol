package com.escapeprotocol.game;

import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.puzzles.PuzzleResult;
import com.escapeprotocol.rooms.RoomId;
import java.util.List;
import java.util.Set;

/**
 * Test-only {@link Puzzle} used to test the engine before the real puzzles exist (Module 05).
 * It lives in the test sources and is never part of the game.
 *
 * <p>Answer rule: {@code null} or blank is INVALID_FORMAT; the exact (trimmed) correct answer is
 * CORRECT; anything else is INCORRECT.
 */
final class FakePuzzle implements Puzzle {

    private final String id;
    private final RoomId roomId;
    private final String correctAnswer;
    private final List<String> hints;
    private final Set<String> requiredItemIds;

    FakePuzzle(String id, RoomId roomId, String correctAnswer, List<String> hints,
               Set<String> requiredItemIds) {
        this.id = id;
        this.roomId = roomId;
        this.correctAnswer = correctAnswer;
        this.hints = List.copyOf(hints);
        this.requiredItemIds = Set.copyOf(requiredItemIds);
    }

    /** A puzzle with the answer "ok", three hints and no required items. */
    static FakePuzzle simple(String id, RoomId roomId) {
        return new FakePuzzle(id, roomId, "ok",
                List.of(id + " hint 1", id + " hint 2", id + " hint 3"), Set.of());
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RoomId roomId() {
        return roomId;
    }

    @Override
    public String prompt() {
        return "Prompt of " + id;
    }

    @Override
    public PuzzleResult submit(String answer) {
        if (answer == null || answer.isBlank()) {
            return new PuzzleResult(PuzzleResult.Status.INVALID_FORMAT, "Please type an answer.");
        }
        if (answer.trim().equals(correctAnswer)) {
            return new PuzzleResult(PuzzleResult.Status.CORRECT, "Correct.");
        }
        return new PuzzleResult(PuzzleResult.Status.INCORRECT, "Wrong.");
    }

    @Override
    public List<String> hints() {
        return hints;
    }

    @Override
    public Set<String> requiredItemIds() {
        return requiredItemIds;
    }
}
