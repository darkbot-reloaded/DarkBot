package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.items.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * API to manage in-game items, from ammo, to rockets, abilities or even fireworks.
 *
 * @see SelectableItem
 */
public interface HeroItemsAPI extends API.Singleton {
    /**
     * @return {@link Item} representation of given {@link SelectableItem} if exists, empty optional otherwise
     */
    Optional<Item> getItem(@NotNull SelectableItem selectableItem);

    /**
     * @param itemCategory the category to get items from
     * @return a {@link Collection<Item>} with all the items inside the given {@link ItemCategory}
     */
    Collection<? extends Item> getItems(@NotNull ItemCategory itemCategory);

    /**
     * This method checks if given {@link Item} is available and can be used in-game.
     * <b>Doesn't</b> check if item is not cooling down or if the quantity is sufficient.
     *
     * If the item isn't in any of the action bars it may not be selectable.
     *
     * @param selectableItem to check
     * @return non-empty optional if item available and can be used, empty optional otherwise
     */
    Optional<Item> getAvailable(@NotNull SelectableItem selectableItem);

    /**
     * Will try to use given {@link SelectableItem} with optional additional {@link ItemUseFlag}s.
     *
     * @param selectableItem item to be used
     * @param itemFlags      flags which this method must respect
     * @return use result of the selectableItem
     */
    ItemUseResult useItem(@NotNull SelectableItem selectableItem, ItemUseFlag... itemFlags);
}
