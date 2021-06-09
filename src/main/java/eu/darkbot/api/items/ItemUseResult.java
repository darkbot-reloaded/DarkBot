package eu.darkbot.api.items;

import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.function.Consumer;

/**
 * The result after attempting to use an in-game item, usually via {@link HeroItemsAPI#useItem(SelectableItem, ItemUseFlag...)}
 */
public enum ItemUseResult {
    /**
     * Item was used successfully
     */
    SUCCESS,
    /**
     * Failed to use an item. Item was available but failed to use it.
     */
    FAILED,
    /**
     * Item isn't ready - is cooling down, so can't be used
     * Only if {@link ItemUseFlag#READY} flag was used
     *
     * @see Item#isReady()
     * @see ItemUseFlag#READY
     */
    NOT_READY,
    /**
     * Item isn't available to use by either implementation and/or in-game.
     */
    NOT_AVAILABLE,
    /**
     * Item is already selected.
     * Only if {@link ItemUseFlag#NOT_SELECTED} flag was used
     *
     * @see Item#isSelected()
     * @see ItemUseFlag#NOT_SELECTED
     */
    ALREADY_SELECTED,
    /**
     * Item have insufficient quantity, non-positive, <= 0
     * Only if {@link ItemUseFlag#POSITIVE_QUANTITY} flag was used
     *
     * @see ItemUseFlag#POSITIVE_QUANTITY
     */
    INSUFFICIENT_QUANTITY;

    /**
     * It's executed on successful using of the item, like {@link #SUCCESS}
     *
     * @param onSuccess consumer to be executed on success
     * @return this {@link ItemUseResult} instance
     */
    public ItemUseResult ifSuccessful(Consumer<ItemUseResult> onSuccess) {
        if (this == SUCCESS) onSuccess.accept(this);
        return this;
    }

    /**
     * It's executed if the use of the the item fails
     *
     * @param onFail consumer to be executed on fail
     * @return this {@link ItemUseResult} instance
     */
    public ItemUseResult ifFailed(Consumer<ItemUseResult> onFail) {
        if (this != SUCCESS) onFail.accept(this);
        return this;
    }
}
