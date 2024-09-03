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
    private Status status = Status.INACTIVE;

    public void update() {
        long data = API.readLong(address + 48) & ByteUtils.ATOM_MASK;

        this.status = API.readBoolean(data + 0x44) ? Status.ACTIVE : Status.INACTIVE;
        this.remainingTime = API.readDouble(API.readLong(data + 0x50) + 0x38);
        this.eventDescription = API.readString(API.readLong(data + 0x60));
        this.npcLeftDescription = API.readString(API.readLong(data + 0x68));
        this.eventDescriptionId = API.readString(API.readLong(data + 0x70));
        this.eventId = API.readString(API.readLong(data + 0x78));
        this.eventName = API.readString(API.readLong(data + 0x80));
        this.npcLeft = API.readInt(data + 0x88);
        this.bossNpcLeft = API.readInt(data + 0x90);
    }

}
