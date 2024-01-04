package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.NpcEventAPI;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class NpcEventProxy extends Updatable implements NpcEventAPI.NpcEvent {
    private double remainingTime;
    private String eventId;
    private String eventName;
    private String eventDescriptionId;
    private String eventDescription;
    private String npcLeftDescription;
    @Accessors(fluent = true)
    private int npcLeft, bossNpcLeft;
    private Status status;

    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.status = API.readBoolean(data + 0x44) ? Status.ACTIVE : Status.INACTIVE;
        this.remainingTime = API.readMemoryDouble(API.readMemoryLong(data + 0x50) + 0x38);
        this.eventDescription = API.readMemoryString(API.readMemoryLong(data + 0x60));
        this.npcLeftDescription = API.readMemoryString(API.readMemoryLong(data + 0x68));
        this.eventDescriptionId = API.readMemoryString(API.readMemoryLong(data + 0x70));
        this.eventId = API.readMemoryString(API.readMemoryLong(data + 0x78));
        this.eventName = API.readMemoryString(API.readMemoryLong(data + 0x80));
        this.npcLeft = API.readInt(data + 0x88);
        this.bossNpcLeft = API.readInt(data + 0x90);
    }

}
