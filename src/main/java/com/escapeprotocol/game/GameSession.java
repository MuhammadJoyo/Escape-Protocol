package com.escapeprotocol.game;

import com.escapeprotocol.inventory.Inventory;
import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.inventory.ItemKind;
import com.escapeprotocol.player.Player;
import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.puzzles.PuzzleResult;
import com.escapeprotocol.rooms.Room;
import com.escapeprotocol.rooms.RoomId;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

/**
 * One play-through of the game: the player, the state, the score, the inventory, the solved
 * puzzles, the unlocked rooms and the revealed hints.
 *
 * <p><b>Abstraction function:</b> AF = one game in progress or finished, for {@code player},
 * in the state {@code state}, where the player has collected {@code inventory}, solved
 * {@code solvedPuzzleIds}, may enter {@code unlockedRooms}, and has {@code score} points.
 *
 * <p><b>Representation invariant (RI)</b>, checked by {@link #checkRep()}:
 * <ul>
 *   <li>{@code state} is non-null and {@code score >= 0};</li>
 *   <li>OFFICE is unlocked;</li>
 *   <li>every solved puzzle id is a puzzle of the catalog;</li>
 *   <li>every inventory item is the reward of a completed room;</li>
 *   <li>every unlocked room satisfies the {@link UnlockRule} (checked with the current items
 *       and solved puzzles; this is true for {@link HardCodedUnlockRule} because items are never
 *       removed. A future rule that uses "not has(...)" must revisit this clause);</li>
 *   <li>a finished state (VICTORY, GAME_OVER) never changes (enforced by
 *       {@link GameState#canMoveTo}).</li>
 * </ul>
 *
 * <p><b>Thread safety, the lock guard (CON-01, CON-02):</b> a single {@link ReentrantLock}
 * named {@code lock} guards <em>all</em> mutable state: {@code state}, {@code score},
 * {@code wrongAttempts}, {@code frozenRemainingSeconds}, {@code inventory} (which is not
 * thread-safe by itself and has no lock of its own), {@code solvedPuzzleIds},
 * {@code unlockedRooms} and {@code revealedHints}. Every public method that reads or changes them
 * takes the lock. Rules that keep the lock safe:
 * <ul>
 *   <li>only the lock of this class is used; there is no second lock, so lock-order deadlocks
 *       cannot happen;</li>
 *   <li>no file or network I/O happens under the lock;</li>
 *   <li>the {@link GameListener} is called only <em>after</em> the lock is released;</li>
 *   <li>the {@code remainingSeconds} supplier and {@link Puzzle#submit} are called under the lock,
 *       so they must be fast, non-blocking, and must not call back into this session;</li>
 *   <li>results and snapshots are immutable copies; no mutable internal object is returned.</li>
 * </ul>
 * Because the first caller to take the lock wins, a correct final answer and {@link #expire()}
 * cannot both succeed: the second caller finds a finished state (FR-TS-08).
 *
 * <p>The timer is not part of this class (Module 07). The caller of {@link #expire()} is the
 * timer, and the remaining time is read through the injected supplier.
 */
public final class GameSession {

    /** The length of a game in seconds (20:00). */
    public static final int TIME_LIMIT_SECONDS = 1200;

    private static final GameListener NO_LISTENER = new GameListener() {
        @Override
        public void onTick(int remainingSeconds) {
            // Intentionally empty.
        }

        @Override
        public void onStateChanged(GameState state) {
            // Intentionally empty.
        }
    };

    private final Player player;
    private final GameCatalog catalog;
    private final UnlockRule unlockRule;
    private final IntSupplier remainingSecondsSupplier;
    private final GameListener listener;
    private final ScoreCalculator scoreCalculator = new ScoreCalculator();
    private final HintService hintService = new HintService();

    private final ReentrantLock lock = new ReentrantLock();

    // ---- Everything below is guarded by 'lock'. ----
    private GameState state = GameState.NOT_STARTED;
    private int score = ScoreCalculator.INITIAL_SCORE;
    private int wrongAttempts;
    private int frozenRemainingSeconds = TIME_LIMIT_SECONDS;
    private final Inventory inventory = new Inventory();
    private final Set<String> solvedPuzzleIds = new LinkedHashSet<>();
    private final Set<RoomId> unlockedRooms = EnumSet.noneOf(RoomId.class);
    private final Map<String, Integer> revealedHints = new HashMap<>();

    /** The outcome of a locked operation plus the state to announce after unlocking, if any. */
    private record Update<T>(T result, GameState newState) {
    }

    /**
     * Creates a session in the NOT_STARTED state without a listener.
     *
     * @see #GameSession(Player, GameCatalog, UnlockRule, IntSupplier, GameListener)
     */
    public GameSession(Player player, GameCatalog catalog, UnlockRule unlockRule,
                       IntSupplier remainingSeconds) {
        this(player, catalog, unlockRule, remainingSeconds, NO_LISTENER);
    }

    /**
     * Creates a session in the NOT_STARTED state with score 1000, an empty inventory, and the
     * rooms that the rule unlocks for an empty player (only OFFICE).
     *
     * @param player the player; must not be {@code null}
     * @param catalog the rooms, rewards and puzzles; must not be {@code null}
     * @param unlockRule decides which rooms are unlocked; must not be {@code null}
     * @param remainingSeconds supplies the seconds left (0 to {@link #TIME_LIMIT_SECONDS}) while
     *        the game is RUNNING or PAUSED. It is called under the session lock, so it must be
     *        fast and non-blocking. Module 07 passes the timer here.
     * @param listener receives state changes after the lock is released; must not throw
     * @throws NullPointerException if an argument is {@code null}
     * @throws IllegalStateException if the rule does not unlock OFFICE
     */
    public GameSession(Player player, GameCatalog catalog, UnlockRule unlockRule,
                       IntSupplier remainingSeconds, GameListener listener) {
        this.player = Objects.requireNonNull(player, "player must not be null");
        this.catalog = Objects.requireNonNull(catalog, "catalog must not be null");
        this.unlockRule = Objects.requireNonNull(unlockRule, "unlockRule must not be null");
        this.remainingSecondsSupplier =
                Objects.requireNonNull(remainingSeconds, "remainingSeconds must not be null");
        this.listener = Objects.requireNonNull(listener, "listener must not be null");
        lock.lock();
        try {
            refreshUnlockedRooms();
            checkRep();
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------ state changes

    /**
     * Starts the game: NOT_STARTED to RUNNING.
     *
     * @return {@code true} if the state changed; {@code false} (state unchanged) in any other state
     */
    public boolean start() {
        return moveTo(GameState.NOT_STARTED, GameState.RUNNING);
    }

    /**
     * Pauses the game: RUNNING to PAUSED. Stopping the timer is the caller's job (Module 07).
     *
     * @return {@code true} if the state changed; {@code false} (state unchanged) otherwise
     */
    public boolean pause() {
        return moveTo(GameState.RUNNING, GameState.PAUSED);
    }

    /**
     * Resumes the game: PAUSED to RUNNING.
     *
     * @return {@code true} if the state changed; {@code false} (state unchanged) otherwise
     */
    public boolean resume() {
        return moveTo(GameState.PAUSED, GameState.RUNNING);
    }

    /**
     * The time is up: RUNNING to GAME_OVER. Called by the timer (Module 07). Later submissions are
     * refused (FR-GM-06).
     *
     * @return {@code true} if the state changed; {@code false} (state unchanged) in any other
     *         state, for example when the game was already won
     */
    public boolean expire() {
        return moveTo(GameState.RUNNING, GameState.GAME_OVER);
    }

    private boolean moveTo(GameState required, GameState next) {
        boolean moved;
        lock.lock();
        try {
            moved = state == required && state.canMoveTo(next);
            if (moved) {
                state = next;
                if (next == GameState.GAME_OVER) {
                    frozenRemainingSeconds = 0;
                }
                checkRep();
            }
        } finally {
            lock.unlock();
        }
        if (moved) {
            listener.onStateChanged(next);
        }
        return moved;
    }

    // ------------------------------------------------------------------ puzzles

    /**
     * Submits an answer. The checks run in this order, all under the lock (Architecture §9):
     * RUNNING, room unlocked, not already solved, required items owned, then the puzzle decides.
     *
     * <ul>
     *   <li>INVALID_FORMAT costs nothing and is not counted as an attempt;</li>
     *   <li>INCORRECT counts as an attempt and costs 25 points (never below 0);</li>
     *   <li>CORRECT marks the puzzle solved; when all required puzzles of the room are solved the
     *       room's reward item is added and the unlock rule is re-evaluated;</li>
     *   <li>a correct answer in FINAL_EXIT ends the game with VICTORY and the final score
     *       (score plus 2 points per remaining second). If no time remains at that moment, the
     *       game is GAME_OVER instead and the answer is refused.</li>
     * </ul>
     *
     * @param puzzle a puzzle of this session's catalog; must not be {@code null}
     * @param answer the player's raw answer; may be {@code null}
     * @return the outcome; never {@code null}
     * @throws NullPointerException if {@code puzzle} is {@code null}
     * @throws IllegalArgumentException if {@code puzzle} is not the catalog's puzzle with its id
     * @throws IllegalStateException if the remaining-time supplier returns a value outside
     *         0 to {@link #TIME_LIMIT_SECONDS}
     */
    public SubmissionResult submit(Puzzle puzzle, String answer) {
        requireCatalogPuzzle(puzzle);
        Update<SubmissionResult> update;
        lock.lock();
        try {
            update = submitLocked(puzzle, answer);
        } finally {
            lock.unlock();
        }
        announce(update.newState());
        return update.result();
    }

    private Update<SubmissionResult> submitLocked(Puzzle puzzle, String answer) {
        if (state != GameState.RUNNING) {
            return unchanged(SubmissionResult.Status.REFUSED_NOT_RUNNING, "The game is not running.");
        }
        RoomId roomId = puzzle.roomId();
        if (!unlockedRooms.contains(roomId)) {
            return unchanged(SubmissionResult.Status.REFUSED_LOCKED, "That room is still locked.");
        }
        if (solvedPuzzleIds.contains(puzzle.id())) {
            return unchanged(SubmissionResult.Status.ALREADY_SOLVED, "You already solved this puzzle.");
        }
        List<String> missingIds = puzzle.requiredItemIds().stream()
                .filter(itemId -> !inventory.contains(itemId))
                .toList();
        if (!missingIds.isEmpty()) {
            return unchanged(SubmissionResult.Status.REFUSED_MISSING_CLUES, missingMessage(missingIds));
        }

        PuzzleResult checked = puzzle.submit(answer);
        if (checked == null) {
            throw new IllegalStateException("Puzzle " + puzzle.id() + " returned null");
        }
        switch (checked.status()) {
            case INVALID_FORMAT:
                return unchanged(SubmissionResult.Status.INVALID_FORMAT, checked.feedback());
            case INCORRECT:
                wrongAttempts++;
                score = scoreCalculator.afterWrongAttempt(score);
                checkRep();
                return unchanged(SubmissionResult.Status.INCORRECT, checked.feedback());
            case CORRECT:
                return acceptCorrect(puzzle, roomId, checked);
            default:
                throw new IllegalStateException("Unknown puzzle status " + checked.status());
        }
    }

    private Update<SubmissionResult> acceptCorrect(Puzzle puzzle, RoomId roomId, PuzzleResult checked) {
        boolean finalRoom = roomId == RoomId.FINAL_EXIT;
        int remaining = 0;
        if (finalRoom) {
            remaining = readRemainingSeconds();
            if (remaining == 0) {
                // Too late: the clock is already at zero, so the outcome is GAME_OVER (Architecture 10.2).
                state = GameState.GAME_OVER;
                frozenRemainingSeconds = 0;
                checkRep();
                return new Update<>(new SubmissionResult(SubmissionResult.Status.REFUSED_NOT_RUNNING,
                        "Time ran out before your answer was accepted. Game over."), GameState.GAME_OVER);
            }
        }

        solvedPuzzleIds.add(puzzle.id());
        Room room = catalog.room(roomId);
        boolean roomComplete = solvedPuzzleIds.containsAll(room.requiredPuzzleIds());

        Item reward = null;
        if (roomComplete) {
            reward = catalog.completionItem(roomId).orElse(null);
            if (reward != null) {
                inventory.add(reward);
            }
            refreshUnlockedRooms();
        }

        if (finalRoom && roomComplete) {
            score = scoreCalculator.victoryScore(score, remaining);
            frozenRemainingSeconds = remaining;
            state = GameState.VICTORY;
            checkRep();
            return new Update<>(new SubmissionResult(SubmissionResult.Status.VICTORY,
                    checked.feedback() + " You escaped! Final score: " + score + "."), GameState.VICTORY);
        }

        checkRep();
        String message = reward == null
                ? checked.feedback()
                : checked.feedback() + " You found: " + reward.name() + ".";
        return unchanged(SubmissionResult.Status.CORRECT, message);
    }

    // ------------------------------------------------------------------ hints

    /**
     * Reveals the next hint of a puzzle. A newly revealed hint costs 50 points (never below 0).
     * Refused, at no cost, when the game is not RUNNING, the room is locked, or the puzzle is
     * already solved. When all (at most three) hints are revealed the result is NO_MORE_HINTS,
     * also at no cost.
     *
     * @param puzzle a puzzle of this session's catalog; must not be {@code null}
     * @return the outcome; never {@code null}
     * @throws NullPointerException if {@code puzzle} is {@code null}
     * @throws IllegalArgumentException if {@code puzzle} is not the catalog's puzzle with its id
     */
    public HintResult revealHint(Puzzle puzzle) {
        requireCatalogPuzzle(puzzle);
        lock.lock();
        try {
            HintResult refusal = hintRefusal(puzzle);
            if (refusal != null) {
                return refusal;
            }
            if (solvedPuzzleIds.contains(puzzle.id())) {
                return refused("You already solved this puzzle, so no new hint is needed.");
            }
            int revealed = revealedHints.getOrDefault(puzzle.id(), 0);
            HintResult result = hintService.request(puzzle.hints(), revealed);
            if (result.status() == HintResult.Status.REVEALED_NEW) {
                revealedHints.put(puzzle.id(), result.level());
                score = scoreCalculator.afterHint(score);
                checkRep();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Shows an already revealed hint again, at no cost. Allowed for solved puzzles. Refused when
     * the game is not RUNNING, the room is locked, or the level is not revealed yet.
     *
     * @param puzzle a puzzle of this session's catalog; must not be {@code null}
     * @param level the 1-based hint level
     * @return the outcome; never {@code null}
     * @throws NullPointerException if {@code puzzle} is {@code null}
     * @throws IllegalArgumentException if {@code puzzle} is not the catalog's puzzle with its id
     */
    public HintResult viewHint(Puzzle puzzle, int level) {
        requireCatalogPuzzle(puzzle);
        lock.lock();
        try {
            HintResult refusal = hintRefusal(puzzle);
            if (refusal != null) {
                return refusal;
            }
            int revealed = revealedHints.getOrDefault(puzzle.id(), 0);
            return hintService.view(puzzle.hints(), revealed, level);
        } finally {
            lock.unlock();
        }
    }

    /** Must be called with the lock held. Returns a refusal, or {@code null} if hints are allowed. */
    private HintResult hintRefusal(Puzzle puzzle) {
        if (state != GameState.RUNNING) {
            return refused("The game is not running.");
        }
        if (!unlockedRooms.contains(puzzle.roomId())) {
            return refused("That room is still locked.");
        }
        return null;
    }

    // ------------------------------------------------------------------ queries

    /**
     * @param room a room; must not be {@code null}
     * @return {@code true} if the room is currently unlocked
     * @throws NullPointerException if {@code room} is {@code null}
     */
    public boolean isUnlocked(RoomId room) {
        Objects.requireNonNull(room, "room must not be null");
        lock.lock();
        try {
            return unlockedRooms.contains(room);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Copies the current state into an immutable snapshot. The remaining time is
     * {@link #TIME_LIMIT_SECONDS} before the start, the supplier's value while RUNNING or PAUSED,
     * the value at the moment of the win after VICTORY, and 0 after GAME_OVER.
     *
     * @return an immutable snapshot; never {@code null}
     * @throws IllegalStateException if the remaining-time supplier returns a value outside
     *         0 to {@link #TIME_LIMIT_SECONDS}
     */
    public SessionSnapshot snapshot() {
        lock.lock();
        try {
            int remaining = (state == GameState.RUNNING || state == GameState.PAUSED)
                    ? readRemainingSeconds()
                    : frozenRemainingSeconds;
            List<RoomId> unlocked = RoomId.inOrder().stream().filter(unlockedRooms::contains).toList();
            return new SessionSnapshot(player.name(), state, score, remaining, unlocked,
                    solvedPuzzleIds, inventory.items(), revealedHints, wrongAttempts);
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------ helpers (lock held)

    private void announce(GameState newState) {
        if (newState != null) {
            listener.onStateChanged(newState);
        }
    }

    private void requireCatalogPuzzle(Puzzle puzzle) {
        Objects.requireNonNull(puzzle, "puzzle must not be null");
        Puzzle known = catalog.puzzle(puzzle.id()).orElse(null);
        if (known != puzzle) {
            throw new IllegalArgumentException("Puzzle " + puzzle.id() + " is not part of this game.");
        }
    }

    private int readRemainingSeconds() {
        int remaining = remainingSecondsSupplier.getAsInt();
        if (remaining < 0 || remaining > TIME_LIMIT_SECONDS) {
            throw new IllegalStateException("Remaining seconds out of range: " + remaining);
        }
        return remaining;
    }

    private void refreshUnlockedRooms() {
        UnlockContext context = unlockContext();
        for (RoomId room : RoomId.values()) {
            if (!unlockedRooms.contains(room) && unlockRule.isUnlocked(room, context)) {
                unlockedRooms.add(room);
            }
        }
    }

    private UnlockContext unlockContext() {
        Set<String> itemIds = inventory.items().stream().map(Item::id).collect(Collectors.toSet());
        return new UnlockContext(itemIds, new HashSet<>(solvedPuzzleIds));
    }

    private String missingMessage(List<String> missingIds) {
        EnumSet<ItemKind> kinds = EnumSet.noneOf(ItemKind.class);
        for (String itemId : missingIds) {
            catalog.item(itemId).ifPresent(item -> kinds.add(item.kind()));
        }
        if (kinds.isEmpty()) {
            return "You are missing required items.";
        }
        return "You are missing: " + kinds.stream().map(Enum::name).collect(Collectors.joining(", "))
                + ". Collect all clues first.";
    }

    private static Update<SubmissionResult> unchanged(SubmissionResult.Status status, String message) {
        return new Update<>(new SubmissionResult(status, message), null);
    }

    private static HintResult refused(String message) {
        return new HintResult(HintResult.Status.REFUSED, message, 0);
    }

    /** Throws {@link IllegalStateException} if the representation invariant is broken. Lock held. */
    private void checkRep() {
        if (state == null) {
            throw new IllegalStateException("RI violated: state is null");
        }
        if (score < 0) {
            throw new IllegalStateException("RI violated: negative score " + score);
        }
        if (!unlockedRooms.contains(RoomId.OFFICE)) {
            throw new IllegalStateException("RI violated: OFFICE is not unlocked");
        }
        for (String puzzleId : solvedPuzzleIds) {
            if (catalog.puzzle(puzzleId).isEmpty()) {
                throw new IllegalStateException("RI violated: unknown solved puzzle " + puzzleId);
            }
        }
        Set<String> rewardIdsOfCompletedRooms = new HashSet<>();
        for (Room room : catalog.rooms()) {
            if (solvedPuzzleIds.containsAll(room.requiredPuzzleIds())) {
                catalog.completionItem(room.id()).ifPresent(item -> rewardIdsOfCompletedRooms.add(item.id()));
            }
        }
        for (Item item : inventory.items()) {
            if (!rewardIdsOfCompletedRooms.contains(item.id())) {
                throw new IllegalStateException("RI violated: item of an incomplete room " + item.id());
            }
        }
        UnlockContext context = unlockContext();
        for (RoomId room : unlockedRooms) {
            if (!unlockRule.isUnlocked(room, context)) {
                throw new IllegalStateException("RI violated: room " + room + " is unlocked but its rule fails");
            }
        }
    }
}
