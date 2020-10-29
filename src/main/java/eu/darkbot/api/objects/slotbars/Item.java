package eu.darkbot.api.objects.slotbars;

public interface Item {

    String getId();
    String getIconLootId();

    double getQuantity();

    boolean isSelected();
    boolean isBuyable();
    boolean isActivatable();
    boolean isAvailable();
    boolean isVisible();

    boolean isReady();

    double readyIn();
    double totalCooldown();
}
