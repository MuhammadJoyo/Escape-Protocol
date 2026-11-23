package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.inventory.Item;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Integration tests: resubmitting solved puzzles through {@link GameEngine} with the real puzzles.
 * A solved puzzle must answer ALREADY_SOLVED, with no penalty and no second reward.
 */
class ResubmitTest {

    private static final String OFFICE_ANSWER = "2004";
    private static final String LIBRARY_ANSWER = "RED,GREEN,BLUE,YELLOW";
    private static final String LAB_ANSWER = "30";
    private static final String FINAL_ANSWER = "2004-7394-30";

    private final AtomicInteger remaining = new AtomicInteger(600);
    private final GameEngine engine =
            new GameEngine(GameCatalog.standardWithPuzzles(), new HardCodedUnlockRule(), remaining::get);

    private ResubmitTest start() {
        engine.newGame("Alice");
        return this;
    }

    private static List<String> itemIds(SessionSnapshot snapshot) {
        return snapshot.items().stream().map(Item::id).toList();
    }

    @Test
    void resubmittingTheOfficePuzzleIsRefusedWithoutPenaltyOrSecondKey() {
        start();
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("office_year", OFFICE_ANSWER).status());
        SessionSnapshot afterSolving = engine.snapshot();

        assertEquals(SubmissionResult.Status.ALREADY_SOLVED, engine.submit("office_year", OFFICE_ANSWER).status());
        assertEquals(afterSolving, engine.snapshot());
        assertEquals(List.of("office_key"), itemIds(engine.snapshot()));
        assertEquals(1000, engine.snapshot().score());
    }

    @Test
    void anyAnswerToASolvedPuzzleIsAlreadySolvedWithoutPenalty() {
        start();
        engine.submit("office_year", OFFICE_ANSWER);
        for (String answer : new String[] {"1998", "20", "abcd", "", null, OFFICE_ANSWER}) {
            assertEquals(SubmissionResult.Status.ALREADY_SOLVED, engine.submit("office_year", answer).status());
        }
        SessionSnapshot s = engine.snapshot();
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
        assertEquals(1, s.items().size());
    }

    @Test
    void everyRoomGivesItsRewardExactlyOnceEvenWhenResubmitted() {
        start();
        String[][] rooms = {
                {"office_year", OFFICE_ANSWER, "office_key"},
                {"library_order", LIBRARY_ANSWER, "code_7394"},
                {"lab_sequence", LAB_ANSWER, "lab_clue_30"}};
        int expectedItems = 0;
        for (String[] room : rooms) {
            assertEquals(SubmissionResult.Status.CORRECT, engine.submit(room[0], room[1]).status());
            expectedItems++;
            for (int again = 0; again < 3; again++) {
                assertEquals(SubmissionResult.Status.ALREADY_SOLVED, engine.submit(room[0], room[1]).status());
            }
            SessionSnapshot s = engine.snapshot();
            assertEquals(expectedItems, s.items().size());
            assertTrue(itemIds(s).contains(room[2]));
            assertEquals(new HashSet<>(itemIds(s)).size(), s.items().size(), "no duplicate item ids");
            assertEquals(1000, s.score());
        }
        assertEquals(List.of("office_key", "code_7394", "lab_clue_30"), itemIds(engine.snapshot()));
    }

    @Test
    void aWrongAnswerBeforeTheCorrectOneDoesNotDuplicateTheReward() {
        start();
        engine.submit("office_year", "1998");
        engine.submit("office_year", OFFICE_ANSWER);
        engine.submit("office_year", OFFICE_ANSWER);
        SessionSnapshot s = engine.snapshot();
        assertEquals(List.of("office_key"), itemIds(s));
        assertEquals(975, s.score());
        assertEquals(1, s.wrongAttempts());
    }

    @Test
    void roomsStayLockedUntilTheirRewardExistsAndResubmissionDoesNotUnlockEarly() {
        start();
        assertEquals(SubmissionResult.Status.REFUSED_LOCKED, engine.submit("library_order", LIBRARY_ANSWER).status());
        engine.submit("office_year", OFFICE_ANSWER);
        engine.submit("office_year", OFFICE_ANSWER);
        assertEquals(SubmissionResult.Status.REFUSED_LOCKED, engine.submit("lab_sequence", LAB_ANSWER).status());
        assertEquals(SubmissionResult.Status.REFUSED_LOCKED, engine.submit("final_exit", FINAL_ANSWER).status());
        assertEquals(Set.of("office_year"), engine.snapshot().solvedPuzzleIds());
    }

    @Test
    void wholeRealGameEndsInVictoryAndLaterSubmissionsChangeNothing() {
        start();
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("office_year", OFFICE_ANSWER).status());
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("library_order", LIBRARY_ANSWER).status());
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("lab_sequence", LAB_ANSWER).status());
        remaining.set(400);
        assertEquals(SubmissionResult.Status.VICTORY, engine.submit("final_exit", FINAL_ANSWER).status());

        SessionSnapshot won = engine.snapshot();
        assertEquals(GameState.VICTORY, won.state());
        assertEquals(1000 + 2 * 400, won.score());
        assertEquals(List.of("office_key", "code_7394", "lab_clue_30"), itemIds(won));

        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, engine.submit("final_exit", FINAL_ANSWER).status());
        assertEquals(SubmissionResult.Status.REFUSED_NOT_RUNNING, engine.submit("office_year", "1998").status());
        assertEquals(won, engine.snapshot());
    }

    @Test
    void restartMakesPuzzlesSolvableAgainWithOneNewReward() {
        start();
        engine.submit("office_year", OFFICE_ANSWER);
        engine.restart();
        assertEquals(List.of(), itemIds(engine.snapshot()));
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("office_year", OFFICE_ANSWER).status());
        assertEquals(SubmissionResult.Status.ALREADY_SOLVED, engine.submit("office_year", OFFICE_ANSWER).status());
        assertEquals(List.of("office_key"), itemIds(engine.snapshot()));
    }
}
