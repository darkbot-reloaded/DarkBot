package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.GauntletPlutusAPI;

import static com.github.manolo8.darkbot.Main.API;

public class GauntletPlutusProxy extends Updatable implements GauntletPlutusAPI {
    public String plutusStatus;

    public void update() {
        long data = API.readLong(address + 48) & ByteUtils.ATOM_MASK;
        long plutusVoAddr = API.readLong(data + 0x58) & ByteUtils.ATOM_MASK;

        this.plutusStatus = API.readString(API.readLong(plutusVoAddr + 0x30));
    }

    @Override
    public Status getStatus() {
        if (plutusStatus != null && !plutusStatus.isEmpty()) {
            if (plutusStatus.contains("spent")) return Status.INSIDE;
            else if (plutusStatus.contains("available")) return Status.AVAILABLE;
            else if (plutusStatus.contains("completed")) return Status.COMPLETED;
        }
        return Status.ENDED;
    }
}
