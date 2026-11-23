package com.escapeprotocol.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.escapeprotocol.inventory.Item;
import com.escapeprotocol.rooms.RoomId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests room progression decisions: which rooms {@link HardCodedUnlockRule} unlocks for which items.
 * Session-level progression (solving puzzles through the engine) is tested in later parts.
 */
class ProgressionTest {

    private final UnlockRule rule = new HardCodedUnlockRule();
    private final GameCatalog catalog = GameCatalog.standard(List.of());

    private static UnlockContext withItems(String... itemIds) {
        return new UnlockContext(Set.of(itemIds), Set.of());
    }

    @Test
    void officeIsUnlockedAtTheStart() {
        assertTrue(rule.isUnlocked(RoomId.OFFICE, withItems()));
    }

    @Test
    void officeStaysUnlockedWhateverTheContext() {
        assertTrue(rule.isUnlocked(RoomId.OFFICE, withItems("office_key", "code_7394", "lab_clue_30")));
    }

    @Test
    void libraryIsLockedUntilTheOfficeKeyIsOwned() {
        assertFalse(rule.isUnlocked(RoomId.LIBRARY, withItems()));
        assertTrue(rule.isUnlocked(RoomId.LIBRARY, withItems("office_key")));
    }

    @Test
    void laboratoryIsLockedUntilTheLibraryCodeIsOwned() {
        assertFalse(rule.isUnlocked(RoomId.LABORATORY, withItems()));
        assertFalse(rule.isUnlocked(RoomId.LABORATORY, withItems("office_key")));
        assertTrue(rule.isUnlocked(RoomId.LABORATORY, withItems("office_key", "code_7394")));
    }

    @Test
    void finalExitIsLockedUntilTheLaboratoryClueIsOwned() {
        assertFalse(rule.isUnlocked(RoomId.FINAL_EXIT, withItems()));
        assertFalse(rule.isUnlocked(RoomId.FINAL_EXIT, withItems("office_key", "code_7394")));
        assertTrue(rule.isUnlocked(RoomId.FINAL_EXIT,
                withItems("office_key", "code_7394", "lab_clue_30")));
    }

    @Test
    void anUnrelatedItemUnlocksNothing() {
        UnlockContext context = withItems("some_other_item");
        assertFalse(rule.isUnlocked(RoomId.LIBRARY, context));
        assertFalse(rule.isUnlocked(RoomId.LABORATORY, context));
        assertFalse(rule.isUnlocked(RoomId.FINAL_EXIT, context));
    }

    @Test
    void solvedPuzzlesAloneDoNotUnlockRoomsBecauseTheRuleLooksAtItems() {
        UnlockContext solvedOnly = new UnlockContext(Set.of(),
                Set.of("office_year", "library_order", "lab_sequence"));
        assertFalse(rule.isUnlocked(RoomId.LIBRARY, solvedOnly));
        assertFalse(rule.isUnlocked(RoomId.LABORATORY, solvedOnly));
        assertFalse(rule.isUnlocked(RoomId.FINAL_EXIT, solvedOnly));
    }

    @Test
    void collectingTheCatalogRewardsInOrderUnlocksOneRoomAtATime() {
        Set<String> owned = new HashSet<>();
        List<RoomId> order = RoomId.inOrder();

        // At the start only the Office is open.
        assertOnlyFirstRoomsUnlocked(owned, 1);

        // After each completed room, exactly the next room opens.
        for (int completed = 1; completed < order.size(); completed++) {
            Item reward = catalog.completionItem(order.get(completed - 1)).orElseThrow();
            owned.add(reward.id());
            assertOnlyFirstRoomsUnlocked(owned, completed + 1);
        }
    }

    private void assertOnlyFirstRoomsUnlocked(Set<String> owned, int unlockedCount) {
        UnlockContext context = new UnlockContext(owned, Set.of());
        List<RoomId> order = RoomId.inOrder();
        for (int i = 0; i < order.size(); i++) {
            boolean expected = i < unlockedCount;
            assertTrue(rule.isUnlocked(order.get(i), context) == expected,
                    order.get(i) + " should be " + (expected ? "unlocked" : "locked")
                            + " with items " + owned);
        }
    }

    @Test
    void finalExitCannotBeUnlockedByTheOfficeAndLibraryRewardsAlone() {
        Set<String> owned = new HashSet<>();
        owned.add(catalog.completionItem(RoomId.OFFICE).orElseThrow().id());
        owned.add(catalog.completionItem(RoomId.LIBRARY).orElseThrow().id());
        assertFalse(rule.isUnlocked(RoomId.FINAL_EXIT, new UnlockContext(owned, Set.of())));
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> rule.isUnlocked(null, withItems()));
        assertThrows(NullPointerException.class, () -> rule.isUnlocked(RoomId.OFFICE, null));
    }

    @Test
    void ruleUsesTheSameIdsAsTheCatalogRewards() {
        assertTrue(catalog.item("office_key").isPresent());
        assertTrue(catalog.item("code_7394").isPresent());
        assertTrue(catalog.item("lab_clue_30").isPresent());
    }
}
