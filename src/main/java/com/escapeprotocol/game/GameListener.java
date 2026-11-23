package com.escapeprotocol.game;

/**
 * Receives game events. The game logic only knows this interface, never Swing.
 *
 * <p>Implementations must be quick and must not block. Callers invoke these methods
 * <em>outside</em> the session lock. The method may be called from a background thread,
 * so a Swing adapter must forward the call with {@code EdtUtil.invokeLater} (a later module).
 */
public interface GameListener {

    /**
     * Called about once per second while the game is running.
     *
     * @param remainingSeconds seconds left, never negative
     */
    void onTick(int remainingSeconds);

    /**
     * Called after the game state changed.
     *
     * @param state the new state; never {@code null}
     */
    void onStateChanged(GameState state);
}
