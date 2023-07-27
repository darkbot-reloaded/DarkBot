package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;

public class QuestProxy extends Updatable implements API.Singleton {

    private final ObjArray questConditionsArr = ObjArray.ofVector(true);
    private boolean active;
    private String category, title, descriptiuon;
    private int id;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long questClass = API.readMemoryLong(address + 0x98) & ByteUtils.ATOM_MASK;

        long actualQuest = API.readMemoryLong(questClass + 0x28) & ByteUtils.ATOM_MASK;

        this.id = API.readMemoryInt(actualQuest + 0x20);
        this.active = API.readMemoryBoolean(actualQuest + 0x24);
        this.category = API.readMemoryString(actualQuest, 0x48);
        this.title = API.readMemoryString(actualQuest, 0x68);
        this.descriptiuon = API.readMemoryString(actualQuest, 0x70);
    }

}