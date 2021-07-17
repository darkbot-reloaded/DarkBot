package eu.darkbot.api.game.items;

/**
 * Represents an item in-game, the type of this item is a {@link SelectableItem}
 */
public interface Item extends SelectableItem {

    /**
     * @return id of the {@link Item}
     */
    String getId();

    /**
     * @return current quantity of item
     */
    double getQuantity();

    /**
     * @return true if item can be used in-game and by API
     */
    boolean isUsable();

    /**
     * @return true if item is selected
     */
    boolean isSelected();

    /**
     * @return true if item can be bought via click
     */
    boolean isBuyable();

    /**
     * @return true if item is available and is not greyed out
     */
    boolean isAvailable();

    /**
     * @return true if item is ready - not cooling down
     */
    boolean isReady();

    /**
     * @return time in {@code milliseconds} needed to be passed till {@link Item} will be available
     */
    double readyIn();

    /**
     * @return total cooldown time in {@code milliseconds} of {@link Item}
     */
    double totalCooldown();

    @Override
    default ItemCategory getCategory() {
        return null; // Category is unknown
    }
}
