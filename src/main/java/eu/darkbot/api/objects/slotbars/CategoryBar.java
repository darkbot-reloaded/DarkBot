package eu.darkbot.api.objects.slotbars;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public interface CategoryBar {

    /**
     * @param category {@link Category} to be checked
     * @return true if {@link CategoryBar} contains given Category
     */
    boolean hasCategory(@NotNull CategoryBar.Category category);

    /**
     * @param category to get item list from
     * @return list of items associated with given {@link Category}
     */
    List<Item> getItems(@NotNull CategoryBar.Category category);

    /**
     * Search every {@link Category} for given {@code itemId}.
     *
     * @param itemId to be searched for
     * @return first encounter of given item id or null if none
     */
    @Nullable
    default Item findItemById(String itemId) {
        for (Category category : Category.values()) {
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
    default Item findItemById(@NotNull CategoryBar.Category category, String itemId) {
        return getItems(category).stream()
                .filter(item -> item.getId().equals(itemId))
                .findAny().orElse(null);
    }

    /**
     * Represents all available categories of {@link CategoryBar}
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
}
