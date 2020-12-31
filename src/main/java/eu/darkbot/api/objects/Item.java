package eu.darkbot.api.objects;

public interface Item {

    /**
     * @return id of the {@link Item}
     */
    String getId();

    /**
     * @return current quantity of item
     */
    double getQuantity();

    /**
     * @return true if item can be activated
     */
    boolean isActivatable();

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
     * @return true if item is ready, available and can be clicked
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
}
