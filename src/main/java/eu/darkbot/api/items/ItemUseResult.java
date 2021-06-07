package eu.darkbot.api.items;

import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.function.Consumer;

/**
 * {@link HeroItemsAPI#useItem(SelectableItem, ItemUseFlag...)} results
 */
public enum ItemUseResult {
    /**
     * Item was used successfully
     */
    SUCCESS,
    /**
     * Failed to use an item
     */
    FAILED,
    /**
     * Item isn't ready
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
     * Item is already selected if {@link ItemUseFlag#NOT_SELECTED} flag was used
     *
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
     * Is executed only on {@link #SUCCESS} result
     *
     * @param onSuccess consumer
     * @return this {@link ItemUseResult} instance
     */
    public ItemUseResult ifSuccessful(Consumer<ItemUseResult> onSuccess) {
        if (this == SUCCESS) onSuccess.accept(this);
        return this;
    }

    /**
     * Is executed only on <b>non</b> {@link #SUCCESS} result.
     *
     * @param onFail consumer
     * @return this {@link ItemUseResult} instance
     */
    public ItemUseResult ifFailed(Consumer<ItemUseResult> onFail) {
        if (this != SUCCESS) onFail.accept(this);
        return this;
    }
}
