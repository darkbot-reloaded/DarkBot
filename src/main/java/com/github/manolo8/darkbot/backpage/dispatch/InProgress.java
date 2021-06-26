package com.github.manolo8.darkbot.backpage.dispatch;

public class InProgress {
    protected String slotID, name, collectable;

    public String getSlotID() {
        return slotID;
    }

    public void setSlotID(String slotID) {
        this.slotID = slotID;
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
    public String toString(){
        return "Retriever{" +
                "slotID=" + slotID +
                "name=" + name +
                "collectable=" + collectable +
                "}"
                ;
    }
}
