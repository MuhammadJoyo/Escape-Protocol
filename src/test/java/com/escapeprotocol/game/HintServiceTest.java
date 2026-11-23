package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class HintServiceTest {

    private static final List<String> THREE = List.of("first hint", "second hint", "third hint");

    private final HintService service = new HintService();

    @Test
    void maximumIsThreeHints() {
        assertEquals(3, HintService.MAX_HINTS);
    }

    @Test
    void hintsAreRevealedOneByOneInOrder() {
        HintResult first = service.request(THREE, 0);
        assertEquals(HintResult.Status.REVEALED_NEW, first.status());
        assertEquals("first hint", first.text());
        assertEquals(1, first.level());

        HintResult second = service.request(THREE, 1);
        assertEquals(HintResult.Status.REVEALED_NEW, second.status());
        assertEquals("second hint", second.text());
        assertEquals(2, second.level());

        HintResult third = service.request(THREE, 2);
        assertEquals(HintResult.Status.REVEALED_NEW, third.status());
        assertEquals("third hint", third.text());
        assertEquals(3, third.level());
    }

    @Test
    void afterAllHintsAreRevealedThereAreNoMoreHints() {
        HintResult result = service.request(THREE, 3);
        assertEquals(HintResult.Status.NO_MORE_HINTS, result.status());
        assertEquals(0, result.level());
    }

    @Test
    void onlyANewHintIsMarkedAsRevealedNew() {
        // The session charges the hint penalty only for REVEALED_NEW, so the other
        // statuses must be different.
        assertEquals(HintResult.Status.REVEALED_NEW, service.request(THREE, 1).status());
        assertEquals(HintResult.Status.VIEWED_AGAIN, service.view(THREE, 1, 1).status());
        assertEquals(HintResult.Status.NO_MORE_HINTS, service.request(THREE, 3).status());
        assertEquals(HintResult.Status.REFUSED, service.view(THREE, 1, 2).status());
    }

    @Test
    void viewingARevealedHintShowsItAgain() {
        HintResult one = service.view(THREE, 2, 1);
        assertEquals(HintResult.Status.VIEWED_AGAIN, one.status());
        assertEquals("first hint", one.text());
        assertEquals(1, one.level());

        HintResult two = service.view(THREE, 2, 2);
        assertEquals(HintResult.Status.VIEWED_AGAIN, two.status());
        assertEquals("second hint", two.text());
        assertEquals(2, two.level());
    }

    @Test
    void viewingAHintThatIsNotRevealedYetIsRefused() {
        assertEquals(HintResult.Status.REFUSED, service.view(THREE, 2, 3).status());
        assertEquals(HintResult.Status.REFUSED, service.view(THREE, 0, 1).status());
    }

    @Test
    void viewingOutOfRangeLevelsIsRefused() {
        assertEquals(HintResult.Status.REFUSED, service.view(THREE, 3, 0).status());
        assertEquals(HintResult.Status.REFUSED, service.view(THREE, 3, -1).status());
        assertEquals(HintResult.Status.REFUSED, service.view(THREE, 3, 4).status());
    }

    @Test
    void refusedAndNoMoreHintsShowNoHintText() {
        HintResult refused = service.view(THREE, 1, 3);
        assertEquals(0, refused.level());
        assertEquals(false, refused.text().isBlank());
        assertEquals(false, THREE.contains(refused.text()));
    }

    @Test
    void neverMoreThanThreeHintsEvenIfThePuzzleSuppliesMore() {
        List<String> five = List.of("h1", "h2", "h3", "h4", "h5");
        assertEquals(HintResult.Status.REVEALED_NEW, service.request(five, 2).status());
        assertEquals(HintResult.Status.NO_MORE_HINTS, service.request(five, 3).status());
        assertEquals(HintResult.Status.REFUSED, service.view(five, 3, 4).status());
        assertThrows(IllegalArgumentException.class, () -> service.request(five, 4));
    }

    @Test
    void puzzleWithFewerHintsRunsOutEarlier() {
        List<String> two = List.of("only one", "only two");
        assertEquals(HintResult.Status.REVEALED_NEW, service.request(two, 1).status());
        assertEquals(HintResult.Status.NO_MORE_HINTS, service.request(two, 2).status());
    }

    @Test
    void puzzleWithoutHintsHasNoMoreHintsImmediately() {
        assertEquals(HintResult.Status.NO_MORE_HINTS, service.request(List.of(), 0).status());
        assertEquals(HintResult.Status.REFUSED, service.view(List.of(), 0, 1).status());
    }

    @Test
    void serviceIsStatelessSoSameInputGivesSameResult() {
        HintResult a = service.request(THREE, 1);
        HintResult b = service.request(THREE, 1);
        assertEquals(a, b);
    }

    @Test
    void invalidArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> service.request(null, 0));
        assertThrows(NullPointerException.class, () -> service.view(null, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> service.request(THREE, -1));
        assertThrows(IllegalArgumentException.class, () -> service.request(THREE, 4));
        assertThrows(IllegalArgumentException.class, () -> service.view(THREE, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.view(THREE, 4, 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.request(Arrays.asList("ok", null), 0));
        assertThrows(IllegalArgumentException.class, () -> service.request(List.of("ok", "  "), 0));
    }
}
