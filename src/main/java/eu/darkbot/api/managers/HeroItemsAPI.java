package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.other.SelectableItem;
import eu.darkbot.api.future.ItemFutureResult;
import eu.darkbot.api.objects.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * API to manage in-game items, from ammo, to rockets, abilities or even fireworks.
 *
 * @see SelectableItem
 */
public interface HeroItemsAPI extends API.Singleton {

    /**
     * This method checks if given {@link Item} can be selected in-game.
     * If the item isn't in any of the action bars it may not be selectable.
     *
     * @param item to check
     * @return non empty optional if item can be selected
     */
    Optional<Item> checkSelectable(@NotNull SelectableItem item);

    /**
     * @param item to be selected
     * @return true on successful select
     */
    ItemFutureResult selectItem(@NotNull SelectableItem item);

    /**
     * @param item to get of
     * @return item associated with given {@link SelectableItem}
     */
    Optional<Item> getItemOf(SelectableItem item);

    /**
     * @return {@link Map} of all {@link Item}s
     */
    Map<Category, List<? extends Item>> getItems();

    /**
     * {@link HeroItemsAPI#selectItem(SelectableItem)} results
     */
    enum UsageResult {
        SUCCESSFUL,
        UNSUCCESSFUL,
        ON_COOLDOWN,
        NOT_AVAILABLE;
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
