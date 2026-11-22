package com.escapeprotocol.util;

import java.util.regex.Pattern;

/**
 * Central place for input validation rules.
 *
 * <p>Module 03 only contains the player-name rule (SRS regex
 * {@code ^[A-Za-z0-9 _-]{1,20}$}). Puzzle-answer validators are added in Module 05.
 *
 * <p>The rule is applied to the <em>trimmed</em> text, so leading and trailing
 * spaces never count towards the 20-character limit.
 */
public final class Validators {

    /** The exact player-name regular expression from the SRS. */
    public static final String PLAYER_NAME_REGEX = "^[A-Za-z0-9 _-]{1,20}$";

    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile(PLAYER_NAME_REGEX);

    private Validators() {
        // Utility class: no instances.
    }

    /**
     * Checks whether a text is an acceptable player name.
     *
     * @param name the raw text typed by the user; may be {@code null}
     * @return {@code true} if, after trimming, the text matches the SRS rule;
     *         {@code false} for {@code null} or any invalid text
     */
    public static boolean isValidPlayerName(String name) {
        if (name == null) {
            return false;
        }
        return PLAYER_NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates a player name and returns its trimmed form.
     *
     * @param name the raw text typed by the user; may be {@code null}
     * @return the trimmed, valid name
     * @throws IllegalArgumentException if the name is {@code null} or invalid
     */
    public static String requireValidPlayerName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Player name must not be null.");
        }
        String trimmed = name.trim();
        if (!PLAYER_NAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Player name must be 1-20 characters: letters, digits, space, '_' or '-'.");
        }
        return trimmed;
    }
}
