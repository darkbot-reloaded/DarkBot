package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

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
     * @return {@link Collection} of all {@link Item}s
     */
    Collection<? extends Item> getItems();

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
        return filterItem(id -> id.equals(itemId));
    }

    /**
     * Search {@link Category} for given {@code itemId}.
     *
     * @param category to be searched
     * @param itemId   to be looked for in given category
     * @return first encounter of given item id
     */
    default Optional<Item> findItem(@NotNull HeroItemsAPI.Category category, String itemId) {
        return filterItem(category, id -> id.equals(itemId));
    }

    /**
     * Filters items in every {@link Category}
     *
     * @param filter the items with
     * @return filtered {@link Optional<Item>} or {@link Optional#empty()}
     */
    default Optional<Item> filterItem(Predicate<String> filter) {
        for (Category category : Category.values()) {
            if (!hasCategory(category)) continue;

            Optional<Item> item = filterItem(category, filter);
            if (item.isPresent()) return item;
        }
        return Optional.empty();
    }

    /**
     * Filters items in given {@link Category}
     *
     * @param category to be searched
     * @param filter   the items with
     * @return filtered {@link Optional<Item>} or {@link Optional#empty()}
     */
    default Optional<Item> filterItem(@NotNull HeroItemsAPI.Category category,
                                      @NotNull Predicate<String> filter) {
        if (!hasCategory(category))
            return Optional.empty();

        for (Item item : getItems(category))
            if (filter.test(item.getId()))
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
