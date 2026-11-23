package com.escapeprotocol.game;

import com.escapeprotocol.rooms.RoomId;
import java.util.Objects;

/**
 * The first {@link UnlockRule} (Module 04). The rules are fixed in code and look only at the items
 * the player owns:
 *
 * <ul>
 *   <li>OFFICE: always unlocked;</li>
 *   <li>LIBRARY: unlocked when the player owns {@link GameCatalog#OFFICE_KEY_ID};</li>
 *   <li>LABORATORY: unlocked when the player owns {@link GameCatalog#LIBRARY_CODE_ID};</li>
 *   <li>FINAL_EXIT: unlocked when the player owns {@link GameCatalog#LABORATORY_CLUE_ID}.</li>
 * </ul>
 *
 * <p>Each room is decided by its own prerequisite item. Items are only granted by the session when a
 * room is completed, so owning the item means the previous room was completed. In Module 10 this
 * class can be replaced by an implementation that reads the same rules from the Escape Rule
 * Language.
 *
 * <p>The class has no state and is safe to share between threads.
 */
public final class HardCodedUnlockRule implements UnlockRule {

    @Override
    public boolean isUnlocked(RoomId room, UnlockContext context) {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(context, "context must not be null");
        return switch (room) {
            case OFFICE -> true;
            case LIBRARY -> context.hasItem(GameCatalog.OFFICE_KEY_ID);
            case LABORATORY -> context.hasItem(GameCatalog.LIBRARY_CODE_ID);
            case FINAL_EXIT -> context.hasItem(GameCatalog.LABORATORY_CLUE_ID);
        };
    }
}
