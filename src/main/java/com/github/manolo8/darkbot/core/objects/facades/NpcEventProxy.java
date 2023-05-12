package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.NpcEventAPI;

import static com.github.manolo8.darkbot.Main.API;

public class NpcEventProxy extends Updatable implements NpcEventAPI {
    public double time;
    public String eventID;
    public String eventName;
    public String eventDescriptionID;
    public String eventDescription;
    public String npcLeftStatus;
    public double leftToKill;
    public double bossCount;
    public boolean eventActive;

    @Override
    public double getRemainingTime() {
        return time;
    }

    @Override
    public NpcEventAPI.Status getStatus() {
        if (this.eventActive) {
            return Status.ACTIVE;
        } else {
            return Status.INACTIVE;
        }
    }

    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.eventActive = API.readBoolean(data+0x44);
        this.time = API.readMemoryDouble(API.readMemoryLong(data+0x50)+0x38);
        this.eventDescription = API.readMemoryString(API.readMemoryLong(data+0x60));
        this.npcLeftStatus = API.readMemoryString(API.readMemoryLong(data+0x68));
        this.eventDescriptionID = API.readMemoryString(API.readMemoryLong(data+0x70));
        this.eventID = API.readMemoryString(API.readMemoryLong(data+0x78));
        this.eventName = API.readMemoryString(API.readMemoryLong(data+0x80));
        this.leftToKill = API.readDouble(data+0x88);
        this.bossCount = API.readDouble(data+0x90);
    }
}
