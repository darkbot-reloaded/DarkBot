package eu.darkbot.api.items;

import java.util.function.Predicate;

public enum ItemUseFlag {
    READY(Item::isReady, ItemUseResult.NOT_READY),
    NOT_SELECTED(item -> !item.isSelected(), ItemUseResult.ALREADY_SELECTED),
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
