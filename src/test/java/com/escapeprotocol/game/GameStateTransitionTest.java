package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GameStateTransitionTest {

    @Test
    void legalTransitionsAreAccepted() {
        assertTrue(GameState.NOT_STARTED.canMoveTo(GameState.RUNNING));
        assertTrue(GameState.RUNNING.canMoveTo(GameState.PAUSED));
        assertTrue(GameState.PAUSED.canMoveTo(GameState.RUNNING));
        assertTrue(GameState.RUNNING.canMoveTo(GameState.VICTORY));
        assertTrue(GameState.RUNNING.canMoveTo(GameState.GAME_OVER));
    }

    @Test
    void exactlyFiveTransitionsAreLegalAmongAllTwentyFivePairs() {
        int legal = 0;
        for (GameState from : GameState.values()) {
            for (GameState to : GameState.values()) {
                if (from.canMoveTo(to)) {
                    legal++;
                }
            }
        }
        assertEquals(5, legal);
    }

    @Test
    void illegalTransitionsAreRejected() {
        assertFalse(GameState.NOT_STARTED.canMoveTo(GameState.PAUSED));
        assertFalse(GameState.NOT_STARTED.canMoveTo(GameState.VICTORY));
        assertFalse(GameState.NOT_STARTED.canMoveTo(GameState.GAME_OVER));
        assertFalse(GameState.PAUSED.canMoveTo(GameState.VICTORY));
        assertFalse(GameState.PAUSED.canMoveTo(GameState.GAME_OVER));
        assertFalse(GameState.RUNNING.canMoveTo(GameState.NOT_STARTED));
        assertFalse(GameState.PAUSED.canMoveTo(GameState.NOT_STARTED));
    }

    @Test
    void finishedStatesHaveNoExit() {
        Set<GameState> finished = EnumSet.of(GameState.VICTORY, GameState.GAME_OVER);
        for (GameState from : finished) {
            for (GameState to : GameState.values()) {
                assertFalse(from.canMoveTo(to), from + " -> " + to + " must be illegal");
            }
        }
    }

    @Test
    void stayingInTheSameStateIsNotATransition() {
        for (GameState state : GameState.values()) {
            assertFalse(state.canMoveTo(state), state + " -> " + state);
        }
    }

    @Test
    void nullTargetIsRejectedWithoutException() {
        for (GameState state : GameState.values()) {
            assertFalse(state.canMoveTo(null));
        }
    }
}
