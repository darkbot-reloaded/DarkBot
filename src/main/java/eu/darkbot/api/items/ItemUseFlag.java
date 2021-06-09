package eu.darkbot.api.items;

import java.util.function.Predicate;

/**
 * Additional flags for item usage.
 *
 * @see eu.darkbot.api.managers.HeroItemsAPI#useItem(SelectableItem, ItemUseFlag...)
 */
public enum ItemUseFlag {
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

    ItemUseFlag(Predicate<Item> filter, ItemUseResult failResult) {
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
