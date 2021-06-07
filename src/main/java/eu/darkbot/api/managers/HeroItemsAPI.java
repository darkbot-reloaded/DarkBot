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
     * @param selectableItem to get {@link Item} instance
     * @return {@link Optional<Item>} associated with given {@link SelectableItem}
     */
    Optional<Item> getItem(@NotNull SelectableItem selectableItem);

    /**
     * Returns {@link Collection<Item>} of given {@link ItemCategory}
     */
    Collection<? extends Item> getItems(@NotNull ItemCategory itemCategory);

    /**
     * This method checks if given {@link Item} can be used in-game.
     * If the item isn't in any of the action bars it may not be selectable.
     *
     * @param item to check
     * @return non empty optional if item can be used
     */
    Optional<Item> getAvailable(@NotNull SelectableItem item);

    /**
     * Will try to use given {@link SelectableItem} with optional additional {@link ItemUseFlag}s.
     * API should check {@link Item#isAvailable()} by default,
     * and return {@link ItemUseResult#NOT_AVAILABLE} if isn't available.
     *
     * @param selectableItem to be used
     * @param itemFlags      which this method must respect
     * @return use result of the selectableItem
     */
    ItemUseResult useItem(@NotNull SelectableItem selectableItem, ItemUseFlag... itemFlags);
}
