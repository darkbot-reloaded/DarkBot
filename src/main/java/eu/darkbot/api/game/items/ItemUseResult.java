package eu.darkbot.api.game.items;

import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.function.Consumer;

/**
 * The result after attempting to use an in-game item, usually via {@link HeroItemsAPI#useItem(SelectableItem, ItemFlag...)}
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
     * Only if {@link ItemFlag#READY} flag was used
     *
     * @see Item#isReady()
     * @see ItemFlag#READY
     */
    NOT_READY,
    /**
     * Item isn't available in-game.
     */
    NOT_AVAILABLE,
    /**
     * Item isn't usable in-game and/or API not able to use it.
     */
    NOT_USABLE,
    /**
     * Item is already selected.
     * Only if {@link ItemFlag#NOT_SELECTED} flag was used
     *
     * @see Item#isSelected()
     * @see ItemFlag#NOT_SELECTED
     */
    ALREADY_SELECTED,
    /**
     * Item have insufficient quantity, non-positive, <= 0
     * Only if {@link ItemFlag#POSITIVE_QUANTITY} flag was used
     *
     * @see ItemFlag#POSITIVE_QUANTITY
     */
    INSUFFICIENT_QUANTITY;

    /**
     * Checks if use attempt was successful
     *
     * @return true if item use attempt was successful, false otherwise
     */
    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    /**
     * It's executed on successful using of the item, like {@link #SUCCESS}
     *
     * @param onSuccess consumer to be executed on success
     * @return this {@link ItemUseResult} instance
     */
    public ItemUseResult ifSuccessful(Consumer<ItemUseResult> onSuccess) {
        if (isSuccessful()) onSuccess.accept(this);
        return this;
    }

    /**
     * It's executed if the use of the the item fails
     *
     * @param onFail consumer to be executed on fail
     * @return this {@link ItemUseResult} instance
     */
    public ItemUseResult ifFailed(Consumer<ItemUseResult> onFail) {
        if (!isSuccessful()) onFail.accept(this);
        return this;
    }
}
