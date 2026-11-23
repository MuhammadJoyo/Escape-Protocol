package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    @Test
    void constantsMatchTheSrsRules() {
        assertEquals(1000, ScoreCalculator.INITIAL_SCORE);
        assertEquals(25, ScoreCalculator.WRONG_ATTEMPT_PENALTY);
        assertEquals(50, ScoreCalculator.HINT_PENALTY);
        assertEquals(2, ScoreCalculator.POINTS_PER_REMAINING_SECOND);
    }

    @Test
    void wrongAttemptSubtractsTwentyFive() {
        assertEquals(975, calculator.afterWrongAttempt(1000));
        assertEquals(950, calculator.afterWrongAttempt(975));
        assertEquals(1, calculator.afterWrongAttempt(26));
    }

    @Test
    void wrongAttemptNeverGoesBelowZero() {
        assertEquals(0, calculator.afterWrongAttempt(25));
        assertEquals(0, calculator.afterWrongAttempt(24));
        assertEquals(0, calculator.afterWrongAttempt(1));
        assertEquals(0, calculator.afterWrongAttempt(0));
    }

    @Test
    void hintSubtractsFifty() {
        assertEquals(950, calculator.afterHint(1000));
        assertEquals(1, calculator.afterHint(51));
    }

    @Test
    void hintNeverGoesBelowZero() {
        assertEquals(0, calculator.afterHint(50));
        assertEquals(0, calculator.afterHint(49));
        assertEquals(0, calculator.afterHint(0));
    }

    @Test
    void penaltiesAccumulateAcrossCalls() {
        int score = ScoreCalculator.INITIAL_SCORE;
        score = calculator.afterWrongAttempt(score);
        score = calculator.afterHint(score);
        score = calculator.afterWrongAttempt(score);
        assertEquals(1000 - 25 - 50 - 25, score);
    }

    @Test
    void victoryAddsTwoPointsPerRemainingSecond() {
        assertEquals(2175, calculator.victoryScore(975, 600));
        assertEquals(1000, calculator.victoryScore(1000, 0));
        assertEquals(1002, calculator.victoryScore(1000, 1));
    }

    @Test
    void maximumPossibleVictoryScoreIsThirtyFourHundred() {
        assertEquals(3400, calculator.victoryScore(1000, 1200));
    }

    @Test
    void victoryScoreWithZeroScoreStillGetsTheTimeBonus() {
        assertEquals(240, calculator.victoryScore(0, 120));
    }

    @Test
    void victoryScoreIsCappedInsteadOfOverflowing() {
        assertEquals(Integer.MAX_VALUE, calculator.victoryScore(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MAX_VALUE, calculator.victoryScore(Integer.MAX_VALUE - 1, 1200));
        assertEquals(Integer.MAX_VALUE, calculator.victoryScore(0, Integer.MAX_VALUE));
    }

    @Test
    void penaltiesAreSafeAtTheTopOfTheIntRange() {
        assertEquals(Integer.MAX_VALUE - 25, calculator.afterWrongAttempt(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE - 50, calculator.afterHint(Integer.MAX_VALUE));
    }

    @Test
    void negativeInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.afterWrongAttempt(-1));
        assertThrows(IllegalArgumentException.class, () -> calculator.afterHint(-1));
        assertThrows(IllegalArgumentException.class, () -> calculator.victoryScore(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> calculator.victoryScore(10, -1));
        assertThrows(IllegalArgumentException.class, () -> calculator.afterHint(Integer.MIN_VALUE));
    }
}
