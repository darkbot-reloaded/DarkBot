package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.DefaultNpcEventAPI;

import static com.github.manolo8.darkbot.Main.API;

public class DefaultNpcEventProxy extends Updatable implements DefaultNpcEventAPI {
    public double time;
    public String eventID;
    public String npcName;
    public String eventDescriptionID;
    public String eventDescription;
    public String npcLeftDescription;
    public int npcCount;
    public int bossCount;
    public boolean eventActive;

    @Override
    public double getRemainingTime() {
        return time;
    }

    @Override
    public String getNpcName() {
        return npcName;
    }

    @Override
    public String getEventID() {
        return eventID;
    }

    @Override
    public String getEventDescription() {
        return eventDescription;
    }

    @Override
    public String getNpcLeftDescription() {
        return npcLeftDescription;
    }

    @Override
    public int npcLeft() {
        return npcCount;
    }

    @Override
    public int bossNpcLeft() {
        return bossCount;
    }

    @Override
    public DefaultNpcEventAPI.Status getStatus() {
        if (this.eventActive) {
            return Status.ACTIVE;
        } else {
            return Status.INACTIVE;
        }
    }

    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.eventActive = API.readBoolean(data + 0x44);
        this.time = API.readMemoryDouble(API.readMemoryLong(data + 0x50) + 0x38);
        this.eventDescription = API.readMemoryString(API.readMemoryLong(data + 0x60));
        this.npcLeftDescription = API.readMemoryString(API.readMemoryLong(data + 0x68));
        this.eventDescriptionID = API.readMemoryString(API.readMemoryLong(data + 0x70));
        this.eventID = API.readMemoryString(API.readMemoryLong(data + 0x78));
        this.npcName = API.readMemoryString(API.readMemoryLong(data + 0x80));
        this.npcCount = API.readInt(data + 0x88);
        this.bossCount = API.readInt(data + 0x90);
    }
}
