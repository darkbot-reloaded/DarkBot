package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

/**
 * API to manage {@link HeroAPI} items.
 */
public interface HeroItemsAPI extends API.Singleton {

    /**
     * This method checks if given {@link Item} can be selected in-game.
     *
     * @param item to check
     * @return true if item can beb selected
     */
    boolean isSelectable(@NotNull Item item);

    /**
     * @param item to be selected
     * @return true on successful select
     */
    boolean selectItem(@NotNull Item item);

    /**
     * @param category {@link Category} to be checked
     * @return true if contains given Category
     */
    boolean hasCategory(@NotNull HeroItemsAPI.Category category);

    /**
     * @param category to get item list from
     * @return list of items associated with given {@link Category}
     */
    Collection<? extends Item> getItems(@NotNull HeroItemsAPI.Category category);

    /**
     * Search every {@link Category} for given {@code itemId}.
     *
     * @param itemId to be searched for
     * @return first encounter of given item id
     */
    default Optional<Item> findItem(String itemId) {
        for (Category category : Category.values()) {
            if (!hasCategory(category)) continue;

            Optional<Item> item = findItem(category, itemId);
            if (item.isPresent()) return item;
        }
        return Optional.empty();
    }

    /**
     * Search {@link Category} for given {@code itemId}.
     *
     * @param category to be searched
     * @param itemId   to be looked for in given category
     * @return first encounter of given item id
     */
    default Optional<Item> findItem(@NotNull HeroItemsAPI.Category category, String itemId) {
        if (!hasCategory(category))
            return Optional.empty();

        for (Item item : getItems(category))
            if (item.getId().equals(itemId))
                return Optional.of(item);

        return Optional.empty();
    }

    /**
     * Represents all available categories of {@link HeroItemsAPI} items.
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
