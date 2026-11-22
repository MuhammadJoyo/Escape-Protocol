package com.escapeprotocol.game;

/**
 * The lifecycle states of a game, plus the table of legal transitions.
 *
 * <pre>
 * NOT_STARTED -&gt; RUNNING
 * RUNNING     -&gt; PAUSED | VICTORY | GAME_OVER
 * PAUSED      -&gt; RUNNING
 * VICTORY, GAME_OVER: no exit (a new game starts from NOT_STARTED)
 * </pre>
 *
 * <p>This type only describes states. The session and timer that use it come in later modules.
 */
public enum GameState {

    /** The game has been created but not started. */
    NOT_STARTED,
    /** The player is playing and the countdown is running. */
    RUNNING,
    /** The game is paused. */
    PAUSED,
    /** The player escaped. */
    VICTORY,
    /** The time ran out. */
    GAME_OVER;

    /**
     * Tells whether moving from this state to {@code next} is allowed.
     *
     * @param next the target state; may be {@code null}
     * @return {@code true} only for the five legal transitions; {@code false} for every other
     *         pair (including staying in the same state) and for {@code null}
     */
    public boolean canMoveTo(GameState next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case NOT_STARTED -> next == RUNNING;
            case RUNNING -> next == PAUSED || next == VICTORY || next == GAME_OVER;
            case PAUSED -> next == RUNNING;
            case VICTORY, GAME_OVER -> false;
        };
    }
}
