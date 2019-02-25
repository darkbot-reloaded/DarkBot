package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.ConfigEntity;

import static com.github.manolo8.darkbot.Main.API;

public class Box extends Entity {

    protected boolean collected;
    public String type;

    public BoxInfo boxInfo;

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

        if (traits.elements.length == 0) {
            boxInfo = new BoxInfo();
            return;
        }
        long data = traits.elements[0];

        data = API.readMemoryLong(data + 64);
        data = API.readMemoryLong(data + 32);
        data = API.readMemoryLong(data + 24);
        data = API.readMemoryLong(data + 8);
        data = API.readMemoryLong(data + 16);
        data = API.readMemoryLong(data + 24);

        type = API.readMemoryString(data);

        if (type.length() > 5) type = type.split(",")[0].replace("box_", "").replace("_box", "");

        boxInfo = ConfigEntity.INSTANCE.getOrCreateBoxInfo(type);
    }
}
