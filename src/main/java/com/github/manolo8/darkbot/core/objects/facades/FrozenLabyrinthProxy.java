package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class FrozenLabyrinthProxy extends Updatable {
    public double time;
    public double synkTime;
    public int keys;
    public String labStatus;


    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.keys = API.readMemoryInt(API.readMemoryLong(data + 0x68) + 0x28);
        this.time = API.readMemoryDouble(API.readMemoryLong(data + 0x58) + 0x38);
        this.labStatus = API.readMemoryString(API.readMemoryLong(data+0x60));
        //this.synkTime = API.readMemoryDouble(API.readMemoryLong(data + 0x50) + 0x38);

    }
}
