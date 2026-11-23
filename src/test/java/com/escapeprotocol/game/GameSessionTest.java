package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.player.Player;
import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.rooms.RoomId;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link GameSession} with fake puzzles (the real puzzles arrive in Module 05). Every fake
 * puzzle accepts the answer "ok"; "nope" is a well-formed wrong answer; "" is an invalid format.
 */
class GameSessionTest {

    private static final String OK = "ok";
    private static final String WRONG = "nope";

    /** Records state changes and whether the session lock was held during a callback. */
    private static final class RecordingListener implements GameListener {
        final List<GameState> states = new CopyOnWriteArrayList<>();
        final AtomicBoolean calledWhileLocked = new AtomicBoolean(false);
        volatile ReentrantLock watchedLock;

        @Override
        public void onTick(int remainingSeconds) {
            // Not used in Module 04.
        }

        @Override
        public void onStateChanged(GameState state) {
            ReentrantLock watched = watchedLock;
            if (watched != null && watched.isHeldByCurrentThread()) {
                calledWhileLocked.set(true);
            }
            states.add(state);
        }
    }

    /** One session with its fake puzzles, a controllable clock and a recording listener. */
    private static final class Fixture {
        final AtomicInteger remaining = new AtomicInteger(600);
        final RecordingListener listener = new RecordingListener();
        final Puzzle office = FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE);
        final Puzzle library = FakePuzzle.simple(GameCatalog.LIBRARY_PUZZLE_ID, RoomId.LIBRARY);
        final Puzzle lab = FakePuzzle.simple(GameCatalog.LABORATORY_PUZZLE_ID, RoomId.LABORATORY);
        final Puzzle finalExit = new FakePuzzle(GameCatalog.FINAL_EXIT_PUZZLE_ID, RoomId.FINAL_EXIT, OK,
                List.of("final hint 1", "final hint 2", "final hint 3"),
                Set.of(GameCatalog.OFFICE_KEY_ID, GameCatalog.LIBRARY_CODE_ID, GameCatalog.LABORATORY_CLUE_ID));
        final GameCatalog catalog = GameCatalog.standard(List.of(office, library, lab, finalExit));
        final GameSession session;

        Fixture() {
            this(new HardCodedUnlockRule());
        }

        Fixture(UnlockRule rule) {
            session = new GameSession(new Player("Alice"), catalog, rule, remaining::get, listener);
            try {
                listener.watchedLock = lockOf(session);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        Fixture started() {
            assertTrue(session.start());
            return this;
        }

        SubmissionResult ok(Puzzle puzzle) {
            return session.submit(puzzle, OK);
        }

        /** Solves Office, Library and Laboratory (all with the right answer). */
        Fixture throughLaboratory() {
            started();
            assertEquals(SubmissionResult.Status.CORRECT, ok(office).status());
            assertEquals(SubmissionResult.Status.CORRECT, ok(library).status());
            assertEquals(SubmissionResult.Status.CORRECT, ok(lab).status());
            return this;
        }
    }

    private static ReentrantLock lockOf(GameSession session) throws ReflectiveOperationException {
        Field field = GameSession.class.getDeclaredField("lock");
        field.setAccessible(true);
        return (ReentrantLock) field.get(session);
    }

    // ------------------------------------------------------------------ initial state

    @Test
    void newSessionHasTheInitialValues() {
        Fixture f = new Fixture();
        SessionSnapshot s = f.session.snapshot();
        assertEquals("Alice", s.playerName());
        assertEquals(GameState.NOT_STARTED, s.state());
        assertEquals(1000, s.score());
        assertEquals(GameSession.TIME_LIMIT_SECONDS, s.remainingSeconds());
        assertEquals(List.of(RoomId.OFFICE), s.unlockedRooms());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.items().isEmpty());
        assertTrue(s.revealedHints().isEmpty());
        assertEquals(0, s.wrongAttempts());
        assertTrue(f.listener.states.isEmpty());
    }

    @Test
    void constructorRejectsNullsAndARuleThatDoesNotUnlockTheOffice() {
        Fixture f = new Fixture();
        Player p = new Player("Bob");
        UnlockRule rule = new HardCodedUnlockRule();
        assertThrows(NullPointerException.class, () -> new GameSession(null, f.catalog, rule, () -> 1));
        assertThrows(NullPointerException.class, () -> new GameSession(p, null, rule, () -> 1));
        assertThrows(NullPointerException.class, () -> new GameSession(p, f.catalog, null, () -> 1));
        assertThrows(NullPointerException.class, () -> new GameSession(p, f.catalog, rule, null));
        assertThrows(NullPointerException.class,
                () -> new GameSession(p, f.catalog, rule, () -> 1, null));
        UnlockRule nothingUnlocked = (room, context) -> false;
        assertThrows(IllegalStateException.class,
                () -> new GameSession(p, f.catalog, nothingUnlocked, () -> 1));
    }

    // ------------------------------------------------------------------ state transitions

    @Test
    void startMovesToRunningOnlyOnce() {
        Fixture f = new Fixture();
        assertTrue(f.session.start());
        assertEquals(GameState.RUNNING, f.session.snapshot().state());
        assertFalse(f.session.start());
        assertEquals(GameState.RUNNING, f.session.snapshot().state());
    }

    @Test
    void pauseAndResumeToggleBetweenRunningAndPaused() {
        Fixture f = new Fixture().started();
        assertTrue(f.session.pause());
        assertEquals(GameState.PAUSED, f.session.snapshot().state());
        assertTrue(f.session.resume());
        assertEquals(GameState.RUNNING, f.session.snapshot().state());
    }

    @Test
    void illegalTransitionsAreRejectedAndLeaveTheStateUnchanged() {
        Fixture f = new Fixture();
        assertFalse(f.session.pause());
        assertFalse(f.session.resume());
        assertFalse(f.session.expire());
        assertEquals(GameState.NOT_STARTED, f.session.snapshot().state());

        f.session.start();
        assertFalse(f.session.resume());
        assertEquals(GameState.RUNNING, f.session.snapshot().state());

        f.session.pause();
        assertFalse(f.session.pause());
        assertFalse(f.session.start());
        assertFalse(f.session.expire());
        assertEquals(GameState.PAUSED, f.session.snapshot().state());
    }

    @Test
    void expireMovesRunningToGameOverWithZeroSecondsLeft() {
        Fixture f = new Fixture().started();
        assertTrue(f.session.expire());
        SessionSnapshot s = f.session.snapshot();
        assertEquals(GameState.GAME_OVER, s.state());
        assertEquals(0, s.remainingSeconds());
    }

    @Test
    void gameOverIsTerminal() {
        Fixture f = new Fixture().started();
        f.session.expire();
        assertFalse(f.session.start());
        assertFalse(f.session.pause());
        assertFalse(f.session.resume());
        assertFalse(f.session.expire());
        assertEquals(GameState.GAME_OVER, f.session.snapshot().state());
    }

    @Test
    void victoryIsTerminal() {
        Fixture f = new Fixture().throughLaboratory();
        assertEquals(SubmissionResult.Status.VICTORY, f.ok(f.finalExit).status());
        assertFalse(f.session.start());
        assertFalse(f.session.pause());
        assertFalse(f.session.resume());
        assertFalse(f.session.expire());
        assertEquals(GameState.VICTORY, f.session.snapshot().state());
    }

    // ------------------------------------------------------------------ submissions: refusals

    @Test
    void submissionsAreRefusedUnlessTheGameIsRunning() {
        Fixture f = new Fixture();
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.ok(f.office).status()); // not started

        f.session.start();
        f.session.pause();
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.ok(f.office).status()); // paused

        f.session.resume();
        f.session.expire();
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.ok(f.office).status()); // game over

        SessionSnapshot s = f.session.snapshot();
        assertEquals(1000, s.score());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.items().isEmpty());
    }

    @Test
    void lockedRoomSubmissionIsRefusedWithoutPenaltyOrAttempt() {
        Fixture f = new Fixture().started();
        for (Puzzle locked : List.of(f.library, f.lab, f.finalExit)) {
            assertEquals(SubmissionResult.Status.REFUSED_LOCKED, f.session.submit(locked, OK).status());
            assertEquals(SubmissionResult.Status.REFUSED_LOCKED, f.session.submit(locked, WRONG).status());
        }
        SessionSnapshot s = f.session.snapshot();
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertTrue(s.items().isEmpty());
    }

    @Test
    void invalidFormatCostsNothingAndIsNotAnAttempt() {
        Fixture f = new Fixture().started();
        assertEquals(SubmissionResult.Status.INVALID_FORMAT, f.session.submit(f.office, "").status());
        assertEquals(SubmissionResult.Status.INVALID_FORMAT, f.session.submit(f.office, null).status());
        SessionSnapshot s = f.session.snapshot();
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
    }

    @Test
    void incorrectAnswerIsAnAttemptAndCostsTwentyFive() {
        Fixture f = new Fixture().started();
        assertEquals(SubmissionResult.Status.INCORRECT, f.session.submit(f.office, WRONG).status());
        assertEquals(975, f.session.snapshot().score());
        f.session.submit(f.office, WRONG);
        SessionSnapshot s = f.session.snapshot();
        assertEquals(950, s.score());
        assertEquals(2, s.wrongAttempts());
        assertTrue(s.solvedPuzzleIds().isEmpty());
        assertEquals(GameState.RUNNING, s.state());
    }

    @Test
    void scoreNeverGoesBelowZero() {
        Fixture f = new Fixture().started();
        for (int i = 0; i < 41; i++) {
            f.session.submit(f.office, WRONG);
        }
        SessionSnapshot s = f.session.snapshot();
        assertEquals(0, s.score());
        assertEquals(41, s.wrongAttempts());
    }

    // ------------------------------------------------------------------ submissions: progress

    @Test
    void correctOfficeAnswerSolvesItGrantsTheKeyAndUnlocksOnlyTheLibrary() {
        Fixture f = new Fixture().started();
        SubmissionResult result = f.ok(f.office);
        assertEquals(SubmissionResult.Status.CORRECT, result.status());
        assertTrue(result.message().contains("Office Key"));

        SessionSnapshot s = f.session.snapshot();
        assertEquals(Set.of("office_year"), s.solvedPuzzleIds());
        assertEquals(List.of("office_key"), s.items().stream().map(Item::id).toList());
        assertEquals(List.of(RoomId.OFFICE, RoomId.LIBRARY), s.unlockedRooms());
        assertEquals(1000, s.score());
        assertTrue(f.session.isUnlocked(RoomId.LIBRARY));
        assertFalse(f.session.isUnlocked(RoomId.LABORATORY));
        assertFalse(f.session.isUnlocked(RoomId.FINAL_EXIT));
    }

    @Test
    void resubmittingASolvedPuzzleGivesNoDuplicateItemNoPenaltyAndNoNewAttempt() {
        Fixture f = new Fixture().started();
        f.ok(f.office);
        assertEquals(SubmissionResult.Status.ALREADY_SOLVED, f.ok(f.office).status());
        assertEquals(SubmissionResult.Status.ALREADY_SOLVED, f.session.submit(f.office, WRONG).status());

        SessionSnapshot s = f.session.snapshot();
        assertEquals(1, s.items().size());
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
    }

    @Test
    void roomsUnlockOneAtATimeThroughTheWholeGame() {
        Fixture f = new Fixture().started();
        assertEquals(List.of(RoomId.OFFICE), f.session.snapshot().unlockedRooms());
        f.ok(f.office);
        assertEquals(List.of(RoomId.OFFICE, RoomId.LIBRARY), f.session.snapshot().unlockedRooms());
        f.ok(f.library);
        assertEquals(List.of(RoomId.OFFICE, RoomId.LIBRARY, RoomId.LABORATORY),
                f.session.snapshot().unlockedRooms());
        f.ok(f.lab);
        assertEquals(RoomId.inOrder(), f.session.snapshot().unlockedRooms());
        assertEquals(List.of("office_key", "code_7394", "lab_clue_30"),
                f.session.snapshot().items().stream().map(Item::id).toList());
    }

    @Test
    void finalExitNamesEveryMissingClueKindWhenNothingWasCollected() {
        // A rule that opens every room lets us reach the Final Exit without the items.
        Fixture f = new Fixture((room, context) -> true).started();
        SubmissionResult result = f.session.submit(f.finalExit, OK);
        assertEquals(SubmissionResult.Status.REFUSED_MISSING_CLUES, result.status());
        assertTrue(result.message().contains("KEY"));
        assertTrue(result.message().contains("CODE"));
        assertTrue(result.message().contains("CLUE"));
        SessionSnapshot s = f.session.snapshot();
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
        assertEquals(GameState.RUNNING, s.state());
    }

    @Test
    void finalExitNamesOnlyTheMissingKinds() {
        Fixture f = new Fixture((room, context) -> true).started();
        f.ok(f.office);
        f.ok(f.library);
        SubmissionResult result = f.session.submit(f.finalExit, OK);
        assertEquals(SubmissionResult.Status.REFUSED_MISSING_CLUES, result.status());
        assertTrue(result.message().contains("CLUE"));
        assertFalse(result.message().contains("KEY"));
        assertFalse(result.message().contains("CODE"));
    }

    // ------------------------------------------------------------------ victory and game over

    @Test
    void correctFinalAnswerWinsAndAddsTwoPointsPerRemainingSecond() {
        Fixture f = new Fixture().throughLaboratory();
        f.remaining.set(600);
        SubmissionResult result = f.ok(f.finalExit);

        assertEquals(SubmissionResult.Status.VICTORY, result.status());
        SessionSnapshot s = f.session.snapshot();
        assertEquals(GameState.VICTORY, s.state());
        assertEquals(1000 + 2 * 600, s.score());
        assertEquals(600, s.remainingSeconds());
        assertTrue(s.solvedPuzzleIds().contains("final_exit"));
        assertEquals(3, s.items().size());
        assertEquals(List.of(GameState.RUNNING, GameState.VICTORY), f.listener.states);
    }

    @Test
    void victoryScoreIncludesThePenaltiesAlreadyApplied() {
        Fixture f = new Fixture().started();
        f.session.submit(f.office, WRONG);        // 975
        f.session.revealHint(f.office);           // 925
        f.ok(f.office);
        f.ok(f.library);
        f.ok(f.lab);
        f.remaining.set(300);
        f.ok(f.finalExit);
        assertEquals(925 + 2 * 300, f.session.snapshot().score());
    }

    @Test
    void remainingTimeIsFrozenAfterTheWin() {
        Fixture f = new Fixture().throughLaboratory();
        f.remaining.set(480);
        f.ok(f.finalExit);
        f.remaining.set(5);
        assertEquals(480, f.session.snapshot().remainingSeconds());
    }

    @Test
    void wrongFinalAnswerCostsThePenaltyAndTheGameContinues() {
        Fixture f = new Fixture().throughLaboratory();
        assertEquals(SubmissionResult.Status.INCORRECT, f.session.submit(f.finalExit, WRONG).status());
        SessionSnapshot s = f.session.snapshot();
        assertEquals(975, s.score());
        assertEquals(GameState.RUNNING, s.state());
        assertFalse(s.solvedPuzzleIds().contains("final_exit"));
    }

    @Test
    void correctFinalAnswerWhenNoTimeIsLeftEndsInGameOver() {
        Fixture f = new Fixture().throughLaboratory();
        f.remaining.set(0);
        SubmissionResult result = f.ok(f.finalExit);

        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, result.status());
        SessionSnapshot s = f.session.snapshot();
        assertEquals(GameState.GAME_OVER, s.state());
        assertFalse(s.solvedPuzzleIds().contains("final_exit"));
        assertEquals(1000, s.score());
        assertEquals(0, s.remainingSeconds());
        assertEquals(List.of(GameState.RUNNING, GameState.GAME_OVER), f.listener.states);
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.ok(f.finalExit).status());
    }

    @Test
    void nothingCanChangeAfterVictory() {
        Fixture f = new Fixture().throughLaboratory();
        f.ok(f.finalExit);
        SessionSnapshot before = f.session.snapshot();
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, f.session.submit(f.office, WRONG).status());
        assertEquals(HintResult.Status.REFUSED, f.session.revealHint(f.office).status());
        assertFalse(f.session.expire());
        assertEquals(before, f.session.snapshot());
    }

    // ------------------------------------------------------------------ hints

    @Test
    void hintsAreRefusedForFreeWhenNotRunningOrWhenTheRoomIsLocked() {
        Fixture f = new Fixture();
        assertEquals(HintResult.Status.REFUSED, f.session.revealHint(f.office).status()); // not started
        f.session.start();
        assertEquals(HintResult.Status.REFUSED, f.session.revealHint(f.library).status()); // locked
        assertEquals(HintResult.Status.REFUSED, f.session.viewHint(f.library, 1).status());
        f.session.pause();
        assertEquals(HintResult.Status.REFUSED, f.session.revealHint(f.office).status()); // paused
        assertEquals(1000, f.session.snapshot().score());
        assertTrue(f.session.snapshot().revealedHints().isEmpty());
    }

    @Test
    void eachNewHintCostsFiftyAndThereAreOnlyThree() {
        Fixture f = new Fixture().started();
        HintResult first = f.session.revealHint(f.office);
        assertEquals(HintResult.Status.REVEALED_NEW, first.status());
        assertEquals(1, first.level());
        assertEquals("office_year hint 1", first.text());
        assertEquals(950, f.session.snapshot().score());

        f.session.revealHint(f.office);
        f.session.revealHint(f.office);
        assertEquals(850, f.session.snapshot().score());

        HintResult none = f.session.revealHint(f.office);
        assertEquals(HintResult.Status.NO_MORE_HINTS, none.status());
        assertEquals(850, f.session.snapshot().score());
        assertEquals(3, f.session.snapshot().hintsRevealed("office_year"));
    }

    @Test
    void viewingARevealedHintIsFreeAndAnUnrevealedOneIsRefused() {
        Fixture f = new Fixture().started();
        f.session.revealHint(f.office);
        f.session.revealHint(f.office);
        assertEquals(900, f.session.snapshot().score());

        HintResult again = f.session.viewHint(f.office, 1);
        assertEquals(HintResult.Status.VIEWED_AGAIN, again.status());
        assertEquals("office_year hint 1", again.text());
        assertEquals(HintResult.Status.VIEWED_AGAIN, f.session.viewHint(f.office, 2).status());
        assertEquals(HintResult.Status.REFUSED, f.session.viewHint(f.office, 3).status());
        assertEquals(HintResult.Status.REFUSED, f.session.viewHint(f.office, 0).status());
        assertEquals(900, f.session.snapshot().score());
    }

    @Test
    void hintCountsAreTrackedPerPuzzle() {
        Fixture f = new Fixture().started();
        f.session.revealHint(f.office);
        f.session.revealHint(f.office);
        f.ok(f.office);
        f.session.revealHint(f.library);
        SessionSnapshot s = f.session.snapshot();
        assertEquals(2, s.hintsRevealed("office_year"));
        assertEquals(1, s.hintsRevealed("library_order"));
        assertEquals(0, s.hintsRevealed("lab_sequence"));
        assertEquals(1000 - 3 * 50, s.score());
    }

    @Test
    void newHintOnASolvedPuzzleIsRefusedButViewingRevealedHintsStaysFree() {
        Fixture f = new Fixture().started();
        f.session.revealHint(f.office);
        f.ok(f.office);
        assertEquals(950, f.session.snapshot().score());

        assertEquals(HintResult.Status.REFUSED, f.session.revealHint(f.office).status());
        assertEquals(HintResult.Status.VIEWED_AGAIN, f.session.viewHint(f.office, 1).status());
        assertEquals(950, f.session.snapshot().score());
        assertEquals(1, f.session.snapshot().hintsRevealed("office_year"));
    }

    @Test
    void hintsNeverTakeTheScoreBelowZero() {
        Fixture f = new Fixture().started();
        for (int i = 0; i < 40; i++) {
            f.session.submit(f.office, WRONG);
        }
        assertEquals(0, f.session.snapshot().score());
        assertEquals(HintResult.Status.REVEALED_NEW, f.session.revealHint(f.office).status());
        assertEquals(0, f.session.snapshot().score());
    }

    // ------------------------------------------------------------------ puzzle identity checks

    @Test
    void puzzlesThatAreNotThePuzzlesOfTheCatalogAreRejected() {
        Fixture f = new Fixture().started();
        Puzzle unknownId = FakePuzzle.simple("not_in_catalog", RoomId.OFFICE);
        Puzzle sameIdOtherInstance = FakePuzzle.simple(GameCatalog.OFFICE_PUZZLE_ID, RoomId.OFFICE);
        for (Puzzle bad : List.of(unknownId, sameIdOtherInstance)) {
            assertThrows(IllegalArgumentException.class, () -> f.session.submit(bad, OK));
            assertThrows(IllegalArgumentException.class, () -> f.session.revealHint(bad));
            assertThrows(IllegalArgumentException.class, () -> f.session.viewHint(bad, 1));
        }
        assertThrows(NullPointerException.class, () -> f.session.submit(null, OK));
        assertThrows(NullPointerException.class, () -> f.session.revealHint(null));
        assertThrows(NullPointerException.class, () -> f.session.viewHint(null, 1));
        assertThrows(NullPointerException.class, () -> f.session.isUnlocked(null));
        assertEquals(1000, f.session.snapshot().score());
    }

    // ------------------------------------------------------------------ snapshots

    @Test
    void snapshotCollectionsCannotBeModified() {
        Fixture f = new Fixture().started();
        f.ok(f.office);
        f.session.revealHint(f.library);
        SessionSnapshot s = f.session.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> s.items().clear());
        assertThrows(UnsupportedOperationException.class, () -> s.unlockedRooms().add(RoomId.FINAL_EXIT));
        assertThrows(UnsupportedOperationException.class, () -> s.solvedPuzzleIds().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> s.revealedHints().put("x", 1));
    }

    @Test
    void snapshotIsAPointInTimeCopy() {
        Fixture f = new Fixture().started();
        SessionSnapshot before = f.session.snapshot();
        f.ok(f.office);
        f.session.submit(f.library, WRONG);
        assertEquals(1000, before.score());
        assertTrue(before.items().isEmpty());
        assertTrue(before.solvedPuzzleIds().isEmpty());
        assertEquals(List.of(RoomId.OFFICE), before.unlockedRooms());
        assertNotEquals(before, f.session.snapshot());
    }

    @Test
    void snapshotReadsTheRemainingTimeWhileRunningAndPausedAndRejectsOutOfRangeValues() {
        Fixture f = new Fixture().started();
        f.remaining.set(900);
        assertEquals(900, f.session.snapshot().remainingSeconds());
        f.session.pause();
        f.remaining.set(850);
        assertEquals(850, f.session.snapshot().remainingSeconds());
        f.remaining.set(1201);
        assertThrows(IllegalStateException.class, () -> f.session.snapshot());
        f.remaining.set(-1);
        assertThrows(IllegalStateException.class, () -> f.session.snapshot());
    }

    // ------------------------------------------------------------------ listener

    @Test
    void listenerReceivesOnlyRealStateChanges() {
        Fixture f = new Fixture();
        f.session.pause();   // rejected
        f.session.start();
        f.session.start();   // rejected
        f.session.pause();
        f.session.resume();
        f.session.resume();  // rejected
        f.session.expire();
        f.session.expire();  // rejected
        assertEquals(List.of(GameState.RUNNING, GameState.PAUSED, GameState.RUNNING, GameState.GAME_OVER),
                f.listener.states);
    }

    @Test
    void listenerIsNeverCalledWhileTheSessionLockIsHeld() {
        Fixture f = new Fixture();
        f.session.start();
        f.session.pause();
        f.session.resume();
        f.ok(f.office);
        f.ok(f.library);
        f.ok(f.lab);
        f.ok(f.finalExit);                       // VICTORY notification

        Fixture g = new Fixture().started();
        g.session.expire();                      // GAME_OVER notification

        Fixture h = new Fixture().throughLaboratory();
        h.remaining.set(0);
        h.ok(h.finalExit);                       // too-late GAME_OVER notification

        assertFalse(f.listener.calledWhileLocked.get());
        assertFalse(g.listener.calledWhileLocked.get());
        assertFalse(h.listener.calledWhileLocked.get());
        assertEquals(List.of(GameState.RUNNING, GameState.PAUSED, GameState.RUNNING, GameState.VICTORY),
                f.listener.states);
    }

    // ------------------------------------------------------------------ concurrency

    private static <T> List<T> runAll(List<Callable<T>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        try {
            List<T> results = new ArrayList<>();
            for (Future<T> future : pool.invokeAll(tasks, 30, TimeUnit.SECONDS)) {
                results.add(future.get(1, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void concurrentWrongAnswersAreAllCountedExactlyOnce() throws Exception {
        Fixture f = new Fixture().started();
        int threads = 8;
        int perThread = 4;
        CyclicBarrier gate = new CyclicBarrier(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            tasks.add(() -> {
                gate.await(10, TimeUnit.SECONDS);
                for (int i = 0; i < perThread; i++) {
                    f.session.submit(f.office, WRONG);
                }
                return true;
            });
        }
        runAll(tasks);
        SessionSnapshot s = f.session.snapshot();
        assertEquals(threads * perThread, s.wrongAttempts());
        assertEquals(1000 - 25 * threads * perThread, s.score());
    }

    @Test
    void concurrentHintRequestsRevealEachHintExactlyOnce() throws Exception {
        Fixture f = new Fixture().started();
        int threads = 6;
        CyclicBarrier gate = new CyclicBarrier(threads);
        List<Callable<HintResult.Status>> tasks = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            tasks.add(() -> {
                gate.await(10, TimeUnit.SECONDS);
                return f.session.revealHint(f.office).status();
            });
        }
        List<HintResult.Status> results = runAll(tasks);
        assertEquals(3L, results.stream().filter(s -> s == HintResult.Status.REVEALED_NEW).count());
        assertEquals(3L, results.stream().filter(s -> s == HintResult.Status.NO_MORE_HINTS).count());
        assertEquals(850, f.session.snapshot().score());
        assertEquals(3, f.session.snapshot().hintsRevealed("office_year"));
    }

    @Test
    void finalAnswerAndExpiryRaceAlwaysEndInExactlyOneTerminalState() throws Exception {
        for (int round = 0; round < 100; round++) {
            Fixture f = new Fixture().throughLaboratory();
            CyclicBarrier gate = new CyclicBarrier(2);
            Callable<SubmissionResult> answer = () -> {
                gate.await(10, TimeUnit.SECONDS);
                return f.ok(f.finalExit);
            };
            Callable<Boolean> expire = () -> {
                gate.await(10, TimeUnit.SECONDS);
                return f.session.expire();
            };
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                Future<SubmissionResult> answered = pool.submit(answer);
                Future<Boolean> expired = pool.submit(expire);
                SubmissionResult result = answered.get(10, TimeUnit.SECONDS);
                boolean expireWon = expired.get(10, TimeUnit.SECONDS);
                SessionSnapshot s = f.session.snapshot();

                boolean won = result.status() == SubmissionResult.Status.VICTORY;
                assertTrue(won != expireWon, "exactly one of win and expiry must succeed");
                if (won) {
                    assertEquals(GameState.VICTORY, s.state());
                    assertTrue(s.solvedPuzzleIds().contains("final_exit"));
                } else {
                    assertEquals(GameState.GAME_OVER, s.state());
                    assertFalse(s.solvedPuzzleIds().contains("final_exit"));
                    assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, result.status());
                }
            } finally {
                pool.shutdownNow();
            }
        }
    }

    @Test
    void isUnlockedAnswersFromTheCurrentState() {
        Fixture f = new Fixture();
        assertTrue(f.session.isUnlocked(RoomId.OFFICE));
        assertFalse(f.session.isUnlocked(RoomId.LIBRARY));
        f.started();
        f.ok(f.office);
        assertTrue(f.session.isUnlocked(RoomId.LIBRARY));
    }
}
