package com.github.manolo8.darkbot.core.entities;

import static com.github.manolo8.darkbot.Main.API;

public class Box extends Entity {

    protected boolean collected;

    public String type;

    public Box(int id) {
        super(id);
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    @Override
    public void update(long address) {
        super.update(address);

        long info = traits.elements[0];

//        System.out.println(API.readMemoryLong(API.readMemoryLong(API.readMemoryLong(info + 64) + 32) + 16));

        long xml = API.readMemoryLong(info + 64);
        xml = API.readMemoryLong(xml + 32);
        xml = API.readMemoryLong(xml + 24);
        xml = API.readMemoryLong(xml + 8);
        xml = API.readMemoryLong(xml + 16);
        long data = xml = API.readMemoryLong(xml + 24);

        type = API.readMemoryString(data);

        if (type.length() > 5) {
            int index;
            this.type = (index = type.indexOf(',')) > 0 ? type.substring(4, index) : type.substring(4);
        } else {
            this.type = "";
        }

    }
}
