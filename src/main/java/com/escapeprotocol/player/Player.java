package com.escapeprotocol.player;

import com.escapeprotocol.util.Validators;

/**
 * An immutable player profile. In Module 03 a player is just a validated name.
 *
 * <p><b>Abstraction function:</b> AF(name) = the player whose display name is {@code name}.
 *
 * <p><b>Representation invariant:</b> {@code name} is non-null, already trimmed, and
 * matches the SRS player-name rule (see {@link Validators#PLAYER_NAME_REGEX}).
 *
 * <p><b>Equality:</b> two players are equal when their names are equal
 * (case-sensitive, exactly as stored).
 */
public final class Player {

    private final String name;

    /**
     * Creates a player.
     *
     * @param name the raw name typed by the user; it is trimmed before validation
     * @throws IllegalArgumentException if the name is {@code null} or invalid
     */
    public Player(String name) {
        this.name = Validators.requireValidPlayerName(name);
    }

    /** @return the trimmed, valid player name */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Player)) {
            return false;
        }
        return name.equals(((Player) other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Player[name=" + name + "]";
    }
}
