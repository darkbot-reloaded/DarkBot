package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.ConfigEntity;

import static com.github.manolo8.darkbot.Main.API;

public class Box extends Entity {

    private long collectedUntil, reset = 7_000;
    public String type;

    public BoxInfo boxInfo;

    public Box(int id) {
        super(id);
    }

    public boolean isCollected() {
        return removed || System.currentTimeMillis() < collectedUntil;
    }

    public void setCollected() {
        collectedUntil = System.currentTimeMillis() + reset;
        reset += reset < 15_000 ? 3_000 : 120_000;
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
