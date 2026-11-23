package com.escapeprotocol.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Central place for input validation rules.
 *
 * <p>It holds every regular expression of the game (FR-LL-07): the player name (Module 03) and the
 * puzzle answers (Module 05): a four-digit year, the Library order, an integer sequence answer and
 * the Final Exit code.
 *
 * <p>Common rules for every method:
 * <ul>
 *   <li>the input is <em>trimmed</em> first, so leading and trailing spaces never matter;</li>
 *   <li>{@code null} and empty input are simply invalid; no method throws for bad player input;</li>
 *   <li>digits mean ASCII digits only;</li>
 *   <li>the {@code parseXxx} methods return the normalized value, or an empty result when the
 *       input is invalid; the {@code isValidXxx} methods are shortcuts for "parse succeeded".</li>
 * </ul>
 */
public final class Validators {

    /** The exact player-name regular expression from the SRS. */
    public static final String PLAYER_NAME_REGEX = "^[A-Za-z0-9 _-]{1,20}$";

    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile(PLAYER_NAME_REGEX);

    /** A year typed by the player: exactly four digits (Design §13, Office puzzle). */
    public static final String YEAR_REGEX = "^\\d{4}$";

    /**
     * An integer answer: an optional minus sign and one to nine digits. Nine digits always fit
     * into an {@code int}, so longer input is rejected instead of overflowing.
     */
    public static final String SEQUENCE_ANSWER_REGEX = "^-?\\d{1,9}$";

    /** The Final Exit answer {@code YYYY-CCCC-N} (SRS FR-PZ-09). */
    public static final String FINAL_EXIT_CODE_REGEX = "^\\d{4}-\\d{4}-\\d{1,3}$";

    /**
     * The shape of a Library order: exactly four comma-separated words; spaces around the words
     * are allowed. Whether the words are the right books is checked by
     * {@link #parseLibraryOrder(String, Collection)}.
     */
    public static final String LIBRARY_ORDER_REGEX = "^\\s*[A-Za-z]+\\s*(,\\s*[A-Za-z]+\\s*){3}$";

    private static final Pattern YEAR_PATTERN = Pattern.compile(YEAR_REGEX);
    private static final Pattern SEQUENCE_ANSWER_PATTERN = Pattern.compile(SEQUENCE_ANSWER_REGEX);
    private static final Pattern FINAL_EXIT_CODE_PATTERN = Pattern.compile(FINAL_EXIT_CODE_REGEX);
    private static final Pattern LIBRARY_ORDER_PATTERN = Pattern.compile(LIBRARY_ORDER_REGEX);

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
    /**
     * Parses a year typed by the player.
     *
     * @param input the raw text; may be {@code null}
     * @return the trimmed four-digit year, or empty if the input is {@code null} or not exactly
     *         four digits
     */
    public static Optional<String> parseYear(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        return YEAR_PATTERN.matcher(trimmed).matches() ? Optional.of(trimmed) : Optional.empty();
    }

    /**
     * @param input the raw text; may be {@code null}
     * @return {@code true} if {@link #parseYear(String)} succeeds
     */
    public static boolean isValidYear(String input) {
        return parseYear(input).isPresent();
    }

    /**
     * Parses a Library order such as {@code "red, Green,BLUE,yellow"}. The input must be four
     * comma-separated words (see {@link #LIBRARY_ORDER_REGEX}); after trimming each word and
     * converting it to upper case, the words must be pairwise different and all belong to
     * {@code books}. Missing, duplicate, unknown or extra books make the input invalid.
     *
     * @param input the raw text; may be {@code null}
     * @param books the allowed book names; must not be {@code null}; compared in upper case
     * @return the four normalized (upper-case) names in the typed order, as an unmodifiable list,
     *         or empty if the input is invalid
     * @throws NullPointerException if {@code books} is {@code null}
     */
    public static Optional<List<String>> parseLibraryOrder(String input, Collection<String> books) {
        Objects.requireNonNull(books, "books must not be null");
        if (input == null || !LIBRARY_ORDER_PATTERN.matcher(input).matches()) {
            return Optional.empty();
        }
        Set<String> allowed = new LinkedHashSet<>();
        for (String book : books) {
            allowed.add(book.toUpperCase(Locale.ROOT));
        }
        Set<String> chosen = new LinkedHashSet<>();
        for (String word : input.split(",", -1)) {
            String name = word.trim().toUpperCase(Locale.ROOT);
            if (!allowed.contains(name) || !chosen.add(name)) {
                return Optional.empty();
            }
        }
        if (chosen.size() != allowed.size()) {
            return Optional.empty();
        }
        return Optional.of(List.copyOf(chosen));
    }

    /**
     * @param input the raw text; may be {@code null}
     * @param books the allowed book names; must not be {@code null}
     * @return {@code true} if {@link #parseLibraryOrder(String, Collection)} succeeds
     * @throws NullPointerException if {@code books} is {@code null}
     */
    public static boolean isValidLibraryOrder(String input, Collection<String> books) {
        return parseLibraryOrder(input, books).isPresent();
    }

    /**
     * Parses an integer answer. At most nine digits (and an optional minus sign) are accepted, so
     * the value always fits into an {@code int}; leading zeros are allowed ({@code "030"} is 30).
     *
     * @param input the raw text; may be {@code null}
     * @return the value, or empty if the input is {@code null}, not an integer, or too long
     */
    public static OptionalInt parseSequenceAnswer(String input) {
        if (input == null) {
            return OptionalInt.empty();
        }
        String trimmed = input.trim();
        if (!SEQUENCE_ANSWER_PATTERN.matcher(trimmed).matches()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(trimmed));
    }

    /**
     * @param input the raw text; may be {@code null}
     * @return {@code true} if {@link #parseSequenceAnswer(String)} succeeds
     */
    public static boolean isValidSequenceAnswer(String input) {
        return parseSequenceAnswer(input).isPresent();
    }

    /**
     * Parses a Final Exit answer of the form {@code YYYY-CCCC-N}: four digits, a minus sign,
     * four digits, a minus sign, and one to three digits. No other separators or spaces are
     * accepted (apart from trimming the whole text).
     *
     * @param input the raw text; may be {@code null}
     * @return the three parts as text (year, code, number) in an unmodifiable list, or empty if
     *         the input is invalid
     */
    public static Optional<List<String>> parseFinalExitCode(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        if (!FINAL_EXIT_CODE_PATTERN.matcher(trimmed).matches()) {
            return Optional.empty();
        }
        String[] parts = trimmed.split("-");
        return Optional.of(List.of(parts[0], parts[1], parts[2]));
    }

    /**
     * @param input the raw text; may be {@code null}
     * @return {@code true} if {@link #parseFinalExitCode(String)} succeeds
     */
    public static boolean isValidFinalExitCode(String input) {
        return parseFinalExitCode(input).isPresent();
    }
}
