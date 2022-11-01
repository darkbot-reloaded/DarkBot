package com.github.manolo8.darkbot.backpage.dispatch;

public class InProgress {
    protected String slotId, id, name, collectable, dispatchRewardPackage;
    // Internal flag set to know if the item is already gone, and should be removed
    protected boolean forRemoval;

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollectable() {
        return collectable;
    }

    public void setCollectable(String collectable) {
        this.collectable = collectable;
    }

    public String getDispatchRewardPackage() {
        return dispatchRewardPackage;
    }

    public void setDispatchRewardPackage(String dispatchRewardPackage) {
        this.dispatchRewardPackage = dispatchRewardPackage;
    }

    public boolean getForRemoval() {
        return this.forRemoval;
    }

    public void setForRemoval(boolean forRemoval) {
        this.forRemoval = forRemoval;
    }

    @Override
    public String toString() {
        return "Retriever{" +
                " slotID=" + slotId +
                ", id=" + id +
                ", name=" + name +
                ", collectable=" + collectable +
                " }";
    }
}
