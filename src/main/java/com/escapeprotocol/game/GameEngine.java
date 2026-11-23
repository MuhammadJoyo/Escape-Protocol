package com.escapeprotocol.game;

import com.escapeprotocol.player.Player;
import com.escapeprotocol.puzzles.Puzzle;
import com.escapeprotocol.rooms.RoomId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;

/**
 * The public facade of the game logic (Design §6). The UI talks only to this class: it starts
 * games, submits answers by puzzle id, asks for hints, checks rooms, pauses and takes snapshots.
 *
 * <p>The engine <em>coordinates</em>; it does not decide anything itself. It looks puzzles up in the
 * {@link GameCatalog} and hands every operation to the current {@link GameSession}, which owns the
 * score, the hint costs, the progression, the room unlocking and the state transitions.
 *
 * <p><b>Lifecycle:</b>
 * <ul>
 *   <li>A new engine has no game. Every operation except {@link #newGame(String)} and
 *       {@link #shutdown()} throws {@link IllegalStateException} until a game exists.</li>
 *   <li>{@link #newGame(String)} validates the name, creates a <em>fresh</em> session and starts
 *       it (NOT_STARTED to RUNNING). Any earlier session is simply dropped.</li>
 *   <li>{@link #restart()} does the same with the name of the last game. A finished session
 *       (VICTORY or GAME_OVER) is never reused: restart always creates a new one.</li>
 *   <li>{@link #shutdown()} drops the session and refuses all later operations. It is safe to call
 *       more than once. No thread or timer exists in this module, so there is nothing else to
 *       stop yet; Module 07 will also stop the timer here.</li>
 * </ul>
 *
 * <p><b>Thread safety:</b> all game state lives in {@link GameSession}, which is guarded by its single
 * lock. The engine has <em>no lock</em> of its own. It only holds the reference to the current
 * session and the last player in atomic fields, so replacing the session is atomic and every
 * operation uses one consistent session. Starting, restarting and shutting down are meant to be
 * called from the UI thread (Design §15.2); the other operations may be called from any thread.
 *
 * <p><b>Listener:</b> the listener is given to each new session and is notified by the session
 * after its lock is released. The engine never calls the listener itself, so no event is delivered
 * twice. Dropping an old session does not notify anything.
 */
public final class GameEngine {

    private final GameCatalog catalog;
    private final UnlockRule unlockRule;
    private final IntSupplier remainingSeconds;
    private final GameListener listener; // null means "no listener"

    private final AtomicReference<GameSession> current = new AtomicReference<>();
    private final AtomicReference<Player> lastPlayer = new AtomicReference<>();
    private final AtomicBoolean shutDown = new AtomicBoolean(false);

    /**
     * Creates an engine without a listener.
     *
     * @param catalog rooms, rewards and puzzles; must not be {@code null}. It may lack some puzzles
     *        (Module 05 supplies them); submitting a missing puzzle id fails with
     *        {@link IllegalArgumentException}
     * @param unlockRule decides which rooms are unlocked; must not be {@code null}
     * @param remainingSeconds supplies the seconds left for a running game; must follow the
     *        {@link GameSession} contract (fast, non-blocking, no call back into the session). The
     *        timer of Module 07 will be passed here
     * @throws NullPointerException if an argument is {@code null}
     */
    public GameEngine(GameCatalog catalog, UnlockRule unlockRule, IntSupplier remainingSeconds) {
        this(catalog, unlockRule, remainingSeconds, false, null);
    }

    /**
     * Creates an engine with a listener.
     *
     * @param catalog rooms, rewards and puzzles; must not be {@code null}
     * @param unlockRule decides which rooms are unlocked; must not be {@code null}
     * @param remainingSeconds supplies the seconds left (see the three-argument constructor)
     * @param listener receives state changes from each session after its lock is released; must not
     *        be {@code null} and must not throw
     * @throws NullPointerException if an argument is {@code null}
     */
    public GameEngine(GameCatalog catalog, UnlockRule unlockRule, IntSupplier remainingSeconds,
                      GameListener listener) {
        this(catalog, unlockRule, remainingSeconds, true,
                Objects.requireNonNull(listener, "listener must not be null"));
    }

    private GameEngine(GameCatalog catalog, UnlockRule unlockRule, IntSupplier remainingSeconds,
                       boolean withListener, GameListener listener) {
        this.catalog = Objects.requireNonNull(catalog, "catalog must not be null");
        this.unlockRule = Objects.requireNonNull(unlockRule, "unlockRule must not be null");
        this.remainingSeconds = Objects.requireNonNull(remainingSeconds, "remainingSeconds must not be null");
        this.listener = withListener ? listener : null;
    }

    /**
     * Starts a new game for the named player: validates the name, creates a fresh session and
     * starts it. The listener receives RUNNING once.
     *
     * @param name the raw player name; trimmed and checked by {@link Player}
     * @throws IllegalArgumentException if the name is {@code null} or invalid; the current game, if
     *         any, is left untouched
     * @throws IllegalStateException if the engine has been shut down
     */
    public void newGame(String name) {
        requireNotShutDown();
        startFreshSession(new Player(name));
    }

    /**
     * Starts a fresh game for the same player as the last game (FR-GM-07). All progress of the old
     * session (score, solved puzzles, items, hints, state) is gone. Allowed in any state,
     * including after VICTORY or GAME_OVER. Asking the player to confirm is the UI's job.
     *
     * @throws IllegalStateException if no game has been started yet, or the engine has been shut down
     */
    public void restart() {
        requireNotShutDown();
        Player player = lastPlayer.get();
        if (player == null) {
            throw new IllegalStateException("No game to restart. Call newGame first.");
        }
        startFreshSession(player);
    }

    /**
     * Submits an answer for a puzzle of the current game.
     *
     * @param puzzleId the id of a puzzle of the catalog
     * @param answer the raw answer; may be {@code null}
     * @return the outcome decided by the session; never {@code null}
     * @throws IllegalArgumentException if the catalog has no puzzle with that id (also for
     *         {@code null})
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public SubmissionResult submit(String puzzleId, String answer) {
        GameSession session = requireSession();
        return session.submit(requirePuzzle(puzzleId), answer);
    }

    /**
     * Reveals the next hint of a puzzle (costs points; decided by the session).
     *
     * @param puzzleId the id of a puzzle of the catalog
     * @return the outcome; never {@code null}
     * @throws IllegalArgumentException if the catalog has no puzzle with that id
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public HintResult requestHint(String puzzleId) {
        GameSession session = requireSession();
        return session.revealHint(requirePuzzle(puzzleId));
    }

    /**
     * Shows an already revealed hint again (free; decided by the session).
     *
     * @param puzzleId the id of a puzzle of the catalog
     * @param level the 1-based hint level
     * @return the outcome; never {@code null}
     * @throws IllegalArgumentException if the catalog has no puzzle with that id
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public HintResult viewHint(String puzzleId, int level) {
        GameSession session = requireSession();
        return session.viewHint(requirePuzzle(puzzleId), level);
    }

    /**
     * Tells whether a room is unlocked in the current game.
     *
     * @param room the room; must not be {@code null}
     * @return {@code true} if unlocked
     * @throws NullPointerException if {@code room} is {@code null}
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public boolean isUnlocked(RoomId room) {
        return requireSession().isUnlocked(room);
    }

    /**
     * Pauses the current game (RUNNING to PAUSED).
     *
     * @return {@code true} if the game was paused; {@code false} if the state does not allow it
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public boolean pause() {
        return requireSession().pause();
    }

    /**
     * Resumes the current game (PAUSED to RUNNING).
     *
     * @return {@code true} if the game was resumed; {@code false} if the state does not allow it
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public boolean resume() {
        return requireSession().resume();
    }

    /**
     * Returns an immutable snapshot of the current game.
     *
     * @return the snapshot; never {@code null}
     * @throws IllegalStateException if there is no game, or the engine has been shut down
     */
    public SessionSnapshot snapshot() {
        return requireSession().snapshot();
    }

    /**
     * Shuts the engine down: drops the session and refuses every later operation. Calling it again
     * has no effect. Nothing is notified.
     */
    public void shutdown() {
        shutDown.set(true);
        current.set(null);
    }

    // ------------------------------------------------------------------ helpers

    private void startFreshSession(Player player) {
        GameSession session = listener == null
                ? new GameSession(player, catalog, unlockRule, remainingSeconds)
                : new GameSession(player, catalog, unlockRule, remainingSeconds, listener);
        lastPlayer.set(player);
        current.set(session);
        if (shutDown.get()) {
            // shutdown() ran while the session was being created: do not leave a live session behind.
            current.set(null);
            throw new IllegalStateException("The engine has been shut down.");
        }
        if (!session.start()) {
            throw new IllegalStateException("A new session could not be started.");
        }
    }

    private void requireNotShutDown() {
        if (shutDown.get()) {
            throw new IllegalStateException("The engine has been shut down.");
        }
    }

    private GameSession requireSession() {
        requireNotShutDown();
        GameSession session = current.get();
        if (session == null) {
            throw new IllegalStateException("No game in progress. Call newGame first.");
        }
        return session;
    }

    private Puzzle requirePuzzle(String puzzleId) {
        return catalog.puzzle(puzzleId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown puzzle id: " + puzzleId));
    }
}
