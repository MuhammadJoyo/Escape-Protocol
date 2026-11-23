package com.escapeprotocol.game;

import com.escapeprotocol.rooms.RoomId;

/**
 * Decides whether a room is unlocked. This is the only place that decides unlocking, so the
 * hard-coded rules (Module 04) can later be replaced by rules read from the Escape Rule
 * Language (Module 10) without changing the engine.
 */
public interface UnlockRule {

    /**
     * Tells whether a room is unlocked for the given player situation.
     *
     * <p>Contract: {@link RoomId#OFFICE} is always unlocked (the game starts there), so every
     * implementation must return {@code true} for it. The method has no side effects.
     *
     * @param room the room to check; must not be {@code null}
     * @param context what the player owns and has solved; must not be {@code null}
     * @return {@code true} if the room is unlocked
     * @throws NullPointerException if an argument is {@code null}
     */
    boolean isUnlocked(RoomId room, UnlockContext context);
}
