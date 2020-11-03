package eu.darkbot.api.objects.slotbars;

public interface Item {

    /**
     * @return id of the item
     */
    String getId();

    /**
     * @return current quantity of item
     */
    double getQuantity();

    /**
     * @return true if plugin is ready, available and can be clicked
     */
    boolean isReady();

    /**
     * @return true if item is selected
     */
    boolean isSelected();

    /**
     * @return true if item can be bought via click
     */
    boolean isBuyable();

    /**
     * @return true if item can be activated
     */
    boolean isActivatable();

    /**
     * @return true if item is available and is not greyed out
     */
    boolean isAvailable();
    //boolean isVisible();

    /**
     * TODO
     * @return time in (ms, sec???) needed to be passed
     */
    double readyIn();

    /**
     * TODO
     * @return total cooldown time in (ms, sec???) of item
     */
    double totalCooldown();
}
