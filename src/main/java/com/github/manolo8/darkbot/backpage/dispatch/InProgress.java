package com.github.manolo8.darkbot.backpage.dispatch;

public class InProgress {
    protected String slotId, id, name, collectable;

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


    @Override
    public String toString() {
        return "Retriever{" +
                "slotID=" + slotID +
                "id=" + id +
                "name=" + name +
                "collectable=" + collectable +
                "}";
    }
}
