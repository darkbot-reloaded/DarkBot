package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;

public class Item extends Updatable.Auto implements eu.darkbot.api.game.items.Item {
    private static final int START = 36, END = 128 + 8;
    private static final byte[] BUFFER = new byte[END - START];

    public final ItemTimer itemTimer = new ItemTimer();
    private final Map<SlotBarsProxy.Type, Set<Integer>> associatedSlots = new EnumMap<>(SlotBarsProxy.Type.class);

    public double quantity;
    public boolean selected, buyable, activatable, available, visible;
    public String id, counterType, actionStyle, iconLootId;

    private long lastUsed;

    private final ItemCategory itemCategory;
    public SelectableItem selectableItem;

    public Item() {
        this(null);
    }

    public Item(ItemCategory itemCategory) {
        this.itemCategory = itemCategory;
    }

    void removeSlot(SlotBarsProxy.Type slotType, int slotNumber) {
        getShortcutSet(slotType).remove(slotNumber);
    }

    void addSlot(SlotBarsProxy.Type slotType, int slotNumber) {
        getShortcutSet(slotType).add(slotNumber);
    }

    private Set<Integer> getShortcutSet(SlotBarsProxy.Type type) {
        return this.associatedSlots.computeIfAbsent(type, l -> new HashSet<>());
    }

    // TODO: 28.08.2022 Sometimes, after reload *some* items are probably invalid? Why?
    @Override
    public void update() {
        // There are *a lot* of items in-game
        // Doing 5 boolean-read calls is way more expensive than a single mem-read to the buffer
        // This IS very ugly, but improves performance.
        // We also avoid updating timer if no other flags change for the extra 3 long-read calls
        API.readMemory(address + START, BUFFER);

        buyable = BUFFER[0] == 1;
        activatable = BUFFER[4] == 1;
        selected = BUFFER[8] == 1;
        available = BUFFER[12] == 1;
        visible = BUFFER[16] == 1;
        quantity = ByteUtils.getDouble(BUFFER, 92);

        long timerAdr = API.readMemoryLong(ByteUtils.getLong(BUFFER, 52), 40);
        if (itemTimer.address != timerAdr) this.itemTimer.update(timerAdr);
        this.itemTimer.update();
    }

    @Override
    public void update(long address) {
        if (this.address != address) {
            this.id = API.readMemoryString(address, 64);
            this.counterType = API.readMemoryString(address, 72);
            this.actionStyle = API.readMemoryString(address, 80);
            this.iconLootId = API.readMemoryString(address, 96);

            if (itemCategory != null)
                this.selectableItem = SelectableItem.ALL_ITEMS.get(itemCategory).stream()
                        .filter(i -> i.getId().equals(id))
                        .findFirst()
                        .orElse(null);
        }
        super.update(address);
    }

    public boolean hasShortcut() {
        return getSlotBarType() != null;
    }

    public SlotBarsProxy.Type getSlotBarType() {
        return this.associatedSlots.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public int getFirstSlotNumber() {
        SlotBarsProxy.Type slotBarType = getSlotBarType();
        if (slotBarType == null) return -1;

        return getShortcutSet(slotBarType).stream()
                .findFirst().orElse(-1);
    }

    public boolean containsSlotNumber(int slotNumber) {
        SlotBarsProxy.Type slotBarType = getSlotBarType();
        if (slotBarType == null) return false;

        return getShortcutSet(slotBarType).contains(slotNumber == 0 ? 10 : slotNumber);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ItemCategory getCategory() {
        return itemCategory;
    }

    @Override
    public <T extends Enum<T> & SelectableItem> @Nullable T getAs(Class<T> type) {
        return type.isInstance(selectableItem) ? type.cast(selectableItem) : null;
    }

    @Override
    public double getQuantity() {
        return quantity;
    }

    @Override
    public boolean isUsable() {
        return isAvailable() && hasShortcut();
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isBuyable() {
        return buyable;
    }

    @Override
    public boolean isAvailable() {
        return available || isActivatable(); //Firework ignite is not available but activatable?
    }

    @Override
    public boolean isActivatable() {
        return activatable;
    }

    @Override
    public Optional<eu.darkbot.api.game.items.ItemTimer> getItemTimer() {
        return itemTimer.address == ByteUtils.NULL ? Optional.empty() : Optional.of(itemTimer);
    }

    @Override
    public long lastUseTime() {
        return lastUsed;
    }

    @Override
    public eu.darkbot.api.game.items.ItemTimer getTimer() {
        return itemTimer.address == 0 ? null : itemTimer;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemTimer=" + itemTimer +
                ", associatedSlots=" + associatedSlots +
                ", quantity=" + quantity +
                ", selected=" + selected +
                ", buyable=" + buyable +
                ", activatable=" + activatable +
                ", available=" + available +
                ", visible=" + visible +
                ", id='" + id + '\'' +
                ", counterType='" + counterType + '\'' +
                ", actionStyle='" + actionStyle + '\'' +
                ", iconLootId='" + iconLootId + '\'' +
                ", lastUsed=" + lastUsed +
                ", itemCategory=" + itemCategory +
                ", selectableItem=" + selectableItem +
                '}';
    }

    public class ItemTimer extends Auto implements eu.darkbot.api.game.items.ItemTimer {
        private final static String ACTIVE_ITEM_STATE = "active";

        public double elapsed, startTime, itemDelay, availableIn;

        private boolean isActivated = false;

        @Override
        public void update() {
            if (address == 0) {
                return; // reset was done on update(long), don't need to reset here
            }

            this.elapsed = API.readMemoryDouble(address + 72);
            this.availableIn = API.readMemoryDouble(address + 96);
        }

        @Override
        public void update(long address) {
            this.address = address;
            if (address == 0) {
                reset();
                return;
            }

            this.isActivated = API.readString(Item.this.address, 88, 32).equals(ACTIVE_ITEM_STATE);

            this.startTime = API.readMemoryDouble(address + 80);
            this.itemDelay = API.readMemoryDouble(address + 88);
        }

        public void reset() {
            this.elapsed = 0;
            this.startTime = 0;
            this.itemDelay = 0;
            this.availableIn = 0;
        }

        @Override
        public boolean isActivated() {
            return isActivated;
        }

        @Deprecated
        @Override
        public double getTotalCoolingTime() {
            return getTotalTime();
        }

        @Override
        public double getTotalTime() {
            return itemDelay;
        }

        @Override
        public double getTimeElapsed() {
            return elapsed;
        }

        @Override
        public double getAvailableIn() {
            return availableIn;
        }

        @Override
        public String toString() {
            return "ItemTimer{" +
                    "elapsed=" + elapsed +
                    ", startTime=" + startTime +
                    ", itemDelay=" + itemDelay +
                    ", availableIn=" + availableIn +
                    ", timerType=" + (isActivated ? "activated" : "cooling down") +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectableItem)) return false;

        if (o instanceof Item) {
            Item item = (Item) o;

            if (selectableItem != null && item.selectableItem != null)
                return selectableItem == item.selectableItem;
        }

        return ((SelectableItem) o).getId().equals(getId());
    }
}
