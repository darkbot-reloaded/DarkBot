package eu.darkbot.api.items;

import java.util.function.Predicate;

/**
 * Additional flags for item usage.
 *
 * @see eu.darkbot.api.managers.HeroItemsAPI#useItem(SelectableItem, ItemFlag...)
 */
public enum ItemFlag {
    /**
     * Item must be available, be equipped on current ship configuration.
     */
    AVAILABLE(Item::isAvailable, ItemUseResult.NOT_AVAILABLE),
    /**
     * Item must be usable, available, ready and API can use it.
     * @see #AVAILABLE
     * @see #READY
     */
    USABLE(Item::isUsable, ItemUseResult.NOT_USABLE),
    /**
     * Item must be ready/not cooling down
     */
    READY(Item::isReady, ItemUseResult.NOT_READY),
    /**
     * Item must not be selected in-game
     */
    NOT_SELECTED(item -> !item.isSelected(), ItemUseResult.ALREADY_SELECTED),
    /**
     * Item need to have positive quantity, >0
     */
    POSITIVE_QUANTITY(item -> item.getQuantity() > 0, ItemUseResult.INSUFFICIENT_QUANTITY);

    private final Predicate<Item> filter;
    private final ItemUseResult failResult;

    ItemFlag(Predicate<Item> filter, ItemUseResult failResult) {
        this.filter = filter;
        this.failResult = failResult;
    }

    public boolean test(Item item) {
        return filter.test(item);
    }

    public ItemUseResult getFailResult() {
        return failResult;
    }
}
