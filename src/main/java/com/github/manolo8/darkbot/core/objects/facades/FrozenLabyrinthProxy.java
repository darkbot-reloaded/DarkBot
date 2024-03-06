package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.FrozenLabyrinthAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.github.manolo8.darkbot.Main.API;

public class FrozenLabyrinthProxy extends Updatable implements FrozenLabyrinthAPI {
    public double time;
    public int keys;
    public String labStatus;
    public String synkMap;
    public int synkZone;

    public void update() {
        long data = API.readLong(address + 48) & ByteUtils.ATOM_MASK;
        long labVoAddr = API.readLong(data + 0x70) & ByteUtils.ATOM_MASK;

        this.keys = API.readInt(API.readLong(data + 0x68) + 0x28);
        this.time = API.readDouble(API.readLong(data + 0x58) + 0x38);
        this.labStatus = API.readString(API.readLong(data+0x60));
        this.synkMap = API.readString(API.readLong(labVoAddr + 0x28));
        this.synkZone = API.readInt(labVoAddr + 0x20);
    }

    @Override
    public double getRemainingTime() {
        return time;
    }

    @Override
    public int getKeyCount() {
        return keys;
    }

    @Override
    public Status getStatus() {
        if(labStatus != null && !labStatus.isEmpty()) {
            if (labStatus.contains("open")) return Status.OPEN;
            else if (labStatus.contains("closed")) return Status.CLOSED;
        }
        return Status.ENDED;
    }

    @Override
    public @Nullable MapZone getSynkMapZone() {
        if (synkMap == null || synkZone < 1 || synkZone > 4) return null;
        try {
            return LabMap.valueOf(synkMap.toUpperCase(Locale.ROOT).replace(' ', '_'))
                    .z(synkZone - 1);
        } catch (IllegalArgumentException e) {
            System.err.println("Couldn't find LabMap for '" + synkMap + "' and zone '" + synkZone + "'");
            return null;
        }
    }
}
