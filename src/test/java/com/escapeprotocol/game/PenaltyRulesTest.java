package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.puzzles.OfficePuzzle;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Integration tests of the scoring rules with the real puzzles, through {@link GameEngine}: the
 * puzzles only judge answers, and {@link GameSession} (with {@link ScoreCalculator}) applies the
 * penalties.
 */
class PenaltyRulesTest {

    private final AtomicInteger remaining = new AtomicInteger(600);
    private final GameEngine engine =
            new GameEngine(GameCatalog.standardWithPuzzles(), new HardCodedUnlockRule(), remaining::get);

    private void solveThroughLaboratory() {
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("office_year", "2004").status());
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("library_order", "RED,GREEN,BLUE,YELLOW").status());
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("lab_sequence", "30").status());
    }

    @Test
    void invalidFormatHasNoPenaltyAndIsNotAnAttemptInEveryRoom() {
        engine.newGame("Alice");
        for (String answer : new String[] {"20", "abcd", "", null, "20045"}) {
            assertEquals(SubmissionResult.Status.INVALID_FORMAT, engine.submit("office_year", answer).status());
        }
        engine.submit("office_year", "2004");
        for (String answer : new String[] {"RED,GREEN,BLUE", "RED,RED,BLUE,YELLOW", "RED,GREEN,BLUE,PURPLE", ""}) {
            assertEquals(SubmissionResult.Status.INVALID_FORMAT, engine.submit("library_order", answer).status());
        }
        engine.submit("library_order", "RED,GREEN,BLUE,YELLOW");
        for (String answer : new String[] {"abc", "99999999999", "3.0", null}) {
            assertEquals(SubmissionResult.Status.INVALID_FORMAT, engine.submit("lab_sequence", answer).status());
        }
        engine.submit("lab_sequence", "30");
        for (String answer : new String[] {"2004-7394", "abc", "2004/7394/30", ""}) {
            assertEquals(SubmissionResult.Status.INVALID_FORMAT, engine.submit("final_exit", answer).status());
        }
        SessionSnapshot s = engine.snapshot();
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
        assertEquals(GameState.RUNNING, s.state());
    }

    @Test
    void anIncorrectButValidAnswerCostsTwentyFiveInEveryRoom() {
        engine.newGame("Alice");
        assertEquals(SubmissionResult.Status.INCORRECT, engine.submit("office_year", "1998").status());
        assertEquals(975, engine.snapshot().score());
        engine.submit("office_year", "2004");

        assertEquals(SubmissionResult.Status.INCORRECT,
                engine.submit("library_order", "GREEN,RED,BLUE,YELLOW").status());
        assertEquals(950, engine.snapshot().score());
        engine.submit("library_order", "RED,GREEN,BLUE,YELLOW");

        assertEquals(SubmissionResult.Status.INCORRECT, engine.submit("lab_sequence", "31").status());
        assertEquals(925, engine.snapshot().score());
        engine.submit("lab_sequence", "30");

        assertEquals(SubmissionResult.Status.INCORRECT, engine.submit("final_exit", "2004-7394-31").status());
        SessionSnapshot s = engine.snapshot();
        assertEquals(900, s.score());
        assertEquals(4, s.wrongAttempts());
    }

    @Test
    void theScoreNeverFallsBelowZero() {
        engine.newGame("Alice");
        for (int i = 0; i < 45; i++) {
            assertEquals(SubmissionResult.Status.INCORRECT, engine.submit("office_year", "1998").status());
        }
        SessionSnapshot s = engine.snapshot();
        assertEquals(0, s.score());
        assertEquals(45, s.wrongAttempts());

        // A correct answer, a hint and a further wrong answer all keep the score at zero.
        assertEquals(SubmissionResult.Status.CORRECT, engine.submit("office_year", "2004").status());
        assertEquals(0, engine.snapshot().score());
        assertEquals(SubmissionResult.Status.INCORRECT, engine.submit("library_order", "BLUE,RED,GREEN,YELLOW").status());
        assertEquals(0, engine.snapshot().score());
        assertEquals(HintResult.Status.REVEALED_NEW, engine.requestHint("library_order").status());
        assertEquals(0, engine.snapshot().score());
    }

    @Test
    void resubmittingASolvedPuzzleHasNoPenalty() {
        engine.newGame("Alice");
        engine.submit("office_year", "2004");
        for (String answer : new String[] {"1998", "abcd", "2004"}) {
            assertEquals(SubmissionResult.Status.ALREADY_SOLVED, engine.submit("office_year", answer).status());
        }
        assertEquals(1000, engine.snapshot().score());
        assertEquals(0, engine.snapshot().wrongAttempts());
    }

    @Test
    void aCorrectAnswerNeverAppliesTheWrongAnswerPenalty() {
        engine.newGame("Alice");
        solveThroughLaboratory();
        assertEquals(1000, engine.snapshot().score());
        assertEquals(0, engine.snapshot().wrongAttempts());

        remaining.set(100);
        assertEquals(SubmissionResult.Status.VICTORY, engine.submit("final_exit", "2004-7394-30").status());
        assertEquals(1000 + 2 * 100, engine.snapshot().score());
    }

    @Test
    void victoryScoreStartsFromTheScoreAfterEarlierPenalties() {
        engine.newGame("Alice");
        engine.submit("office_year", "1998");        // 975
        engine.requestHint("office_year");           // 925
        solveThroughLaboratory();
        engine.submit("final_exit", "1999-7394-30"); // 900
        remaining.set(250);
        assertEquals(SubmissionResult.Status.VICTORY, engine.submit("final_exit", "2004-7394-30").status());
        assertEquals(900 + 2 * 250, engine.snapshot().score());
    }

    @Test
    void finalExitFeedbackCountsComponentsAndStillCostsOneWrongAttempt() {
        engine.newGame("Alice");
        solveThroughLaboratory();

        SubmissionResult twoOfThree = engine.submit("final_exit", "2004-1111-30");
        assertEquals(SubmissionResult.Status.INCORRECT, twoOfThree.status());
        assertTrue(twoOfThree.message().contains("2 of 3"));
        assertEquals(975, engine.snapshot().score());

        SubmissionResult noneOfThree = engine.submit("final_exit", "1999-1111-20");
        assertEquals(SubmissionResult.Status.INCORRECT, noneOfThree.status());
        assertTrue(noneOfThree.message().contains("0 of 3"));

        SessionSnapshot s = engine.snapshot();
        assertEquals(950, s.score());
        assertEquals(2, s.wrongAttempts());
        assertEquals(GameState.RUNNING, s.state());
    }

    @Test
    void finalExitFeedbackDoesNotRevealWhichComponentsAreRight() {
        engine.newGame("Alice");
        solveThroughLaboratory();
        String wrongYear = engine.submit("final_exit", "1999-7394-30").message();
        String wrongCode = engine.submit("final_exit", "2004-1111-30").message();
        String wrongNumber = engine.submit("final_exit", "2004-7394-31").message();
        assertEquals(wrongYear, wrongCode);
        assertEquals(wrongCode, wrongNumber);
    }

    @Test
    void newHintsCostFiftyEachAndTheRealPuzzleHasExactlyThree() {
        engine.newGame("Alice");
        OfficePuzzle office = new OfficePuzzle();
        for (int level = 1; level <= 3; level++) {
            HintResult hint = engine.requestHint("office_year");
            assertEquals(HintResult.Status.REVEALED_NEW, hint.status());
            assertEquals(level, hint.level());
            assertEquals(office.hints().get(level - 1), hint.text());
            assertEquals(1000 - 50 * level, engine.snapshot().score());
        }
        assertEquals(HintResult.Status.NO_MORE_HINTS, engine.requestHint("office_year").status());
        assertEquals(850, engine.snapshot().score());

        HintResult again = engine.viewHint("office_year", 2);
        assertEquals(HintResult.Status.VIEWED_AGAIN, again.status());
        assertEquals(office.hints().get(1), again.text());
        assertEquals(850, engine.snapshot().score());
    }

    @Test
    void everyRealPuzzleOffersThreeHintsThroughTheEngine() {
        engine.newGame("Alice");
        String[][] stages = {
                {"office_year", "2004"},
                {"library_order", "RED,GREEN,BLUE,YELLOW"},
                {"lab_sequence", "30"},
                {"final_exit", null}};
        for (String[] stage : stages) {
            int before = engine.snapshot().score();
            for (int i = 0; i < 3; i++) {
                assertEquals(HintResult.Status.REVEALED_NEW, engine.requestHint(stage[0]).status(), stage[0]);
            }
            assertEquals(HintResult.Status.NO_MORE_HINTS, engine.requestHint(stage[0]).status(), stage[0]);
            assertEquals(before - 150, engine.snapshot().score(), stage[0]);
            if (stage[1] != null) {
                engine.submit(stage[0], stage[1]);
            }
        }
    }

    @Test
    void missingCluesAreRefusedForFreeAndNameTheMissingKinds() {
        // A rule that opens every room lets us reach the Final Exit without having collected any clue.
        GameEngine open = new GameEngine(GameCatalog.standardWithPuzzles(), (room, context) -> true, remaining::get);
        open.newGame("Alice");
        SubmissionResult result = open.submit("final_exit", "2004-7394-30");
        assertEquals(SubmissionResult.Status.REFUSED_MISSING_CLUES, result.status());
        assertTrue(result.message().contains("KEY"));
        assertTrue(result.message().contains("CODE"));
        assertTrue(result.message().contains("CLUE"));
        SessionSnapshot s = open.snapshot();
        assertEquals(1000, s.score());
        assertEquals(0, s.wrongAttempts());
        assertEquals(GameState.RUNNING, s.state());
        assertFalse(s.solvedPuzzleIds().contains("final_exit"));
    }
}
