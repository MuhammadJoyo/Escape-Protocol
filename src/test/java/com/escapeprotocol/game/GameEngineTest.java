package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.rooms.RoomId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link GameEngine} together with the real {@link GameSession}, {@link GameCatalog},
 * {@link HardCodedUnlockRule}, {@link ScoreCalculator} and {@link HintService}. Only the puzzles are
 * test doubles ({@link FakePuzzle}); the real ones arrive in Module 05. All tests are
 * single-threaded and do not depend on timing.
 */
class GameEngineTest {

    private static final String OFFICE = GameCatalog.OFFICE_PUZZLE_ID;
    private static final String LIBRARY = GameCatalog.LIBRARY_PUZZLE_ID;
    private static final String LAB = GameCatalog.LABORATORY_PUZZLE_ID;
    private static final String FINAL = GameCatalog.FINAL_EXIT_PUZZLE_ID;
    private static final String OK = "ok";
    private static final String WRONG = "nope";

    /** Records the states reported to the listener. */
    private static final class RecordingListener implements GameListener {
        final List<GameState> states = new CopyOnWriteArrayList<>();

        @Override
        public void onTick(int remainingSeconds) {
            // Not used in Module 04.
        }

        @Override
        public void onStateChanged(GameState state) {
            states.add(state);
        }
    }

    /** An engine with a complete fake-puzzle catalog, a controllable clock and a listener. */
    private static final class Fixture {
        final AtomicInteger remaining = new AtomicInteger(600);
        final RecordingListener listener = new RecordingListener();
        final GameCatalog catalog = completeCatalog();
        final GameEngine engine = new GameEngine(catalog, new HardCodedUnlockRule(), remaining::get, listener);

        static GameCatalog completeCatalog() {
            Puzzle finalExit = new FakePuzzle(FINAL, RoomId.FINAL_EXIT, OK,
                    List.of("final hint 1", "final hint 2", "final hint 3"),
                    Set.of(GameCatalog.OFFICE_KEY_ID, GameCatalog.LIBRARY_CODE_ID, GameCatalog.LABORATORY_CLUE_ID));
            return GameCatalog.standard(List.of(
                    FakePuzzle.simple(OFFICE, RoomId.OFFICE),
                    FakePuzzle.simple(LIBRARY, RoomId.LIBRARY),
                    FakePuzzle.simple(LAB, RoomId.LABORATORY),
                    finalExit));
        }

        Fixture withGame() {
            engine.newGame("Alice");
            return this;
        }

        void solveThroughLaboratory() {
            assertEquals(SubmissionResult.Status.CORRECT, engine.submit(OFFICE, OK).status());
            assertEquals(SubmissionResult.Status.CORRECT, engine.submit(LIBRARY, OK).status());
            assertEquals(SubmissionResult.Status.CORRECT, engine.submit(LAB, OK).status());
        }
    }

    // ------------------------------------------------------------------ construction

    @Test
    void constructorsRejectNullDependencies() {
        GameCatalog catalog = Fixture.completeCatalog();
        UnlockRule rule = new HardCodedUnlockRule();
        assertThrows(NullPointerException.class, () -> new GameEngine(null, rule, () -> 1));
        assertThrows(NullPointerException.class, () -> new GameEngine(catalog, null, () -> 1));
        assertThrows(NullPointerException.class, () -> new GameEngine(catalog, rule, null));
        assertThrows(NullPointerException.class, () -> new GameEngine(catalog, rule, () -> 1, null));
        assertThrows(NullPointerException.class, () -> new GameEngine(null, rule, () -> 1, new RecordingListener()));
    }

    @Test
    void engineWithoutListenerWorks() {
        GameEngine engine = new GameEngine(Fixture.completeCatalog(), new HardCodedUnlockRule(), () -> 600);
        engine.newGame("Alice");
        assertEquals(GameState.RUNNING, engine.snapshot().state());
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit(OFFICE, OK).status());
    }

    @Test
    void operationsBeforeAnyGameFailWithIllegalState() {
        Fixture f = new Fixture();
        assertThrows(IllegalStateException.class, () -> f.engine.submit(OFFICE, OK));
        assertThrows(IllegalStateException.class, () -> f.engine.requestHint(OFFICE));
        assertThrows(IllegalStateException.class, () -> f.engine.viewHint(OFFICE, 1));
        assertThrows(IllegalStateException.class, () -> f.engine.isUnlocked(RoomId.OFFICE));
        assertThrows(IllegalStateException.class, () -> f.engine.pause());
        assertThrows(IllegalStateException.class, () -> f.engine.resume());
        assertThrows(IllegalStateException.class, () -> f.engine.snapshot());
        assertThrows(IllegalStateException.class, () -> f.engine.restart());
        assertTrue(f.listener.states.isEmpty());
    }

    // ------------------------------------------------------------------ new game

    @Test
    void newGameCreatesAStartedFreshSession() {
        Fixture f = new Fixture();
        f.engine.newGame("  Alice  ");
        SessionSnapshot s = f.engine.snapshot();
        assertEquals("Alice", s.playerName());
        assertEquals(GameState.RUNNING, s.state());
        assertEquals(1000, s.score());
        assertEquals(600, s.remainingSeconds());
        assertEquals(List.of(RoomId.OFFICE), s.unlockedRooms());
        assertTrue(s.items().isEmpty());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.revealedHints().isEmpty());
        assertEquals(0, s.wrongAttempts());
    }

    @Test
    void newGameNotifiesTheListenerExactlyOncePerSession() {
        Fixture f = new Fixture();
        f.engine.newGame("Alice");
        assertEquals(List.of(GameState.RUNNING), f.listener.states);
        f.engine.newGame("Bob");
        assertEquals(List.of(GameState.RUNNING, GameState.RUNNING), f.listener.states);
    }

    @Test
    void invalidNamesAreRejectedAndCreateNoGame() {
        Fixture f = new Fixture();
        for (String bad : new String[] {null, "", "   ", "A".repeat(21), "bad!name", "two\nlines"}) {
            assertThrows(IllegalArgumentException.class, () -> f.engine.newGame(bad));
        }
        assertThrows(IllegalStateException.class, () -> f.engine.snapshot());
        assertTrue(f.listener.states.isEmpty());
    }

    @Test
    void invalidNameDoesNotDisturbTheCurrentGame() {
        Fixture f = new Fixture().withGame();
        f.engine.submit(OFFICE, WRONG);
        assertThrows(IllegalArgumentException.class, () -> f.engine.newGame("bad!"));
        SessionSnapshot s = f.engine.snapshot();
        assertEquals("Alice", s.playerName());
        assertEquals(975, s.score());
        assertEquals(List.of(GameState.RUNNING), f.listener.states);
    }

    @Test
    void newGameReplacesTheOldSessionWithAFreshOne() {
        Fixture f = new Fixture().withGame();
        f.engine.submit(OFFICE, OK);
        f.engine.requestHint(LIBRARY);
        f.engine.newGame("Bob");
        SessionSnapshot s = f.engine.snapshot();
        assertEquals("Bob", s.playerName());
        assertEquals(1000, s.score());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.items().isEmpty());
        assertEquals(List.of(RoomId.OFFICE), s.unlockedRooms());
        assertFalse(f.engine.isUnlocked(RoomId.LIBRARY));
    }

    // ------------------------------------------------------------------ submit

    @Test
    void submitDelegatesToTheSessionAndReturnsItsResults() {
        Fixture f = new Fixture().withGame();
        assertEquals(SubmissionResult.Status.INVALID_FORMAT, f.engine.submit(OFFICE, "").status());
        assertEquals(1000, f.engine.snapshot().score());

        assertEquals(SubmissionResult.Status.INCORRECT, f.engine.submit(OFFICE, WRONG).status());
        SessionSnapshot afterWrong = f.engine.snapshot();
        assertEquals(975, afterWrong.score());
        assertEquals(1, afterWrong.wrongAttempts());

        SubmissionResult correct = f.engine.submit(OFFICE, OK);
        assertEquals(SubmissionResult.Status.CORRECT, correct.status());
        SessionSnapshot afterCorrect = f.engine.snapshot();
        assertEquals(Set.of(OFFICE), afterCorrect.solvedPuzzleIds());
        assertEquals(List.of(GameCatalog.OFFICE_KEY_ID), afterCorrect.items().stream().map(Item::id).toList());

        assertEquals(SubmissionResult.Status.ALREADY_SOLVED, f.engine.submit(OFFICE, OK).status());
    }

    @Test
    void lockedRoomsCannotBeReachedThroughTheEngine() {
        Fixture f = new Fixture().withGame();
        for (String locked : List.of(LIBRARY, LAB, FINAL)) {
            assertEquals(SubmissionResult.Status.REFUSED_LOCKED, f.engine.submit(locked, OK).status());
            assertEquals(SubmissionResult.Status.REFUSED_LOCKED, f.engine.submit(locked, WRONG).status());
        }
        SessionSnapshot s = f.engine.snapshot();
        assertEquals(1000, s.score());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.items().isEmpty());
    }

    @Test
    void unknownOrNullPuzzleIdsAreRejectedWithoutAnyEffect() {
        Fixture f = new Fixture().withGame();
        assertThrows(IllegalArgumentException.class, () -> f.engine.submit("no_such_puzzle", OK));
        assertThrows(IllegalArgumentException.class, () -> f.engine.submit(null, OK));
        assertThrows(IllegalArgumentException.class, () -> f.engine.requestHint("no_such_puzzle"));
        assertThrows(IllegalArgumentException.class, () -> f.engine.requestHint(null));
        assertThrows(IllegalArgumentException.class, () -> f.engine.viewHint("no_such_puzzle", 1));
        assertThrows(IllegalArgumentException.class, () -> f.engine.viewHint(null, 1));
        assertEquals(1000, f.engine.snapshot().score());
    }

    @Test
    void puzzleMissingFromAnIncompleteCatalogIsUnknownToTheEngine() {
        GameCatalog partial = GameCatalog.standard(List.of(FakePuzzle.simple(OFFICE, RoomId.OFFICE)));
        GameEngine engine = new GameEngine(partial, new HardCodedUnlockRule(), () -> 600);
        engine.newGame("Alice");
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit(OFFICE, OK).status());
        assertTrue(engine.isUnlocked(RoomId.LIBRARY));
        assertThrows(IllegalArgumentException.class, () -> engine.submit(LIBRARY, OK));
    }

    // ------------------------------------------------------------------ hints

    @Test
    void hintsAreDelegatedWithTheSessionCosts() {
        Fixture f = new Fixture().withGame();
        HintResult first = f.engine.requestHint(OFFICE);
        assertEquals(HintResult.Status.REVEALED_NEW, first.status());
        assertEquals(1, first.level());
        assertEquals(950, f.engine.snapshot().score());

        HintResult again = f.engine.viewHint(OFFICE, 1);
        assertEquals(HintResult.Status.VIEWED_AGAIN, again.status());
        assertEquals(first.text(), again.text());
        assertEquals(950, f.engine.snapshot().score());

        assertEquals(HintResult.Status.REFUSED, f.engine.viewHint(OFFICE, 2).status());
        assertEquals(1, f.engine.snapshot().hintsRevealed(OFFICE));
    }

    @Test
    void afterThreeHintsThereAreNoMoreAndNothingIsCharged() {
        Fixture f = new Fixture().withGame();
        f.engine.requestHint(OFFICE);
        f.engine.requestHint(OFFICE);
        f.engine.requestHint(OFFICE);
        assertEquals(850, f.engine.snapshot().score());
        assertEquals(HintResult.Status.NO_MORE_HINTS, f.engine.requestHint(OFFICE).status());
        assertEquals(850, f.engine.snapshot().score());
    }

    @Test
    void hintsForLockedRoomsAreRefusedForFree() {
        Fixture f = new Fixture().withGame();
        assertEquals(HintResult.Status.REFUSED, f.engine.requestHint(LIBRARY).status());
        assertEquals(HintResult.Status.REFUSED, f.engine.viewHint(LIBRARY, 1).status());
        assertEquals(1000, f.engine.snapshot().score());
    }

    // ------------------------------------------------------------------ room queries

    @Test
    void isUnlockedFollowsTheProgression() {
        Fixture f = new Fixture().withGame();
        assertTrue(f.engine.isUnlocked(RoomId.OFFICE));
        assertFalse(f.engine.isUnlocked(RoomId.LIBRARY));
        f.engine.submit(OFFICE, OK);
        assertTrue(f.engine.isUnlocked(RoomId.LIBRARY));
        assertFalse(f.engine.isUnlocked(RoomId.LABORATORY));
        f.engine.submit(LIBRARY, OK);
        assertTrue(f.engine.isUnlocked(RoomId.LABORATORY));
        assertFalse(f.engine.isUnlocked(RoomId.FINAL_EXIT));
        f.engine.submit(LAB, OK);
        assertTrue(f.engine.isUnlocked(RoomId.FINAL_EXIT));
    }

    @Test
    void isUnlockedRejectsNull() {
        Fixture f = new Fixture().withGame();
        assertThrows(NullPointerException.class, () -> f.engine.isUnlocked(null));
    }

    // ------------------------------------------------------------------ pause / resume

    @Test
    void pauseAndResumeAreDelegatedAndNotified() {
        Fixture f = new Fixture().withGame();
        assertTrue(f.engine.pause());
        assertEquals(GameState.PAUSED, f.engine.snapshot().state());
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.engine.submit(OFFICE, OK).status());
        assertEquals(HintResult.Status.REFUSED, f.engine.requestHint(OFFICE).status());
        assertFalse(f.engine.pause());

        assertTrue(f.engine.resume());
        assertEquals(GameState.RUNNING, f.engine.snapshot().state());
        assertFalse(f.engine.resume());
        assertEquals(List.of(GameState.RUNNING, GameState.PAUSED, GameState.RUNNING), f.listener.states);
    }

    // ------------------------------------------------------------------ whole game and terminal sessions

    @Test
    void playingTheWholeGameThroughTheEngineEndsInVictory() {
        Fixture f = new Fixture().withGame();
        f.engine.submit(OFFICE, WRONG);          // 975
        f.engine.requestHint(OFFICE);            // 925
        f.solveThroughLaboratory();
        f.remaining.set(300);
        SubmissionResult result = f.engine.submit(FINAL, OK);

        assertEquals(SubmissionResult.Status.VICTORY, result.status());
        SessionSnapshot s = f.engine.snapshot();
        assertEquals(GameState.VICTORY, s.state());
        assertEquals(925 + 2 * 300, s.score());
        assertEquals(300, s.remainingSeconds());
        assertEquals(List.of(GameCatalog.OFFICE_KEY_ID, GameCatalog.LIBRARY_CODE_ID, GameCatalog.LABORATORY_CLUE_ID),
                s.items().stream().map(Item::id).toList());
        assertEquals(List.of(GameState.RUNNING, GameState.VICTORY), f.listener.states);
    }

    @Test
    void finishedGamesAcceptNothingMore() {
        Fixture f = new Fixture().withGame();
        f.solveThroughLaboratory();
        f.engine.submit(FINAL, OK);
        SessionSnapshot before = f.engine.snapshot();

        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.engine.submit(OFFICE, WRONG).status());
        assertEquals(HintResult.Status.REFUSED, f.engine.requestHint(OFFICE).status());
        assertFalse(f.engine.pause());
        assertFalse(f.engine.resume());
        assertEquals(before, f.engine.snapshot());
    }

    @Test
    void aFinalAnswerWithNoTimeLeftEndsTheGameAndRestartGivesAFreshOne() {
        Fixture f = new Fixture().withGame();
        f.solveThroughLaboratory();
        f.remaining.set(0);
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.engine.submit(FINAL, OK).status());
        assertEquals(GameState.GAME_OVER, f.engine.snapshot().state());

        f.remaining.set(600);
        f.engine.restart();
        SessionSnapshot s = f.engine.snapshot();
        assertEquals(GameState.RUNNING, s.state());
        assertEquals(1000, s.score());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertEquals(List.of(GameState.RUNNING, GameState.GAME_OVER, GameState.RUNNING), f.listener.states);
    }

    // ------------------------------------------------------------------ restart

    @Test
    void restartKeepsOnlyThePlayerNameAndDropsAllProgress() {
        Fixture f = new Fixture().withGame();
        f.engine.submit(OFFICE, WRONG);
        f.engine.requestHint(OFFICE);
        f.engine.requestHint(OFFICE);
        f.engine.submit(OFFICE, OK);
        f.engine.pause();
        SessionSnapshot old = f.engine.snapshot();

        f.engine.restart();
        SessionSnapshot s = f.engine.snapshot();
        assertEquals("Alice", s.playerName());
        assertEquals(GameState.RUNNING, s.state());
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
        assertEquals(List.of(RoomId.OFFICE), s.unlockedRooms());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.items().isEmpty());
        assertTrue(s.revealedHints().isEmpty());
        assertFalse(f.engine.isUnlocked(RoomId.LIBRARY));

        // The snapshot taken earlier is unchanged.
        assertEquals(GameState.PAUSED, old.state());
        assertEquals(Set.of(OFFICE), old.solvedPuzzleIds());
        assertNotEquals(old, s);
    }

    @Test
    void restartedGameCanBePlayedAgainFromTheBeginning() {
        Fixture f = new Fixture().withGame();
        f.solveThroughLaboratory();
        f.engine.submit(FINAL, OK);
        assertEquals(GameState.VICTORY, f.engine.snapshot().state());

        f.engine.restart();
        assertEquals(SubmissionResult.Status.REFUSED_LOCKED, f.engine.submit(LIBRARY, OK).status());
        assertEquals(SubmissionResult.Status.CORRECT, f.engine.submit(OFFICE, OK).status());
        assertTrue(f.engine.isUnlocked(RoomId.LIBRARY));
        assertEquals(List.of(GameState.RUNNING, GameState.VICTORY, GameState.RUNNING), f.listener.states);
    }

    @Test
    void restartUsesTheLastPlayerNameOfNewGame() {
        Fixture f = new Fixture().withGame();
        f.engine.newGame("Bob");
        f.engine.restart();
        assertEquals("Bob", f.engine.snapshot().playerName());
    }

    @Test
    void restartNotifiesTheListenerExactlyOncePerNewSession() {
        Fixture f = new Fixture().withGame();
        f.engine.restart();
        f.engine.restart();
        assertEquals(List.of(GameState.RUNNING, GameState.RUNNING, GameState.RUNNING), f.listener.states);
    }

    // ------------------------------------------------------------------ snapshots and clock

    @Test
    void snapshotsFromTheEngineAreIndependentCopies() {
        Fixture f = new Fixture().withGame();
        SessionSnapshot before = f.engine.snapshot();
        f.engine.submit(OFFICE, OK);
        assertTrue(before.items().isEmpty());
        assertTrue(before.solvedPuzzleIds().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> f.engine.snapshot().items().clear());
    }

    @Test
    void remainingTimeComesFromTheInjectedSupplier() {
        Fixture f = new Fixture().withGame();
        f.remaining.set(842);
        assertEquals(842, f.engine.snapshot().remainingSeconds());
        f.remaining.set(1201);
        assertThrows(IllegalStateException.class, () -> f.engine.snapshot());
    }

    // ------------------------------------------------------------------ shutdown

    @Test
    void shutdownRefusesEveryLaterOperation() {
        Fixture f = new Fixture().withGame();
        f.engine.shutdown();
        assertThrows(IllegalStateException.class, () -> f.engine.submit(OFFICE, OK));
        assertThrows(IllegalStateException.class, () -> f.engine.requestHint(OFFICE));
        assertThrows(IllegalStateException.class, () -> f.engine.viewHint(OFFICE, 1));
        assertThrows(IllegalStateException.class, () -> f.engine.isUnlocked(RoomId.OFFICE));
        assertThrows(IllegalStateException.class, () -> f.engine.pause());
        assertThrows(IllegalStateException.class, () -> f.engine.resume());
        assertThrows(IllegalStateException.class, () -> f.engine.snapshot());
        assertThrows(IllegalStateException.class, () -> f.engine.restart());
        assertThrows(IllegalStateException.class, () -> f.engine.newGame("Bob"));
    }

    @Test
    void shutdownIsSafeWithoutAGameAndWhenRepeatedAndNotifiesNothing() {
        Fixture f = new Fixture();
        f.engine.shutdown();
        f.engine.shutdown();
        assertTrue(f.listener.states.isEmpty());

        Fixture g = new Fixture().withGame();
        g.engine.shutdown();
        g.engine.shutdown();
        assertEquals(List.of(GameState.RUNNING), g.listener.states);
    }
}
