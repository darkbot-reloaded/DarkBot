package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Point;
import eu.darkbot.api.objects.slotbars.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;

/**
 * API to get info about slot bars,
 * category bar, items etc.
 */
public interface SlotBarAPI extends API {

    /**
     * @param slotBarType to be checked
     * @return true if has given {@link SlotBarAPI.Type}
     */
    boolean hasSlotBar(SlotBarAPI.Type slotBarType);

    /**
     * @param slotBarType to be checked
     * @return true if slot bar is visible, useful for {@link Type#PRO_ACTION_BAR}
     */
    boolean isSlotBarVisible(SlotBarAPI.Type slotBarType);

    Point getSlotBarPosition(SlotBarAPI.Type slotBarType);

    /**
     * @param category {@link Category} to be checked
     * @return true if contains given Category
     */
    boolean hasCategory(@NotNull SlotBarAPI.Category category);

    /**
     * @param category to get item list from
     * @return list of items associated with given {@link Category}
     */
    Collection<? extends Item> getItems(@NotNull SlotBarAPI.Category category);

    /**
     * Search every {@link Category} for given {@code itemId}.
     *
     * @param itemId to be searched for
     * @return first encounter of given item id or null if none
     */
    @Nullable
    default Item findItemById(String itemId) {
        for (Category category : Category.values()) {
            if (!hasCategory(category)) continue;

            Item item = findItemById(category, itemId);
            if (item != null) return item;
        }
        return null;
    }

    /**
     * Search {@link Category} for given {@code itemId}.
     *
     * @param category to be searched
     * @param itemId   to be looked for in given category
     * @return first encounter of given item id or null if none
     */
    @Nullable
    default Item findItemById(@NotNull SlotBarAPI.Category category, String itemId) {
        return getItems(category).stream()
                .filter(item -> item.getId().equals(itemId))
                .findAny().orElse(null);
    }

    /**
     * Represents all available categories of {@link SlotBarAPI} items.
     */
    enum Category {
        LASERS,
        ROCKETS,
        ROCKET_LAUNCHERS,
        SPECIAL_ITEMS,
        MINES,
        CPUS,
        BUY_NOW,
        TECH_ITEMS,
        SHIP_ABILITIES,
        DRONE_FORMATIONS,
        PET;

        public String getId() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    enum Type {
        DEFAULT_BAR,
        PREMIUM_BAR(KeyBindsAPI.Shortcut.PREMIUM_BAR),
        PRO_ACTION_BAR;

        private final KeyBindsAPI.Shortcut shortcutType;

        Type() {
            this(KeyBindsAPI.Shortcut.DEFAULT_BAR);
        }

        Type(KeyBindsAPI.Shortcut shortcutType) {
            this.shortcutType = shortcutType;
        }

        public KeyBindsAPI.Shortcut getShortcutType() {
            return shortcutType;
        }
    }
}
